package com.mountains.bledemo.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.mountains.bledemo.R
import com.mountains.bledemo.adapter.BloodPressureAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.BloodPressureBean
import com.mountains.bledemo.presenter.BloodPressureDetailsPresenter
import com.mountains.bledemo.ui.fragment.CalendarDialogFragment
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.BloodPressureDetailsView
import com.mountains.bledemo.weiget.SelectDateView
import kotlinx.android.synthetic.main.activity_blood_pressure_details.*

class BloodPressureDetailsActivity : BaseActivity<BloodPressureDetailsPresenter>(),BloodPressureDetailsView {
    private val bloodPressureList = mutableListOf<BloodPressureBean>()
    private val bloodPressureAdapter by lazy { BloodPressureAdapter(R.layout.item_heart_rate,bloodPressureList) }
    var currentSelectTime = System.currentTimeMillis()

    override fun createPresenter(): BloodPressureDetailsPresenter {
        return BloodPressureDetailsPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_pressure_details)
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
            adapter = bloodPressureAdapter
        }

        val header = LayoutInflater.from(getContext()).inflate(R.layout.item_blood_pressure_header,recyclerView,false)
        bloodPressureAdapter.addHeaderView(header)
    }

    private fun initData(){
        val calendar = CalendarUtil.getCalendar(currentSelectTime)
        CalendarUtil.setCalendarToBegin(calendar)
        val beginTime = calendar.timeInMillis
        CalendarUtil.setCalendarToEnd(calendar)
        val endTime = calendar.timeInMillis
        presenter.getBloodPressureData(beginTime, endTime)
    }

    override fun onBloodPressureData(list: List<BloodPressureBean>,avgBloodDiastolic:String,avgBloodSystolic:String,minBloodDiastolic:String,maxBloodSystolic:String) {
        bloodPressureHistogramView.loadData(list)

        tvAvgBloodDiastolic.text = avgBloodDiastolic
        tvAvgBloodSystolic.text = avgBloodSystolic

        bloodPressureList.clear()
        bloodPressureList.addAll(list)
        bloodPressureAdapter.notifyDataSetChanged()
    }
}