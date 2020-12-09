package com.mountains.bledemo.ui.activity

import android.os.Bundle
import android.view.View
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.presenter.AlarmClockPresenter
import com.mountains.bledemo.view.AlarmClockView
import kotlinx.android.synthetic.main.activity_alarm_clock.*


class AlarmClockActivity : BaseActivity<AlarmClockPresenter>(),AlarmClockView {

    override fun createPresenter(): AlarmClockPresenter {
        return AlarmClockPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_clock)
        initView()
    }

    private fun initView(){
        timePicker.setIs24HourView(true)

        val hour = timePicker.currentHour
        val minute = timePicker.currentMinute

        var repeat = 0
        val repeatArray = booleanArrayOf(false,false,false,false,false,false,false)
        for (i in repeatArray.indices){
            if (repeatArray[i]){
                repeat = repeat or 1 shl i
            }
        }
        if (repeat == 0){
            repeat = 128
        }
        DeviceManager.writeCharacteristic(CommHelper.setAlarmClock(2,1,repeat,0,48,0))
    }

}