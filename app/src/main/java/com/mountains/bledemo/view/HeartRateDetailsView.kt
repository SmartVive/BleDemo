package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView
import com.mountains.bledemo.bean.HeartRateBean

interface HeartRateDetailsView : BaseView {

    fun onHeartRateData(list: List<HeartRateBean>,avgHeartRate:Int,maxHeartRate:Int,minHeartRate:Int)
}