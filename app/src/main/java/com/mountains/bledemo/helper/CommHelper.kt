package com.mountains.bledemo.helper

import android.graphics.Bitmap
import androidx.recyclerview.widget.ItemTouchHelper
import com.mountains.bledemo.bean.WallpaperPackage
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


object CommHelper {

    //设置设备时间
     fun setDeviceTime():ByteArray {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 2
        bArr[2] = 1
        val buffer = byteArrayOf((year shr 24).toByte(), (year shr 16).toByte(), (year shr 8).toByte(), year.toByte())
        bArr[3] = buffer[2]
        bArr[4] = buffer[3]
        bArr[5] = month.toByte()
        bArr[6] = day.toByte()
        bArr[7] = hour.toByte()
        bArr[8] = minute.toByte()
        bArr[9] = second.toByte()
        return bArr
    }


    /**
     * 设置手环信息
     * @param isLiftWristBrightScreen 抬腕亮屏
     * @param isLostReminder 丢失提醒
     * @param isHeartRateAutoCheck 自动检测心率
     */
    fun setDeviceOtherInfo(isLiftWristBrightScreen: Boolean, isLostReminder: Boolean, isHeartRateAutoCheck: Boolean): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 2
        bArr[2] = 10
        bArr[3] = 0
        bArr[4] = boolean2Int(isLiftWristBrightScreen).toByte()
        bArr[5] = boolean2Int(isLostReminder).toByte()
        bArr[6] = boolean2Int(isHeartRateAutoCheck).toByte()
        return bArr
    }


    //查找手机，（震动）
    fun findDevice(flag: Boolean): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 5
        bArr[2] = 3
        if (flag) {
            bArr[3] = 1
            return bArr
        }
        bArr[3] = 0
        return bArr
    }


    //心率检测
    fun heartRateDetection(on: Int): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 5
        bArr[2] = 1
        bArr[3] = on.toByte()
        return bArr
    }

    /**
     * 血压检测
     */
    fun bloodPressureDetection(on:Int):ByteArray{
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 5
        bArr[2] = 5
        bArr[3] = on.toByte()
        return bArr
    }

    /**
     * 血氧检测
     */
    fun bloodOxygenDetection(on:Int):ByteArray{
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 5
        bArr[2] = 4
        bArr[3] = on.toByte()
        return bArr
    }

    //获取设备信息
    fun getDeviceInfo(): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 1
        bArr[2] = 2
        return bArr
    }


    /**
     * 心率历史记录
     */
    fun getHistoryHeartRateData(): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 7
        bArr[2] = 7
        return bArr
    }

    /**
     * 步数历史记录
     */
    fun getHistorySportData(): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 7
        bArr[2] = 3
        return bArr
    }

    /**
     * 睡眠历史记录
     */
    fun getHistorySleepData(): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 7
        bArr[2] = 4
        return bArr
    }


    /**
     * 实时步数
     */
    fun getRealTimeSportData() : ByteArray{
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 7
        bArr[2] = 1
        return bArr
    }

    /**
     * 实时心率
     */
    fun getRealTimeHeartRateData() : ByteArray{
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 7
        bArr[2] = 2
        return bArr
    }


    /**
     * 推送信息
     */
    fun getPushMessageData(title: String, content: String): List<ByteArray> {
        val shortTitle = cutShortString(title)
        val shortContent = cutShortString(content)
        val byteSet = LinkedList<ByteArray>()
        val buffer = getMessage(shortTitle, shortContent)
        //当前包中信息内容所占多少字节
        var j = 0
        val str = ByteArray(15)
        val length = buffer.size
        for (i in 0 until length) {
            str[j] = buffer[i]
            j++
            //当包长度达到20（头信息占5字节） 或 信息内容已读取完毕 时添加到集合
            if (j == 15 || i == length - 1) {
                val bytes = ByteArray(20)
                bytes[0] = 5
                bytes[1] = 4
                bytes[2] = 3
                //当前为第几个包
                bytes[3] = byteSet.size.toByte()
                bytes[4] = j.toByte()
                for (l in str.indices) {
                    bytes[5 + l] = str[l]
                    str[l] = 0
                }
                byteSet.add(bytes)
                j = 0
            }
        }
        byteSet.add(endByte(3, 3))
        return byteSet
    }

    /**
     * 设置闹钟
     * @param index 第几个闹钟
     * @param isOpen 是否开启
     * @param repeat 重复
     * @param hour 小时
     * @param minute 分钟
     * @param i6
     */
    fun setAlarmClock(index: Int, isOpen: Int, repeat: Int, hour: Int, minute: Int, i6: Int): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 2
        bArr[2] = 4
        bArr[3] = index.toByte()
        bArr[4] = isOpen.toByte()
        bArr[5] = repeat.toByte()
        bArr[6] = hour.toByte()
        bArr[7] = minute.toByte()
        bArr[8] = i6.toByte()
        return bArr
    }

    /**
     * 获取闹钟
     */
    fun getAlarmClock():ByteArray{
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 1
        bArr[2] = 4
        return bArr
    }

    /**
     * 设置高速传输状态
     */
    fun setHighSpeedTransportStatus(z: Boolean): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 2
        bArr[2] = 17
        val i = if (z) {
             1
        }else{
            0
        }
        bArr[3] = i.toByte()
        return bArr
    }

    /**
     * 获取设备壁纸信息
     */
    fun getWallpaperScreenInfo(): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 1
        bArr[2] = 20
        return bArr
    }

    /**
     * 获取设备壁纸字体信息
     */
    fun getWallpaperFontInfo(): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 1
        bArr[2] = 21
        return bArr
    }


    /**
     * 开启关闭壁纸
     */
    fun setWallpaperEnable(z: Boolean): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 2
        bArr[2] = 18
        val i = if (z) {
            1
        }else{
            2
        }
        bArr[3] = i.toByte()
        return bArr
    }

    /**
     * 壁纸信息
     */
    fun setWallpaperTimeInfo(
        isTimeEnable: Boolean,
        timeFontSizeX: Int,
        timeFontSizeY: Int,
        fontColor: Int,
        timeLocationX: Int,
        timeLocationY: Int
    ): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 2
        bArr[2] = 19
        bArr[3] = (if (isTimeEnable) 1 else 2).toByte()
        bArr[4] = 1
        bArr[5] = timeFontSizeX.toByte()
        bArr[6] = timeFontSizeY.toByte()
        val RGB888ToRGB565: Int = RGB888ToRGB565(fontColor)
        bArr[7] = (RGB888ToRGB565 shr 8).toByte()
        bArr[8] = (RGB888ToRGB565 and 255).toByte()
        bArr[9] = (timeLocationX shr 8).toByte()
        bArr[10] = (timeLocationX and 255).toByte()
        bArr[11] = (timeLocationY shr 8).toByte()
        bArr[12] = (timeLocationY and 255).toByte()
        return bArr
    }

    fun setWallpaperStepInfo(
        isStepEnable: Boolean,
        stepFontSizeX: Int,
        stepFontSizeY: Int,
        fontColor: Int,
        stepLocationX: Int,
        stepLocationY: Int
    ): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 2
        bArr[2] = 20
        bArr[3] = (if (isStepEnable) 1 else 2).toByte()
        bArr[4] = 1
        bArr[5] = stepFontSizeX.toByte()
        bArr[6] = stepFontSizeY.toByte()
        val RGB888ToRGB565: Int = RGB888ToRGB565(fontColor)
        bArr[7] = (RGB888ToRGB565 shr 8).toByte()
        bArr[8] = (RGB888ToRGB565 and 255).toByte()
        bArr[9] = (stepLocationX shr 8).toByte()
        bArr[10] = (stepLocationX and 255).toByte()
        bArr[11] = (stepLocationY shr 8).toByte()
        bArr[12] = (stepLocationY and 255).toByte()
        return bArr
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


    fun RGB888ToRGB565(i: Int): Int {
        return (i and 255 shr 3 shl 0) + (16711680 and i shr 19 shl 11) + (65280 and i shr 10 shl 5)
    }


    private fun cutShortString(str:String):String {
        if (str.length > 200) {
            return str.substring(0, ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION)
        }
        return str
    }

     private fun getMessage(name:String , content:String ):ByteArray {
        var index = 0
        val n = getUnicode(name)
        val c = getUnicode(content)
        val nLength = n.length
        val cLength = c.length
        val bytes = ByteArray(((nLength + cLength) / 2) + 2)
         for(i in 0 until nLength step 2){
             bytes[index++] = Integer.parseInt(n.substring(i,i+2),16).toByte()
         }

         //标题与内容中间隔两个-1
        bytes[index++] = -1
        bytes[index++] = -1

         for(i in 0 until cLength step 2){
             bytes[index++] = Integer.parseInt(c.substring(i,i+2),16).toByte()
         }
        return bytes
    }

    private fun getUnicode(src:String):String {
        var str = ""
        for (i in src.indices) {
            val char = src[i]
            str += String.format("%04X", char.toInt())
        }
        return str
    }

    private fun endByte(functionKey:Int, appType:Int):ByteArray {
        return byteArrayOf(5, 4, functionKey.toByte(), -1,  appType.toByte())
    }

    /**
     * boolean转换为int
     */
    private fun boolean2Int(boolean: Boolean):Int{
        if (boolean){
            return 1
        }else{
            return 0
        }
    }
}