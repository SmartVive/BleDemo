package com.mountains.bledemo.presenter

import com.mountains.bledemo.view.MainView
import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.BloodOxygenBean
import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.util.CalendarUtil
import org.litepal.LitePal
import org.litepal.extension.find
import java.util.*
import kotlin.Comparator

class MainPresenter : BasePresenter<MainView>() {

    fun getHeartRateData(){
        val todayBeginTime = CalendarUtil.getTodayBeginCalendar().timeInMillis
        val todayEndTime = CalendarUtil.getTodayEndCalendar().timeInMillis
        val heartRateList = LitePal.where("datetime between ? and ? and value > ?", "$todayBeginTime", "$todayEndTime","0").order("datetime desc")
            .find<HeartRateBean>()

        if(heartRateList.isEmpty()){

        }else{
            val maxHeartRate = heartRateList.maxBy { it.value }?.value
            val minHeartRate = heartRateList.minBy { it.value }?.value

            val time = CalendarUtil.format("HH:mm", heartRateList.first().dateTime)

            val valueContent = "$minHeartRate - $maxHeartRate bpm"
            val timeContent = "最后一次:$time"
            view?.onHeartRateData(valueContent,timeContent)
        }

    }

    fun getBloodOxygenData(){
        val todayBeginTime = CalendarUtil.getTodayBeginCalendar().timeInMillis
        val todayEndTime = CalendarUtil.getTodayEndCalendar().timeInMillis
        val bloodOxygenList = LitePal.where("datetime between ? and ? and value > ?", "$todayBeginTime", "$todayEndTime","0").order("datetime desc")
            .find<BloodOxygenBean>()

        if (bloodOxygenList.isEmpty()){

        }else{
            val maxBloodOxygen = bloodOxygenList.maxBy { it.value }?.value
            val minBloodOxygen = bloodOxygenList.minBy { it.value }?.value
            val time = CalendarUtil.format("HH:mm", bloodOxygenList.first().dateTime)
            val valueContent = "$minBloodOxygen - $maxBloodOxygen %"
            val timeContent = "最后一次:$time"
            view?.onBloodOxygenData(valueContent,timeContent)
        }
    }
}