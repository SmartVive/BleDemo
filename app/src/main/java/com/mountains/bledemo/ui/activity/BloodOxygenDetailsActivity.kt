package com.mountains.bledemo.ui.activity

import android.os.Bundle
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.BloodOxygenBean
import com.mountains.bledemo.presenter.BloodOxygenDetailsPresenter
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.BloodOxygenDetailsView
import kotlinx.android.synthetic.main.activity_blood_oxygen_details.*

class BloodOxygenDetailsActivity : BaseActivity<BloodOxygenDetailsPresenter>(),BloodOxygenDetailsView {

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

    }

    private fun initData(){
        val startTime = CalendarUtil.getTodayBeginCalendar().timeInMillis
        val endTime = CalendarUtil.getTodayEndCalendar().timeInMillis
        presenter.getBloodOxygenData(startTime, endTime)
    }

    override fun onBloodOxygenData(
        bloodOxygenData: List<BloodOxygenBean>,
        avgBloodOxygen: String,
        maxBloodOxygen: String,
        minBloodOxygen: String
    ) {
        histogramView.loadData(bloodOxygenData)
        tvAvgBloodOxygen.text = avgBloodOxygen
        tvMaxBloodOxygen.text = maxBloodOxygen
        tvMinBloodOxygen.text = minBloodOxygen
    }

}