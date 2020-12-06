package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView
import com.mountains.bledemo.bean.BloodOxygenBean

interface BloodOxygenDetailsView : BaseView {

    fun onBloodOxygenData(bloodOxygenData:List<BloodOxygenBean>,avgBloodOxygen:String,minBloodOxygen:String,maxBloodOxygen:String)
}