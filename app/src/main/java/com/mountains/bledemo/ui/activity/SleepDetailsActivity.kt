package com.mountains.bledemo.ui.activity

import android.os.Bundle
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.SleepBean
import com.mountains.bledemo.presenter.SleepDetailsPresenter
import com.mountains.bledemo.ui.fragment.CalendarDialogFragment
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.SleepDetailsView
import kotlinx.android.synthetic.main.activity_sleep_details.*
import kotlinx.android.synthetic.main.activity_sleep_details.tvDeepTime
import java.text.SimpleDateFormat
import java.util.*

class SleepDetailsActivity : BaseActivity<SleepDetailsPresenter>(),SleepDetailsView {
    private var selectTimeMillis:Long? = null

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
            CalendarDialogFragment().show(supportFragmentManager,javaClass.simpleName,selectTimeMillis,object : CalendarDialogFragment.OnCalendarSelectListener{
                override fun onCalendarSelect(calendarTime: Long) {
                    selectTimeMillis = calendarTime
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = calendarTime
                    calendar.add(Calendar.DAY_OF_YEAR,-1)
                    calendar.set(Calendar.HOUR_OF_DAY,21)
                    val beginTime = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_YEAR,1)
                    calendar.set(Calendar.HOUR_OF_DAY,12)
                    val endTime = calendar.timeInMillis
                    presenter.getSleepData(beginTime,endTime)
                }

            })
        }
    }

    private fun initData(){
        val yesterdayCalendar = CalendarUtil.getYesterdayCalendar()
        yesterdayCalendar.set(Calendar.HOUR_OF_DAY,21)
        val todayBeginCalendar = CalendarUtil.getTodayBeginCalendar()
        todayBeginCalendar.set(Calendar.HOUR_OF_DAY,12)

        val beginTime = yesterdayCalendar.timeInMillis
        val endTime = todayBeginCalendar.timeInMillis
        presenter.getSleepData(beginTime,endTime)
    }

    override fun onSleepData(sleepData: SleepBean?) {
        if (sleepData != null){
            sleepHistogramView.loadData(sleepData.sleepData)

            tvDeepTime.text = dateFormat(sleepData.deep)
            tvLightTime.text = dateFormat(sleepData.light)
            tvSoberTime.text = dateFormat(sleepData.sober)
        }else{
            tvDeepTime.text = "--"
            tvLightTime.text = "--"
            tvSoberTime.text = "--"
            sleepHistogramView.loadData(emptyList())
        }


    }

    private fun dateFormat(min:Int):String{
        val hourStr = (min / 60)
        val minStr = (min  % 60)

        if (hourStr == 0){
            return "${minStr}分钟"
        }
        return "${hourStr}小时${minStr}分钟"
    }
}