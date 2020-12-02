package com.mountains.bledemo.presenter

import com.mountains.bledemo.view.MainView
import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.BloodOxygenBean
import com.mountains.bledemo.bean.BloodPressureBean
import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.bean.SleepBean
import com.mountains.bledemo.util.CalendarUtil
import org.litepal.LitePal
import org.litepal.extension.find
import java.util.*
import kotlin.Comparator

class MainPresenter : BasePresenter<MainView>() {

    fun getHeartRateData() {
        val todayBeginTime = CalendarUtil.getTodayBeginCalendar().timeInMillis
        val todayEndTime = CalendarUtil.getTodayEndCalendar().timeInMillis
        val heartRateList =
            LitePal.where("datetime between ? and ? and value > ?", "$todayBeginTime", "$todayEndTime", "0")
                .order("datetime desc")
                .find<HeartRateBean>()

        if (heartRateList.isEmpty()) {
            view?.onHeartRateData("0 - 0 bpm", "最后一次:暂无数据")
        } else {
            val maxHeartRate = heartRateList.maxBy { it.value }?.value
            val minHeartRate = heartRateList.minBy { it.value }?.value

            val time = CalendarUtil.format("HH:mm", heartRateList.first().dateTime)

            val valueContent = "$minHeartRate - $maxHeartRate bpm"
            val timeContent = "最后一次:$time"
            view?.onHeartRateData(valueContent, timeContent)
        }

    }

    fun getBloodOxygenData() {
        val todayBeginTime = CalendarUtil.getTodayBeginCalendar().timeInMillis
        val todayEndTime = CalendarUtil.getTodayEndCalendar().timeInMillis
        val bloodOxygenList =
            LitePal.where("datetime between ? and ? and value > ?", "$todayBeginTime", "$todayEndTime", "0")
                .order("datetime desc")
                .find<BloodOxygenBean>()

        if (bloodOxygenList.isEmpty()) {
            view?.onBloodOxygenData("0 - 0 %", "最后一次:暂无数据")
        } else {
            val maxBloodOxygen = bloodOxygenList.maxBy { it.value }?.value
            val minBloodOxygen = bloodOxygenList.minBy { it.value }?.value
            val time = CalendarUtil.format("HH:mm", bloodOxygenList.first().dateTime)
            val valueContent = "$minBloodOxygen - $maxBloodOxygen %"
            val timeContent = "最后一次:$time"
            view?.onBloodOxygenData(valueContent, timeContent)
        }
    }

    fun getSleepData() {
        val yesterdayCalendar = CalendarUtil.getYesterdayCalendar()
        yesterdayCalendar.set(Calendar.HOUR_OF_DAY, 21)
        val todayBeginCalendar = CalendarUtil.getTodayBeginCalendar()
        todayBeginCalendar.set(Calendar.HOUR_OF_DAY, 12)

        val beginTime = yesterdayCalendar.timeInMillis
        val endTime = todayBeginCalendar.timeInMillis

        val sleepData = LitePal.where("beginDateTime >= ? and endDateTime <= ?", "$beginTime", "$endTime")
            .order("beginDateTime desc").find<SleepBean>(true)

        if (sleepData.isEmpty()) {
            view?.onSleepData("0h 0min", "最后一次:暂无数据")
        } else {
            val time = CalendarUtil.format("HH:mm", sleepData.first().beginDateTime)
            val isToday = CalendarUtil.isToday(sleepData.first().beginDateTime)
            val sleepTotalMin = sleepData.first().deep + sleepData.first().light + sleepData.first().sober
            val min = sleepTotalMin % 60
            val hour = sleepTotalMin / 60
            val valueContent = "${hour}h ${min}min"
            val timeContent = if (isToday) {
                "最后一次:$time"
            } else {
                "最后一次:昨天 $time"
            }
            view?.onSleepData(valueContent, timeContent)
        }
    }

    fun getBloodPressureData() {
        val todayBeginTime = CalendarUtil.getTodayBeginCalendar().timeInMillis
        val todayEndTime = CalendarUtil.getTodayEndCalendar().timeInMillis
        val bloodPressureData = LitePal.where("datetime between ? and ?", "$todayBeginTime", "$todayEndTime").order("datetime desc")
                .find<BloodPressureBean>()
        if (bloodPressureData.isEmpty()){
            view?.onBloodPressureData("0 / 0 mmHg","最后一次:暂无数据")
        }else{
            val time = CalendarUtil.format("HH:mm", bloodPressureData.first().dateTime)
            val minBloodDiastolic = bloodPressureData.minBy { it.bloodDiastolic }?.bloodDiastolic
            val maxBloodDiastolic = bloodPressureData.maxBy { it.bloodSystolic }?.bloodSystolic

            val valueContent = "$minBloodDiastolic / $maxBloodDiastolic mmHg"
            val timeContent =  "最后一次:$time"
            view?.onBloodPressureData(valueContent, timeContent)
        }
    }
}