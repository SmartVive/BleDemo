package com.mountains.bledemo.ui.activity

import android.os.Bundle
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.presenter.AlarmClockAddPresenter
import com.mountains.bledemo.view.AlarmClockAddView
import kotlinx.android.synthetic.main.activity_alarm_clock_add.*


class AlarmClockAddActivity : BaseActivity<AlarmClockAddPresenter>(), AlarmClockAddView {

    override fun createPresenter(): AlarmClockAddPresenter {
        return AlarmClockAddPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_clock_add)
        initView()
    }

    private fun initView() {
        timePicker.setIs24HourView(true)

        titleBar.leftView.setOnClickListener {
            finish()
        }

        titleBar.rightView.setOnClickListener {
            addAlarmClock()
        }

        DeviceManager.writeCharacteristic(CommHelper.getAlarmClock())
    }

    private fun addAlarmClock() {
        val hour = timePicker.currentHour
        val minute = timePicker.currentMinute
        val repeatArray = mutableListOf(
            cbSunday.isChecked,
            cbMonday.isChecked,
            cbTuesday.isChecked,
            cbWednesday.isChecked,
            cbThursday.isChecked,
            cbFriday.isChecked,
            cbSaturday.isChecked
        )
        presenter.addAlarmClock(hour,minute, repeatArray)
    }

    override fun onAddAlarmClockSuccess() {
        showToast("添加成功")
        finish()
    }
}