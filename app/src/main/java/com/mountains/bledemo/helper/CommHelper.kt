package com.mountains.bledemo.helper

import java.util.*

object CommHelper {

    //设置设备时间
     fun setDeviceTime():ByteArray {
        val calendar = Calendar.getInstance();
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
        bArr[3] = buffer[2];
        bArr[4] = buffer[3];
        bArr[5] = month.toByte()
        bArr[6] = day.toByte()
        bArr[7] = hour.toByte()
        bArr[8] = minute.toByte()
        bArr[9] = second.toByte()
        return bArr;
    }


    /**
     * 设置手环信息
     * @param isLiftWristBrightScreen 抬腕亮屏
     * @param isSedentaryReminder 久坐提醒
     * @param isHeartRateAutoCheck 自动检测心率
     */
    fun setDeviceOtherInfo(isLiftWristBrightScreen: Int, isSedentaryReminder: Int, isHeartRateAutoCheck: Int): ByteArray? {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 2
        bArr[2] = 10
        bArr[3] = 0
        bArr[4] = isLiftWristBrightScreen.toByte()
        bArr[5] = isSedentaryReminder.toByte()
        bArr[6] = isHeartRateAutoCheck.toByte()
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


}