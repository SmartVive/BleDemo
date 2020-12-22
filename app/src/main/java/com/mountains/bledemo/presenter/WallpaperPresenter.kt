package com.mountains.bledemo.presenter

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
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
import java.nio.ByteBuffer
import java.nio.ByteOrder


class WallpaperPresenter : BasePresenter<WallpaperView>() {
    private var wallpaperBitmap : Bitmap?= null
    private var thread : Thread? = null

    private val notifyCallback = object : NotifyCallback {
        override fun onNotify(uuid: String, byteArray: ByteArray?) {
            if (uuid == BaseUUID.NOTIFY_WALLPAPER) {
                Logger.i("NOTIFY_WALLPAPER")
                synchronized(lock) {
                    lock.notify()
                }
            }else if (uuid == BaseUUID.NOTIFY){
                if (HexUtil.bytes2HexString(byteArray).startsWith("05021101")){
                    Logger.i("开启高速模式,开始上传壁纸")
                    wallpaperBitmap?.let {
                        uploadWallpaper(it)
                    }
                }
                if (byteArray != null && HexUtil.bytes2HexString(byteArray).startsWith("050114")){
                    val screenWidth = HexUtil.subBytesToInt(byteArray,2,3,4)
                    val screenHeight = HexUtil.subBytesToInt(byteArray,2,5,6)
                    val isSupportWallpaper = byteArray[7].toInt() and 255 == 1
                    val isWallpaperEnable = byteArray[8].toInt() and 255 == 1
                    val isTimeEnable = byteArray[9].toInt() and 255 == 1
                    val isStepEnable = byteArray[10].toInt() and 255 == 1

                    //onLoadWallpaperInfo(isSupportWallpaper, isWallpaperEnable, screenWidth, screenHeight, this.screenType, isTimeEnable isStepEnable, this.fontList, this.wallpaperSrc, zArr[0], this.timeLocation, this.fontColor);
                    Logger.i("屏幕尺寸:$screenWidth,$screenHeight")
                    Logger.i("支持屏保:$isSupportWallpaper")
                    Logger.i("屏保开关:$isWallpaperEnable")
                    Logger.i("时间开关:$isTimeEnable")
                    Logger.i("步数开关:$isStepEnable")
                    writeCharacteristic(CommHelper.getWallpaperFontInfo())
                }

                if (byteArray != null && HexUtil.bytes2HexString(byteArray).startsWith("050115")){
                    val b = byteArray[3].toInt() and 255
                    for (i in 4 until 4 + b*2 step 2){
                        val timeWidth = byteArray[i].toInt() and 255
                        val timeHeight = byteArray[i+1].toInt() and 255
                        Logger.i("timeWidth:$timeWidth")
                        Logger.i("timeHeight:$timeHeight")
                    }
                }
            }
        }

    }

    companion object {
        val lock = Object()
    }

    private fun writeCharacteristic(byteArray: ByteArray,e:()->Unit = {}){
        DeviceManager.writeCharacteristic(byteArray,object : CommCallback{
            override fun onSuccess(byteArray: ByteArray?) {
                e()
            }

            override fun onFail(exception: BleException) {

            }
        })
    }

    fun getWallpaperInfo(){
        enableNotify {
            writeCharacteristic(CommHelper.getWallpaperScreenInfo()){


            }
        }

    }

    fun setWallpaper(wallpaperInfoBean: WallpaperInfoBean){
        view?.onUploadStart()
        wallpaperInfoBean.apply {
            //是否开启壁纸
            writeCharacteristic(CommHelper.setWallpaperEnable(enableWallpaper)) {
                //设置壁纸时间
                writeCharacteristic(CommHelper.setWallpaperTimeInfo(isTimeEnable,timeFontSizeX,timeFontSizeY,fontColor,timeLocationX, timeLocationY)){
                    //设置壁纸步数
                    /*writeCharacteristic(CommHelper.setWallpaperStepInfo(isStepEnable, stepFontSizeX, stepFontSizeY, fontColor, stepLocationX, stepLocationY)){
                        if (bitmap!=null){
                            //设置壁纸
                            setWallpaper(bitmap!!)
                        }
                    }*/
                }
            }
        }

    }


    private fun enableNotify(e:()->Unit){
        DeviceManager.getDevice()?.addNotifyCallBack(notifyCallback)
        DeviceManager.getDevice()?.enableNotify(BaseUUID.SERVICE, BaseUUID.NOTIFY_WALLPAPER, BaseUUID.DESC, true, object : CommCallback {
            override fun onSuccess(byteArray: ByteArray?) {
                Logger.i("开启壁纸通知成功")
                e()
            }

            override fun onFail(exception: BleException) {
                Logger.i("开启壁纸通知失败：${exception.message}")
                wallpaperBitmap = null
                uploadStop()
            }
        })
    }

    fun setWallpaper(bitmap: Bitmap) {
        view?.onUploadStart()
        wallpaperBitmap = bitmap
        enableNotify {
            setHighSpeedTransportStatus(true)
        }
    }

