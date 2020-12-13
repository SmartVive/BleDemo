package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.view.HeartRateDetailsView
import org.litepal.LitePal
import org.litepal.extension.find

class HeartRateDetailsPresenter : BasePresenter<HeartRateDetailsView>() {

    fun getHeartRate(beginTime:Long, endTime:Long){
        val mac = DeviceManager.getDevice()?.getMac()
        if (mac == null){
            view?.onHeartRateData(emptyList(),"--", "--", "--")
            return
        }

        val heartRateData = LitePal.where("mac = ? and datetime between ? and ? and value > ?", mac,"$beginTime", "$endTime","0").order("datetime desc")
            .find<HeartRateBean>()

        if (heartRateData.isEmpty()){
            view?.onHeartRateData(emptyList(),"--", "--", "--")
            return
        }


        val minHeartRate = heartRateData.minBy { it.value }?.value
        val maxHeartRate = heartRateData.maxBy { it.value }?.value
        val avgHeartRate = heartRateData.sumBy { it.value }/heartRateData.size

        val minHeartRateString = "$minHeartRate bpm"
        val maxHeartRateString = "$maxHeartRate bpm"
        val avgHeartRateString = "$avgHeartRate bpm"


        view?.onHeartRateData(heartRateData,avgHeartRateString,minHeartRateString,maxHeartRateString)
    }




}