package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView

interface MainView : BaseView {

    fun onHeartRateData(valueContent: String,timeContent:String)

    fun onBloodOxygenData(valueContent: String,timeContent: String)
}