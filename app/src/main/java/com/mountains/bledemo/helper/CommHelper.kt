package com.mountains.bledemo.helper

import androidx.recyclerview.widget.ItemTouchHelper
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

    fun getAlarmClock():ByteArray{
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 1
        bArr[2] = 4
        return bArr
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