    private fun uploadWallpaper(bitmap: Bitmap){
        thread = Thread(Runnable {
            try {
                synchronized(lock) {
                    val wallpaperPackageList = createWallpaperPackage(bitmap)
                    wallpaperPackageList.forEachIndexed { index, it ->
                        it.bytes20.forEach {
                            DeviceManager.writeWallpaperCharacteristic(it)
                        }
                        uploadWallpaperProgress(index,wallpaperPackageList.size)
                        Logger.i("WAIT_WALLPAPER,当前：$index,总共:${wallpaperPackageList.size}")
                        lock.wait()
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                uploadStop()
            }
        })
        thread?.start()

    }

    private fun uploadWallpaperProgress(current:Int,total:Int){
        Handler(Looper.getMainLooper()).post {
            view?.onUploadWallpaperProgress(current,total)
        }
    }

    private fun uploadStop(){
        thread?.interrupt()
        DeviceManager.getDevice()?.removeNotifyCallBack(notifyCallback)
        DeviceManager.getDevice()?.enableNotify(BaseUUID.SERVICE, BaseUUID.NOTIFY_WALLPAPER, BaseUUID.DESC, false)
        setHighSpeedTransportStatus(false)
        Handler(Looper.getMainLooper()).post {
            view?.onUploadStop()
        }
    }

    fun stopUploadWallpaper(){
        thread?.interrupt()
        DeviceManager.getDevice()?.removeNotifyCallBack(notifyCallback)
        DeviceManager.getDevice()?.enableNotify(BaseUUID.SERVICE, BaseUUID.NOTIFY_WALLPAPER, BaseUUID.DESC, false)
        //uploadStop()
    }

    private fun setHighSpeedTransportStatus(open: Boolean) {
        DeviceManager.writeCharacteristic(CommHelper.setHighSpeedTransportStatus(open))
    }




    private fun createWallpaperPackage(bitmap: Bitmap): ArrayList<WallpaperPackage> {
        val convertBitmap = BMP2RGB565bytes(bitmap)
        //一共要发多少个包
        val round = Math.round(convertBitmap.size.toFloat() * 1.0f / 504.0f)
        val arrayList = ArrayList<WallpaperPackage>()
        for (i in convertBitmap.indices step 504) {
            if (i + 504 < convertBitmap.size) {
                val byteArray = ByteArray(512)
                System.arraycopy(convertBitmap, i, byteArray, 8, 504)
                addWallpaperHeader(arrayList.size.toLong(), round.toLong(), byteArray)
                arrayList.add(WallpaperPackage(getBytes(byteArray)));
            } else {
                val bArr2 = ByteArray(convertBitmap.size + 8 - i)
                System.arraycopy(convertBitmap, i, bArr2, 8, bArr2.size - 8)
                addWallpaperHeader(arrayList.size.toLong(), round.toLong(), bArr2)
                arrayList.add(WallpaperPackage(getBytes(bArr2)))
            }
        }
        return arrayList
    }

    //头信息
    private fun addWallpaperHeader(index: Long, round: Long, bArr: ByteArray) {
        var i = 0
        for (i2 in 8 until bArr.size) {
            i += bArr[i2]
        }
        val i3: Long = index or (round or 0 shl 14)
        val length = (bArr.size - 8).toLong() shl 1 or 0 shl 1 or 0 shl 4 or 0
        bArr[0] = 5
        bArr[1] = (i and 255).toByte()
        bArr[2] = (i3 shr 24 and 255).toInt().toByte()
        bArr[3] = (i3 shr 16 and 255).toInt().toByte()
        bArr[4] = (i3 shr 8 and 255).toInt().toByte()
        bArr[5] = (i3 and 255).toInt().toByte()
        bArr[6] = (length shr 8 and 255).toInt().toByte()
        bArr[7] = (length and 255).toInt().toByte()

    }

    private fun getBytes(bArr: ByteArray): List<ByteArray> {
        val arrayList = ArrayList<ByteArray>()
        var i = 0
        while (i < bArr.size) {
            if (i + 20 < bArr.size) {
                val bArr2 = ByteArray(20)
                System.arraycopy(bArr, i, bArr2, 0, bArr2.size)
                arrayList.add(bArr2)
            } else {
                val bArr3 = ByteArray(bArr.size - i)
                System.arraycopy(bArr, i, bArr3, 0, bArr3.size)
                arrayList.add(bArr3)
            }
            i += 20
        }
        return arrayList
    }


    private fun BMP2RGB565bytes(bitmap: Bitmap): ByteArray {
        val createScaledBitmap = Bitmap.createScaledBitmap(
            bitmap.copy(Bitmap.Config.RGB_565, false),
            240,
            240,
            false
        )
        val allocate: ByteBuffer = ByteBuffer.allocate(createScaledBitmap.width * createScaledBitmap.height * 2)
        allocate.order(ByteOrder.BIG_ENDIAN)
        createScaledBitmap.copyPixelsToBuffer(allocate)
        allocate.order(ByteOrder.LITTLE_ENDIAN)
        return converting(allocate.array())
    }

    private fun converting(bArr: ByteArray): ByteArray {
        val length = bArr.size
        val bArr2 = ByteArray(length)
        var i = 0
        while (i < length) {
            val i2 = i + 1
            bArr2[i] = bArr[i2]
            bArr2[i2] = bArr[i]
            i += 2
        }
        return bArr2
    }

}