package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView
import com.mountains.bledemo.bean.AlarmClockBean

interface AlarmClockView : BaseView {
    fun onAlarmClockList(list: List<AlarmClockBean>)

    fun onSwitchAlarmClockSuccess(isOpen:Boolean,distanceNextAlarmTime:String)

    fun onDeleteAlarmClockSuccess(position:Int)
}