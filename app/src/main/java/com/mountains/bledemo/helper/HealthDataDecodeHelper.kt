package com.mountains.bledemo.helper

import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.bean.TemperatureBean
import com.mountains.bledemo.event.BloodOxygenDetectionEvent
import com.mountains.bledemo.event.BloodPressureDetectionEvent
import com.mountains.bledemo.event.DataUpdateEvent
import com.mountains.bledemo.event.HeartRateDetectionEvent
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.util.HexUtil
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus
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
            var mHeartRate = bArr[3].toInt() and 255
            val mBloodOxygen = bArr[4].toInt() and 255
            val mBloodDiastolic = bArr[5].toInt() and 255
            val mBloodSystolic = bArr[6].toInt() and 255

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
                }else{
                    //saveHeartRateData(mHeartRate)
                    EventBus.getDefault().post(HeartRateDetectionEvent(mHeartRate))
                }

            }
            if (mBloodOxygen != 0) {
                //BloodOxygenStorageHelper.asyncSaveRealTimeData(SNBLEHelper.getDeviceMacAddress(), mBloodOxygen)
                EventBus.getDefault().post(BloodOxygenDetectionEvent(mBloodOxygen))
            }
            if (!(mBloodDiastolic == 0 || mBloodSystolic == 0)) {
                //BloodPressureStorageHelper.asyncSaveRealTimeData(SNBLEHelper.getDeviceMacAddress(), mBloodDiastolic, mBloodSystolic)
                EventBus.getDefault().post(BloodPressureDetectionEvent(mBloodDiastolic,mBloodSystolic))
            }
        }

        if (HexUtil.bytes2HexString(bArr).startsWith("050707")) {
            val index = bArr[3].toInt() and 255
            if (index <= 11) {
                if (index == 0) {
                    Logger.i("心率大数据:解析开始")
                    mHeartRateDataCalendar = CalendarUtil.getTodayBeginCalendar()
                    heartRates.clear()
                }
                mHeartRateDataCalendar?.let {mHeartRateDataCalendar->
                    if (index != 0 && index % 6 == 0) {
                        mHeartRateDataCalendar.add(Calendar.DAY_OF_MONTH, -2)
                    }
                    for (i in 4 until 20) {
                        var heart = bArr[i].toInt() and 255
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
            if(heartRates.isNotEmpty()){
                saveHeartRatesData(heartRates)
                EventBus.getDefault().post(DataUpdateEvent(DataUpdateEvent.HEART_RATE_UPDATE_TYPE))
            }
        }

        if(HexUtil.bytes2HexString(bArr).startsWith("05070C",true)){
            val index2 = bArr[3].toInt() and 255
            if (index2 <= 11) {
                if (index2 == 0) {
                    Logger.i("体温大数据:解析开始")
                    mTemperatureDataCalendar = CalendarUtil.getTodayBeginCalendar()
                    temperatures.clear()
                }
                mTemperatureDataCalendar?.let {mTemperatureDataCalendar->
                    if (index2 != 0 && index2 % 6 == 0) {
                        mTemperatureDataCalendar.add(Calendar.DAY_OF_MONTH, -2)
                    }
                    for (i in 4 until 20 step 2) {
                        val temp = ((((bArr[i]).toInt() and 255).shl(8)) or (bArr[i + 1].toInt()and 255)) and 65535
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
    private fun saveHeartRatesData(newDataList:MutableList<HeartRateBean>){
        if(!checkHeartData(newDataList)){
            Logger.w("心率大数据异常，不保存")
            return
        }
        //根据时间戳获取当天00:00:00点日历
        val startCalendar = CalendarUtil.getBeginCalendar(newDataList.last().dateTime)
        //根据时间戳获取当天23:59:59点日历
        val endCalendar = CalendarUtil.getEndCalendar(newDataList.first().dateTime)
        val startTime = startCalendar.timeInMillis
        val endTime = endCalendar.timeInMillis

        val oldDataList = LitePal.where("datetime between ? and ?", "$startTime", "$endTime").order("datetime desc")
            .find<HeartRateBean>()

        var updateCount = 0
        for (i in newDataList.size - 1 downTo 0) {
            for (j in oldDataList.size - 1 downTo 0) {
                val newData = newDataList[i]
                val oldData = oldDataList[j]
                //当时间一致时更新
                if(newData.dateTime == oldData.dateTime){
                    updateCount++
                    newData.update(oldData.id)
                    newDataList.removeAt(i)
                    break
                }
            }
        }


        Logger.d("更新心率大数据条目数量：$updateCount")
        Logger.d("新增心率大数据条目数量：${newDataList.size}")
        newDataList.saveAll()
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