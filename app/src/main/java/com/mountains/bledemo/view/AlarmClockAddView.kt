package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView

interface AlarmClockAddView : BaseView {
    fun onAddAlarmClockSuccess(distanceNextAlarmTime:String)
}