package com.mountains.bledemo.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.mountains.bledemo.R
import com.mountains.bledemo.adapter.BloodOxygenAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.BloodOxygenBean
import com.mountains.bledemo.presenter.BloodOxygenDetailsPresenter
import com.mountains.bledemo.ui.fragment.CalendarDialogFragment
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.BloodOxygenDetailsView
import com.mountains.bledemo.weiget.SelectDateView
import kotlinx.android.synthetic.main.activity_blood_oxygen_details.*
import kotlinx.android.synthetic.main.activity_blood_oxygen_details.histogramView
import kotlinx.android.synthetic.main.activity_blood_oxygen_details.recyclerView
import kotlinx.android.synthetic.main.activity_blood_oxygen_details.selectDateView
import kotlinx.android.synthetic.main.activity_blood_oxygen_details.titleBar
import kotlinx.android.synthetic.main.activity_heart_rate_details.*

class BloodOxygenDetailsActivity : BaseActivity<BloodOxygenDetailsPresenter>(),BloodOxygenDetailsView {
    val bloodOxygenList = mutableListOf<BloodOxygenBean>()
    val bloodOxygenAdapter by lazy { BloodOxygenAdapter(R.layout.item_heart_rate,bloodOxygenList) }
    var currentSelectTime = System.currentTimeMillis()

    override fun createPresenter(): BloodOxygenDetailsPresenter {
        return BloodOxygenDetailsPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_oxygen_details)
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

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bloodOxygenAdapter
        }
        val headerView = LayoutInflater.from(getContext()).inflate(R.layout.item_blood_oxygen_header,recyclerView,false)
        bloodOxygenAdapter.addHeaderView(headerView)
    }

    private fun initData(){
        val calendar = CalendarUtil.getCalendar(currentSelectTime)
        CalendarUtil.setCalendarToBegin(calendar)
        val beginTime = calendar.timeInMillis
        CalendarUtil.setCalendarToEnd(calendar)
        val endTime = calendar.timeInMillis
        presenter.getBloodOxygenData(beginTime, endTime)
    }

    override fun onBloodOxygenData(
        bloodOxygenData: List<BloodOxygenBean>,
        avgBloodOxygen: String,
        minBloodOxygen: String,
        maxBloodOxygen: String
    ) {
        histogramView.loadData(bloodOxygenData)
        tvAvgBloodOxygen.text = avgBloodOxygen
        tvMaxBloodOxygen.text = maxBloodOxygen
        tvMinBloodOxygen.text = minBloodOxygen

        bloodOxygenList.clear()
        bloodOxygenList.addAll(bloodOxygenData)
        bloodOxygenAdapter.notifyDataSetChanged()
    }

}