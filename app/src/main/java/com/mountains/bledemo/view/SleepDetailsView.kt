package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView
import com.mountains.bledemo.bean.SleepBean

interface SleepDetailsView : BaseView {

    fun onSleepData(sleepData:List<SleepBean.SleepData>,deepDuration:String,lightDuration:String,soberDuration:String)
}