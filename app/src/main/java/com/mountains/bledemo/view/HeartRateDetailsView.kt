package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView
import com.mountains.bledemo.bean.HeartRateBean

interface HeartRateDetailsView : BaseView {

    fun onHeartRateData(list: List<HeartRateBean>,avgHeartRate:String,maxHeartRate:String,minHeartRate:String)
}