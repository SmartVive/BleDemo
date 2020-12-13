package com.mountains.bledemo.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.AlarmClockBean
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.presenter.AlarmClockAddPresenter
import com.mountains.bledemo.view.AlarmClockAddView
import kotlinx.android.synthetic.main.activity_alarm_clock_add.*


class AlarmClockAddActivity : BaseActivity<AlarmClockAddPresenter>(), AlarmClockAddView {
    private val alarmClockBean by lazy { intent.getParcelableExtra<AlarmClockBean>(ALARM_CLOCK_BEAN) }

    companion object{
        const val ALARM_CLOCK_BEAN = "alarmClockBean"

        fun actionStart(activity: Activity,alarmClockBean: AlarmClockBean,requestCode:Int){
            val intent = Intent(activity, AlarmClockAddActivity::class.java)
            intent.putExtra(ALARM_CLOCK_BEAN,alarmClockBean)
            activity.startActivityForResult(intent,requestCode)
        }

        fun actionStart(activity: Activity,requestCode:Int){
            val intent = Intent(activity, AlarmClockAddActivity::class.java)
            activity.startActivityForResult(intent,requestCode)
        }
    }

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

        val cbs = arrayOf(cbSunday,cbMonday,cbTuesday,cbWednesday,cbThursday,cbFriday,cbSaturday)
        alarmClockBean?.run {
            timePicker.currentHour = hour
            timePicker.currentMinute = minute
            repeat.forEachIndexed { index, i ->
                cbs[index].isChecked = i==1
            }
        }


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
        if (alarmClockBean != null){
            presenter.updateAlarmClock(alarmClockBean!!.id,alarmClockBean!!.index,hour,minute, repeatArray)
        }else{
            presenter.addAlarmClock(hour,minute, repeatArray)
        }

    }

    override fun onAddAlarmClockSuccess(distanceNextAlarmTime: String) {
        showToast("距离下次闹钟还剩$distanceNextAlarmTime")
        setResult(Activity.RESULT_OK)
        finish()
    }
}