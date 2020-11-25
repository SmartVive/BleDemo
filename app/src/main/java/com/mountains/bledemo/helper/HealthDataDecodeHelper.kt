package com.mountains.bledemo.helper

import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.bean.TemperatureBean
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.util.HexUtil
import com.orhanobut.logger.Logger
import org.litepal.LitePal
import org.litepal.extension.find
import org.litepal.extension.saveAll
import java.util.*

class HealthDataDecodeHelper  : IDataDecodeHelper {
    var mHeartRateDataCalendar:Calendar? = null
    var mTemperatureDataCalendar:Calendar? = null
    var heartRates = mutableListOf<HeartRateBean>()
    var temperatures = mutableListOf<TemperatureBean>()


    override fun decode(bArr: ByteArray) {

        if (HexUtil.bytes2HexString(bArr).startsWith("050702")) {
            Logger.i("实时体检数据:解析开始")
            var mHeartRate = bArr[3].toInt()
            val mBloodOxygen = bArr[4].toInt()
            val mBloodDiastolic = bArr[5].toInt()
            val mBloodSystolic = bArr[6].toInt()

            if (mHeartRate == 0 && mBloodOxygen == 0 && mBloodDiastolic == 0 && mBloodSystolic == 0) {
                Logger.w(
                    "实时体检数据:数据异常,心率:%d,血氧:%d,舒张压:%d,收缩压:%d",
                    mHeartRate, mBloodOxygen, mBloodDiastolic, mBloodSystolic
                )
                return
            }
            Logger.i(
                "实时体检数据:心率:%d,血氧:%d,舒张压:%d,收缩压:%d",
                mHeartRate, mBloodOxygen, mBloodDiastolic, mBloodSystolic
            )
            Logger.i("实时体检数据:解析完成")

            if (mHeartRate != 0) {
                if (mHeartRate < 55 || mHeartRate > 200) {
                    Logger.w("实时体检数据:心率数据异常,心率:%d", mHeartRate)
                    mHeartRate = 0
                }
                //HeartRateStorageHelper.asyncSaveRealTimeData(SNBLEHelper.getDeviceMacAddress(), mHeartRate)
            }
            if (mBloodOxygen != 0) {
                //BloodOxygenStorageHelper.asyncSaveRealTimeData(SNBLEHelper.getDeviceMacAddress(), mBloodOxygen)
            }
            if (!(mBloodDiastolic == 0 || mBloodSystolic == 0)) {
                //BloodPressureStorageHelper.asyncSaveRealTimeData(SNBLEHelper.getDeviceMacAddress(), mBloodDiastolic, mBloodSystolic)
            }
        }

        if (HexUtil.bytes2HexString(bArr).startsWith("050707")) {
            val index = bArr[3].toInt()
            if (index <= 11) {
                if (index == 0) {
                    Logger.i("心率大数据:解析开始")
                    mHeartRateDataCalendar = CalendarUtil.getTodayCalendar()
                    heartRates.clear()
                }
                mHeartRateDataCalendar?.let {mHeartRateDataCalendar->
                    if (index != 0 && index % 6 == 0) {
                        mHeartRateDataCalendar.add(Calendar.DAY_OF_MONTH, -2)
                    }
                    for (i in 4 until 20) {
                        var heart = bArr[i].toInt()
                        val timeIndex = CalendarUtil.convertTimeToIndex(mHeartRateDataCalendar, 1)
                        val date = CalendarUtil.format("yyyy-MM-dd HH:mm:ss", mHeartRateDataCalendar)
                        Logger.i("心率大数据:%d,index:%d,时间:%s", heart, timeIndex, date)
                        if (heart < 55 || heart > 200) {
                            Logger.w("心率大数据:数据异常,心率:%d", heart)
                            heart = 0
                        }
                        heartRates.add(HeartRateBean(mHeartRateDataCalendar.timeInMillis,timeIndex, heart))
                        mHeartRateDataCalendar.add(Calendar.MINUTE, 15)
                    }
                }
            }else{
                return
            }
        }

        if(HexUtil.bytes2HexString(bArr).startsWith("0507FD",true)){
            Logger.i("心率大数据:解析完成")
            saveHeartRateData()
        }

        if(HexUtil.bytes2HexString(bArr).startsWith("05070C",true)){
            val index2 = bArr[3].toInt()
            if (index2 <= 11) {
                if (index2 == 0) {
                    Logger.i("体温大数据:解析开始")
                    mTemperatureDataCalendar = CalendarUtil.getTodayCalendar()
                    temperatures.clear()
                }
                mTemperatureDataCalendar?.let {mTemperatureDataCalendar->
                    if (index2 != 0 && index2 % 6 == 0) {
                        mTemperatureDataCalendar.add(Calendar.DAY_OF_MONTH, -2)
                    }
                    for (i in 4 until 20 step 2) {
                        val temp = (((bArr[i]).toInt().shl(8)) or (bArr[i + 1].toInt())) and(65535)
                        val timeIndex = CalendarUtil.convertTimeToIndex(mTemperatureDataCalendar, 1)
                        val date = CalendarUtil.format("yyyy-MM-dd HH:mm:ss", mTemperatureDataCalendar)
                        Logger.i("体温大数据:%d,index:%d,时间:%s", temp, timeIndex, date);
                        temperatures.add(TemperatureBean(mTemperatureDataCalendar.timeInMillis,timeIndex, temp))
                        mTemperatureDataCalendar.add(12, 30)
                    }
                }

            }else{
                return
            }
        }

        if (HexUtil.bytes2HexString(bArr).startsWith( "0507FC",true)) {
            Logger.i("体温大数据:解析完成")
            //TemperatureStorageHelper.asyncSaveData(SNBLEHelper.getDeviceMacAddress(), this.temperatures)
        }

    }

