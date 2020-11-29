package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView

interface HeartRateDetectionView : BaseView {

    fun onStartDetection()

    fun onStopDetection()

}