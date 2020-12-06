package com.mountains.bledemo.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.mountains.bledemo.R
import com.mountains.bledemo.adapter.HeartRateAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.presenter.HeartRateDetailsPresenter
import com.mountains.bledemo.ui.fragment.CalendarDialogFragment
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.HeartRateDetailsView
import com.mountains.bledemo.weiget.SelectDateView
import kotlinx.android.synthetic.main.activity_heart_rate_details.*
import kotlinx.android.synthetic.main.activity_heart_rate_details.histogramView
import kotlinx.android.synthetic.main.activity_heart_rate_details.recyclerView
import kotlinx.android.synthetic.main.activity_heart_rate_details.selectDateView
import kotlinx.android.synthetic.main.activity_heart_rate_details.titleBar
import kotlinx.android.synthetic.main.activity_step_details.*
import java.util.*

class HeartRateDetailsActivity : BaseActivity<HeartRateDetailsPresenter>(),HeartRateDetailsView{
    val heartRateList = mutableListOf<HeartRateBean>()
    val heartRateAdapter by lazy { HeartRateAdapter(R.layout.item_heart_rate,heartRateList) }
    var currentSelectTime = System.currentTimeMillis()

    override fun createPresenter(): HeartRateDetailsPresenter {
        return HeartRateDetailsPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate_details)
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

        recyclerView.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = heartRateAdapter
        }



        val headerView = LayoutInflater.from(getContext()).inflate(R.layout.item_heart_rate_header,recyclerView,false)
        heartRateAdapter.addHeaderView(headerView)
    }

    private fun initData(){
        val calendar = CalendarUtil.getCalendar(currentSelectTime)
        CalendarUtil.setCalendarToBegin(calendar)
        val beginTime = calendar.timeInMillis
        CalendarUtil.setCalendarToEnd(calendar)
        val endTime = calendar.timeInMillis
        presenter.getHeartRate(beginTime,endTime)
    }

    override fun onHeartRateData(
        list: List<HeartRateBean>,
        avgHeartRate: String,
        minHeartRate: String,
        maxHeartRate: String
    ) {
        histogramView.loadData(list)

        heartRateList.clear()
        heartRateList.addAll(list)
        heartRateAdapter.notifyDataSetChanged()

        tvAvgHeartRate.text = avgHeartRate
        tvMinHeartRate.text = minHeartRate
        tvMaxHeartRate.text = maxHeartRate
    }




}