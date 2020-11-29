package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView

interface BloodOxygenDetectionView : BaseView {

    fun onStartDetection()

    fun onStopDetection()
}