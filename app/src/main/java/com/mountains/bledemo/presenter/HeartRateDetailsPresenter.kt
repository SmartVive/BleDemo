package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.view.HeartRateDetailsView
import org.litepal.LitePal
import org.litepal.extension.find

class HeartRateDetailsPresenter : BasePresenter<HeartRateDetailsView>() {

    fun getHeartRate(startTime:Long,endTime:Long){
        val heartRateData = LitePal.where("datetime between ? and ? and value > ?", "$startTime", "$endTime","0").order("datetime desc")
            .find<HeartRateBean>()

        if (heartRateData.isEmpty()){
            view?.onHeartRateData(heartRateData,"--", "--", "--")
            return
        }


        val minHeartRate = heartRateData.maxBy { it.value }?.value
        val maxHeartRate = heartRateData.minBy { it.value }?.value
        val avgHeartRate = heartRateData.sumBy { it.value }/heartRateData.size

        val minHeartRateString = "$minHeartRate bpm"
        val maxHeartRateString = "$maxHeartRate bpm"
        val avgHeartRateString = "$avgHeartRate bpm"


        view?.onHeartRateData(heartRateData,avgHeartRateString, maxHeartRateString, minHeartRateString)
    }




}