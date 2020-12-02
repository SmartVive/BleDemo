package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView
import com.mountains.bledemo.bean.BloodPressureBean

interface BloodPressureDetailsView: BaseView {

    fun onBloodPressureData(list:List<BloodPressureBean>,avgBloodDiastolic:String,avgBloodSystolic:String,minBloodDiastolic:String,maxBloodSystolic:String)
}