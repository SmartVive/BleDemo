package com.mountains.bledemo.presenter

import com.mountains.bledemo.view.MainView
import com.mountains.bledemo.base.BasePresenter
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
            var maxHeartRate = Integer.MIN_VALUE
            var minHeartRate = Integer.MAX_VALUE
            heartRateList.forEach {
                maxHeartRate = Math.max(maxHeartRate,it.value)
                minHeartRate = Math.min(minHeartRate,it.value)
            }
            val time = CalendarUtil.format("HH:mm", heartRateList.first().dateTime)

            val valueContent = "$minHeartRate - $maxHeartRate bpm"
            val timeContent = "最后一次:$time"
            view?.onHeartRateData(valueContent,timeContent)
        }

    }
}