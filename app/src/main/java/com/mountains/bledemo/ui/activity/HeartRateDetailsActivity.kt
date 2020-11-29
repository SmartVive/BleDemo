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
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.HeartRateDetailsView
import kotlinx.android.synthetic.main.activity_heart_rate_details.*
import kotlinx.android.synthetic.main.activity_heart_rate_details.histogramView
import java.util.*

class HeartRateDetailsActivity : BaseActivity<HeartRateDetailsPresenter>(),HeartRateDetailsView{
    val heartRateList = mutableListOf<HeartRateBean>()
    val heartRateAdapter by lazy { HeartRateAdapter(R.layout.item_heart_rate,heartRateList) }

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

        recyclerView.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = heartRateAdapter
        }

        val headerView = LayoutInflater.from(getContext()).inflate(R.layout.item_heart_rate_header,recyclerView,false)
        heartRateAdapter.addHeaderView(headerView)
    }

    private fun initData(){
        val todayBeginCalendar = CalendarUtil.getTodayBeginCalendar()
        val todayEndCalendar = CalendarUtil.getTodayEndCalendar()
        val startTime = todayBeginCalendar.timeInMillis
        val endTime = todayEndCalendar.timeInMillis
        presenter.getHeartRate(startTime,endTime)
    }

    override fun onHeartRateData(list: List<HeartRateBean>, avgHeartRate: Int, maxHeartRate: Int, minHeartRate: Int) {
        histogramView.loadData(list)

        heartRateList.clear()
        heartRateList.addAll(list)
        heartRateAdapter.notifyDataSetChanged()


        setTextValue(tvAvgHeartRate,avgHeartRate)
        setTextValue(tvMaxHeartRate,maxHeartRate)
        setTextValue(tvMinHeartRate,minHeartRate)
    }

    private fun setTextValue(textView: TextView,value:Int){
        textView.text =  if (value!=0){
            "$value bpm"
        }else{
            "--"
        }
    }


}