package com.mountains.bledemo.presenter

import android.graphics.Bitmap
import com.mountains.bledemo.base.BasePresenter
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
import java.util.*
import kotlin.collections.ArrayList


class WallpaperPresenter : BasePresenter<WallpaperView>() {
    companion object {
        val lock = Object()
    }

    fun setWallpaper(bitmap: Bitmap) {


        DeviceManager.getDevice()?.addNotifyCallBack(object : NotifyCallback {
            override fun onNotify(uuid: String, byteArray: ByteArray?) {
                if (uuid == BaseUUID.NOTIFY_WALLPAPER) {
                    Logger.i("NOTIFY_WALLPAPER")
                    synchronized(lock) {
                        lock.notify()
                    }
                }else if (uuid == BaseUUID.NOTIFY){
                    if (HexUtil.bytes2HexString(byteArray).startsWith("05021101")){
                        Logger.i("开启高速模式,开始上传壁纸")
                        uploadWallpaper(bitmap)
                    }
                }
            }

        })

        DeviceManager.getDevice()
            ?.enableNotify(BaseUUID.SERVICE, BaseUUID.NOTIFY_WALLPAPER, BaseUUID.DESC, true, object : CommCallback {
                override fun onSuccess(byteArray: ByteArray?) {
                    Logger.i("开启壁纸通知成功")
                }

                override fun onFail(exception: BleException) {
                    Logger.i("开启壁纸通知失败：${exception.message}")
                }

            })

        setHighSpeedTransportStatus(true)


    }

    private fun uploadWallpaper(bitmap: Bitmap){
        Thread(Runnable {
            Thread.sleep(10*1000)
            synchronized(lock) {
                val wallpaperPackageList = createWallpaperPackage(bitmap)
                wallpaperPackageList.forEachIndexed { index, it ->
                    it.bytes20.forEach {
                        DeviceManager.writeWallpaperCharacteristic(it)
                    }
                    Logger.i("WAIT_WALLPAPER,当前：$index,总共:${wallpaperPackageList.size}")
                    lock.wait()
                }
                setHighSpeedTransportStatus(false)
            }
        }).start()

    }

    fun setHighSpeedTransportStatus(open: Boolean) {
        DeviceManager.writeCharacteristic(CommHelper.setHighSpeedTransportStatus(open))
    }

    fun createWallpaperPackage(bitmap: Bitmap): ArrayList<WallpaperPackage> {
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