package com.mountains.bledemo.presenter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import com.mountains.bledemo.App
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.WallpaperInfoBean
import com.mountains.bledemo.bean.WallpaperPackage
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.ble.callback.NotifyCallback
import com.mountains.bledemo.helper.BaseUUID
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.util.HexUtil
import com.mountains.bledemo.view.WallpaperView
import com.orhanobut.logger.Logger
import org.litepal.LitePal
import org.litepal.extension.find
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder


class WallpaperPresenter : BasePresenter<WallpaperView>() {
    private var wallpaperBitmap: Bitmap? = null
    private var wallpaperInfo:WallpaperInfoBean? = null
    private var thread: Thread? = null

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var isSupportWallpaper: Boolean = false
    private var isWallpaperEnable: Boolean = false
    private var isTimeEnable: Boolean = false
    private var isStepEnable: Boolean = false
    private var timeFontSize: IntArray? = null
    private var stepFontSize: IntArray? = null

    companion object {
        val lock = Object()
        private var isUploadWallpaper = false
    }


    private val notifyCallback = object : NotifyCallback {
        override fun onNotify(uuid: String, byteArray: ByteArray?) {
            if (uuid == BaseUUID.NOTIFY_WALLPAPER) {
                Logger.i("NOTIFY_WALLPAPER")
                synchronized(lock) {
                    lock.notify()
                }
            } else if (uuid == BaseUUID.NOTIFY) {
                if (HexUtil.bytes2HexString(byteArray).startsWith("05021101")) {
                    wallpaperBitmap?.let {
                        Logger.i("已开启高速模式,开始上传壁纸")
                        uploadWallpaper(it)
                    }
                }
                if (byteArray != null && HexUtil.bytes2HexString(byteArray).startsWith("050114")) {
                    screenWidth = HexUtil.subBytesToInt(byteArray, 2, 3, 4)
                    screenHeight = HexUtil.subBytesToInt(byteArray, 2, 5, 6)
                    isSupportWallpaper = byteArray[7].toInt() and 255 == 1
                    isWallpaperEnable = byteArray[8].toInt() and 255 == 1
                    isTimeEnable = byteArray[9].toInt() and 255 == 1
                    isStepEnable = byteArray[10].toInt() and 255 == 1

                    //onLoadWallpaperInfo(isSupportWallpaper, isWallpaperEnable, screenWidth, screenHeight, this.screenType, isTimeEnable isStepEnable, this.fontList, this.wallpaperSrc, zArr[0], this.timeLocation, this.fontColor);
                    Logger.i("屏幕尺寸:$screenWidth,$screenHeight")
                    Logger.i("支持屏保:$isSupportWallpaper")
                    Logger.i("屏保开关:$isWallpaperEnable")
                    Logger.i("时间开关:$isTimeEnable")
                    Logger.i("步数开关:$isStepEnable")

                }

                if (byteArray != null && HexUtil.bytes2HexString(byteArray).startsWith("050115")) {
                    val b = byteArray[3].toInt() and 255
                    val fontSizeList = mutableListOf<IntArray>()
                    for (i in 4 until 4 + b * 2 step 2) {
                        val fontWidth = byteArray[i].toInt() and 255
                        val fontHeight = byteArray[i + 1].toInt() and 255
                        fontSizeList.add(intArrayOf(fontWidth, fontHeight))
                    }
                    if (fontSizeList.size > 2) {
                        timeFontSize = intArrayOf(fontSizeList[0][0], fontSizeList[0][1])
                        stepFontSize = intArrayOf(fontSizeList[1][0], fontSizeList[1][1])
                    }

                    var wallpaperBitmap :Bitmap? = null
                    var timeLocation:Point? = null
                    var fontColor:Int = -1
                    DeviceManager.getDevice()?.getMac()?.let {
                        val wallpaperInfoBean = LitePal.where("mac = ?", it).find<WallpaperInfoBean>().lastOrNull()
                        wallpaperInfoBean?.let {
                            timeLocation = Point().apply {
                                x = it.timeLocationX
                                y = it.timeLocationY
                            }
                            fontColor = it.fontColor
                            wallpaperBitmap = if (it.bitmapPath == null){
                                BitmapFactory.decodeFile(it.bitmapPath)
                            }else{
                                BitmapFactory.decodeResource(App.context.resources, R.drawable.ic_default_wallpaper)
                            }
                        }
                    }


                    hideLoading()
                    view?.onWallpaperInfo(
                        wallpaperBitmap,
                        screenWidth,
                        screenHeight,
                        isSupportWallpaper,
                        isWallpaperEnable,
                        isTimeEnable,
                        isStepEnable,
                        timeFontSize,
                        stepFontSize,
                        fontColor,
                        timeLocation
                    )
                }
            }
        }

    }




    private fun writeCharacteristic(byteArray: ByteArray, e: () -> Unit = {}) {
        DeviceManager.writeCharacteristic(byteArray, object : CommCallback {
            override fun onSuccess(byteArray: ByteArray?) {
                e()
            }

            override fun onFail(exception: BleException) {
                showToast("发生错误:${exception.message}")
                hideLoading()
            }
        })
    }


