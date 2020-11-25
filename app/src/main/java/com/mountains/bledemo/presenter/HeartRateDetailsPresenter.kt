package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.view.HeartRateDetailsView
import org.litepal.LitePal
import org.litepal.extension.find

class HeartRateDetailsPresenter : BasePresenter<HeartRateDetailsView>() {

    fun getHeartRate(startTime:Long,endTime:Long){
        val heartRate = LitePal.where("datetime between ? and ? and value > ?", "$startTime", "$endTime","0").order("datetime desc")
            .find<HeartRateBean>()

        if (heartRate.isEmpty()){
            view?.onHeartRateData(heartRate,0, 0, 0)
            return
        }

        var minHeartRate = Integer.MAX_VALUE
        var maxHeartRate = Integer.MIN_VALUE
        var avgHeartRate = 0
        var sumHeartRate = 0
        for (item in heartRate){
            minHeartRate = Math.min(minHeartRate,item.value)
            maxHeartRate = Math.max(maxHeartRate,item.value)
            sumHeartRate+=item.value
        }
        avgHeartRate = sumHeartRate/heartRate.size



        view?.onHeartRateData(heartRate,avgHeartRate, maxHeartRate, minHeartRate)
    }




}