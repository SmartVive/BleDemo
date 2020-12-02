package com.mountains.bledemo.ui.activity

import android.os.Bundle
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.BloodPressureBean
import com.mountains.bledemo.presenter.BloodPressureDetailsPresenter
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.BloodPressureDetailsView
import kotlinx.android.synthetic.main.activity_blood_pressure_details.*

class BloodPressureDetailsActivity : BaseActivity<BloodPressureDetailsPresenter>(),BloodPressureDetailsView {

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

    }

    private fun initData(){
        val beginTime = CalendarUtil.getTodayBeginCalendar().timeInMillis
        val endTime = CalendarUtil.getTodayEndCalendar().timeInMillis
        presenter.getBloodPressureData(beginTime, endTime)
    }

    override fun onBloodPressureData(list: List<BloodPressureBean>) {
        bloodPressureHistogramView.loadBloodPressureData(list)
    }
}