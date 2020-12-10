package com.mountains.bledemo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.mountains.bledemo.R
import com.mountains.bledemo.adapter.AlarmClockAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.AlarmClockBean
import com.mountains.bledemo.presenter.AlarmClockPresenter
import com.mountains.bledemo.view.AlarmClockView
import kotlinx.android.synthetic.main.activity_alarm_clock.*

class AlarmClockActivity : BaseActivity<AlarmClockPresenter>(),AlarmClockView{
    private val alarmClockList = mutableListOf<AlarmClockBean>()
    private val alarmClockAdapter by lazy { AlarmClockAdapter(R.layout.item_alarm_clock,alarmClockList) }

    override fun createPresenter(): AlarmClockPresenter {
        return AlarmClockPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_clock)
        initView()
        initData()
    }


    private fun initView(){
        titleBar.leftView.setOnClickListener {
            finish()
        }

        titleBar.rightView.setOnClickListener {
            val intent = Intent(getContext(), AlarmClockAddActivity::class.java)
            startActivity(intent)
        }


        swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.colorPrimary)
            setOnRefreshListener {
                presenter.getAlarmClockList()
                isRefreshing = false
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = alarmClockAdapter
        }

        val emptyView = LayoutInflater.from(getContext()).inflate(R.layout.item_empty_data, recyclerView, false)
        emptyView.findViewById<TextView>(R.id.tvEmpty).text = "暂未设置闹钟"
        alarmClockAdapter.setEmptyView(emptyView)
    }

    private fun initData(){
        presenter.getAlarmClockList()
    }

    override fun onAlarmClockList(list: List<AlarmClockBean>) {
        alarmClockList.clear()
        alarmClockList.addAll(list)
        alarmClockAdapter.notifyDataSetChanged()
    }
}