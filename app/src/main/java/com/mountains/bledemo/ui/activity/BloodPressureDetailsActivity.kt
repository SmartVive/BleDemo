package com.mountains.bledemo.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.mountains.bledemo.R
import com.mountains.bledemo.adapter.BloodPressureAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.BloodPressureBean
import com.mountains.bledemo.presenter.BloodPressureDetailsPresenter
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.BloodPressureDetailsView
import kotlinx.android.synthetic.main.activity_blood_pressure_details.*

class BloodPressureDetailsActivity : BaseActivity<BloodPressureDetailsPresenter>(),BloodPressureDetailsView {
    private val bloodPressureList = mutableListOf<BloodPressureBean>()
    private val bloodPressureAdapter by lazy { BloodPressureAdapter(R.layout.item_heart_rate,bloodPressureList) }

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
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bloodPressureAdapter
        }

        val header = LayoutInflater.from(getContext()).inflate(R.layout.item_blood_pressure_header,recyclerView,false)
        bloodPressureAdapter.addHeaderView(header)
    }

    private fun initData(){
        val beginTime = CalendarUtil.getTodayBeginCalendar().timeInMillis
        val endTime = CalendarUtil.getTodayEndCalendar().timeInMillis
        presenter.getBloodPressureData(beginTime, endTime)
    }

    override fun onBloodPressureData(list: List<BloodPressureBean>,avgBloodDiastolic:String,avgBloodSystolic:String,minBloodDiastolic:String,maxBloodSystolic:String) {
        bloodPressureHistogramView.loadBloodPressureData(list)

        tvAvgBloodDiastolic.text = avgBloodDiastolic
        tvAvgBloodSystolic.text = avgBloodSystolic

        bloodPressureList.clear()
        bloodPressureList.addAll(list)
        bloodPressureAdapter.notifyDataSetChanged()
    }
}