    //开启通知
    fun init() {
        showLoading()
        DeviceManager.getDevice()?.addNotifyCallBack(notifyCallback)
        DeviceManager.getDevice()?.enableNotify(BaseUUID.SERVICE, BaseUUID.NOTIFY_WALLPAPER, BaseUUID.DESC, true, object : CommCallback {
                override fun onSuccess(byteArray: ByteArray?) {
                    Logger.i("开启壁纸通知成功")
                    hideLoading()
                    view?.initSuccess()
                }

                override fun onFail(exception: BleException) {
                    Logger.i("开启壁纸通知失败：${exception.message}")
                    showToast("开启壁纸通知失败")
                    hideLoading()
                    view?.initFail()
                }
            })
    }

    //释放，关闭通知
    fun release() {
        thread?.interrupt()
        DeviceManager.getDevice()?.removeNotifyCallBack(notifyCallback)
        DeviceManager.getDevice()?.enableNotify(BaseUUID.SERVICE, BaseUUID.NOTIFY_WALLPAPER, BaseUUID.DESC, false)
    }

    //获取设备壁纸信息
    fun getWallpaperInfo() {
        showLoading()
        writeCharacteristic(CommHelper.getWallpaperScreenInfo()) {
            writeCharacteristic(CommHelper.getWallpaperFontInfo())
        }
    }


    fun setWallpaper(wallpaperInfoBean: WallpaperInfoBean) {
        showLoading()
        wallpaperInfo = wallpaperInfoBean
        wallpaperInfoBean.apply {
            //是否开启壁纸
            writeCharacteristic(CommHelper.setWallpaperEnable(enableWallpaper)) {
                //设置壁纸时间
                writeCharacteristic(
                    CommHelper.setWallpaperTimeInfo(
                        isTimeEnable,
                        timeFontWidth,
                        timeFontHeight,
                        fontColor,
                        timeLocationX,
                        timeLocationY
                    )
                ) {
                    //设置壁纸步数
                    writeCharacteristic(
                        CommHelper.setWallpaperStepInfo(
                            isStepEnable,
                            stepFontWidth,
                            stepFontHeight,
                            fontColor,
                            stepLocationX,
                            stepLocationY
                        )
                    ) {
                        hideLoading()
                        if (bitmap != null) {
                            //设置壁纸
                            Logger.i("开始上传壁纸")
                            setWallpaper(bitmap!!)
                        }
                    }
                }
            }
        }

    }


    private fun setWallpaper(bitmap: Bitmap) {
        view?.onUploadStart()
        uploadWallpaperProgress(0, 100)
        wallpaperBitmap = bitmap
        setHighSpeedTransportStatus(true)
    }

    private fun uploadWallpaper(bitmap: Bitmap) {
        if (isUploadWallpaper) {
            return
        }
        isUploadWallpaper = true
        thread = Thread(Runnable {
            try {
                synchronized(lock) {
                    val wallpaperPackageList = CommHelper.createWallpaperPackage(bitmap)
                    for (index in wallpaperPackageList.indices) {
                        if (thread!=null && thread!!.isInterrupted){
                            break
                        }
                        wallpaperPackageList[index].bytes20.forEach {
                            DeviceManager.writeWallpaperCharacteristic(it)
                        }
                        uploadWallpaperProgress(index, wallpaperPackageList.size - 1)
                        Logger.i("WAIT_WALLPAPER,当前：$index,总共:${wallpaperPackageList.size - 1}")
                        lock.wait()
                    }
                    uploadSuccess()
                }
            } catch (e:InterruptedException){
                e.printStackTrace()
                Logger.i("中断上传")
            }catch (e: Exception) {
                e.printStackTrace()
                showToast("上传壁纸发送错误")
            } finally {
                isUploadWallpaper = false
                wallpaperBitmap = null
                uploadStop()
            }
        })
        thread?.start()

    }

    private fun uploadWallpaperProgress(current: Int, total: Int) {
        Handler(Looper.getMainLooper()).post {
            view?.onUploadWallpaperProgress(current, total)
        }
    }


    private fun uploadStop() {
        thread?.interrupt()
        setHighSpeedTransportStatus(false)
        Handler(Looper.getMainLooper()).post {
            hideLoading()
            view?.onUploadStop()
        }
    }

    private fun uploadSuccess(){
        wallpaperInfo?.let {
            //保存bitmap，保存路径存进数据库
            it.bitmap?.let {bitmap->
                val file = File(App.context.filesDir, "${it.mac}${System.currentTimeMillis()}.jpg")
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,file.outputStream())
                it.bitmapPath = file.path
                Logger.i("保存壁纸，路径${file.path}")
            }

            val wallpaperInfo = LitePal.where("mac = ?", it.mac).find<WallpaperInfoBean>().lastOrNull()
            if (wallpaperInfo != null){
                it.assignBaseObjId(wallpaperInfo.id)
           }
            it.save()
        }
    }

    private fun setHighSpeedTransportStatus(open: Boolean) {
        DeviceManager.writeCharacteristic(CommHelper.setHighSpeedTransportStatus(open))
    }


    private fun showLoading() {
        Handler(Looper.getMainLooper()).post {
            view?.showLoading()
        }
    }

    private fun hideLoading() {
        Handler(Looper.getMainLooper()).post {
            view?.hideLoading()
        }
    }

    private fun showToast(message:String){
        Handler(Looper.getMainLooper()).post {
            view?.showToast(message)
        }
    }

}