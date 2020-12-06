package com.mountains.bledemo.ui.activity

import android.os.Bundle
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.SleepBean
import com.mountains.bledemo.presenter.SleepDetailsPresenter
import com.mountains.bledemo.ui.fragment.CalendarDialogFragment
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.SleepDetailsView
import com.mountains.bledemo.weiget.SelectDateView
import kotlinx.android.synthetic.main.activity_sleep_details.*
import kotlinx.android.synthetic.main.activity_sleep_details.tvDeepTime
import java.text.SimpleDateFormat
import java.util.*

class SleepDetailsActivity : BaseActivity<SleepDetailsPresenter>(),SleepDetailsView {
    var currentSelectTime = System.currentTimeMillis()

    override fun createPresenter(): SleepDetailsPresenter {
        return SleepDetailsPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_details)
        initView()
        initData()
    }

    private fun initView(){
        titleBar.leftView.setOnClickListener {
            finish()
        }
        titleBar.rightView.setOnClickListener {
            CalendarDialogFragment().show(supportFragmentManager,javaClass.name,currentSelectTime,object : CalendarDialogFragment.OnCalendarSelectListener{
                override fun onCalendarSelect(calendarTime: Long) {
                    currentSelectTime = calendarTime
                    selectDateView.setDate(currentSelectTime)
                    initData()
                }

            })
        }

        selectDateView.setDate(currentSelectTime)
        selectDateView.setOnDateChangeListener(object : SelectDateView.OnDateChangeListener{
            override fun onDateChange(date: Long) {
                currentSelectTime = date
                initData()
            }
        })

    }

    private fun initData(){
        val calendar = CalendarUtil.getCalendar(currentSelectTime)
        calendar.set(Calendar.MINUTE,0)
        calendar.set(Calendar.SECOND,0)
        calendar.set(Calendar.MILLISECOND,0)
        calendar.set(Calendar.HOUR_OF_DAY,12)
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH,-1)
        calendar.set(Calendar.HOUR_OF_DAY,21)
        val beginTime = calendar.timeInMillis
        presenter.getSleepData(beginTime,endTime)
    }


    override fun onSleepData(
        sleepData: List<SleepBean.SleepData>,
        deepDuration: String,
        lightDuration: String,
        soberDuration: String
    ) {
        val popupDate = SimpleDateFormat("MM.dd", Locale.getDefault()).format(currentSelectTime)
        sleepHistogramView.loadData(sleepData,popupDate)
        tvDeepTime.text = deepDuration
        tvLightTime.text = lightDuration
        tvSoberTime.text =soberDuration
    }


}