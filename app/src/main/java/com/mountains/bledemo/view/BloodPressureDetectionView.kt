package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView

interface BloodPressureDetectionView : BaseView {

    fun onStartDetection()

    fun onStopDetection()

    fun onDetectionSuccess(bloodDiastolic:Int,bloodSystolic:Int)
}