    /**
     * 保存心率大数据
     */
    private fun saveHeartRateData(){
        if(!checkHeartData(heartRates)){
            Logger.w("心率大数据异常，不保存")
            return
        }
        val startTime = heartRates.last().dateTime
        val endTime = heartRates.first().dateTime
        val existsData = LitePal.where("datetime between ? and ?", "$startTime", "$endTime").order("datetime desc")
            .find<HeartRateBean>()



        var existsIndex = existsData.size-1
        var heartRatesIndex = heartRates.size-1
        var updateCount = 0
        while(heartRatesIndex >= 0 && existsIndex >= 0){
            while(existsData[existsIndex].dateTime != heartRates[heartRatesIndex].dateTime){
                existsIndex--;
            }
            //相同的时间，当前数值大于已存在数值时更新
            if(existsIndex >= 0){
                if (heartRates[heartRatesIndex].value > existsData[existsIndex].value){
                    heartRates[heartRatesIndex].update(existsData[existsIndex].id)
                    updateCount++
                }
                heartRates.removeAt(heartRatesIndex)
            }
            existsIndex--
            heartRatesIndex--
        }


        Logger.d("更新心率大数据条目数量：$updateCount")
        Logger.d("新增心率大数据条目数量：${heartRates.size}")
        heartRates.saveAll()
    }


    /**
     * 检查心率数据是否异常
     */
    private fun checkHeartData(list:MutableList<HeartRateBean>):Boolean{
        if(list.isEmpty()){
            return false
        }
        list.sort()
        val tomorrowCalendar = CalendarUtil.getTomorrowCalendar()
        val yesterdayCalendar = CalendarUtil.getYesterdayCalendar()
        //数据只能为昨天00：00点到今天23：59分,超过说明数据异常
        if(list.first().dateTime >= tomorrowCalendar.timeInMillis){
            return false
        }else if (list.last().dateTime < yesterdayCalendar.timeInMillis){
            return false
        }
        return true
    }
}