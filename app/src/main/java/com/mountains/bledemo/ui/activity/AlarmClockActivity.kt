package com.mountains.bledemo.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
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

    companion object{
        const val ALARM_CLOCK_ADD_ACTIVITY_REQUEST_CODE = 100
    }

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
            AlarmClockAddActivity.actionStart(this,ALARM_CLOCK_ADD_ACTIVITY_REQUEST_CODE)
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
            addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.VERTICAL))
            adapter = alarmClockAdapter
        }



        alarmClockAdapter.apply {
            val emptyView = LayoutInflater.from(getContext()).inflate(R.layout.item_empty_data, recyclerView, false)
            emptyView.findViewById<TextView>(R.id.tvEmpty).text = "暂未设置闹钟"
            setEmptyView(emptyView)

            setOnItemChildClickListener { adapter, view, position ->
                if (view.id == R.id.contentView){
                    AlarmClockAddActivity.actionStart(this@AlarmClockActivity,alarmClockList[position], ALARM_CLOCK_ADD_ACTIVITY_REQUEST_CODE)
                }else if (view.id == R.id.rightMenuView){
                    presenter.deleteAlarmClock(position,alarmClockList[position])
                }
            }

            setOnAlarmClockSwitchListener(object : AlarmClockAdapter.OnAlarmClockSwitchListener{
                override fun onAlarmClockSwitch(buttonView: CompoundButton, isChecked: Boolean, position: Int) {
                    presenter.switchAlarmClock(alarmClockList[position],isChecked)
                }
            })
        }


    }

    private fun initData(){
        presenter.getAlarmClockList()
    }

    override fun onAlarmClockList(list: List<AlarmClockBean>) {
        alarmClockList.clear()
        alarmClockList.addAll(list)
        alarmClockAdapter.notifyDataSetChanged()
    }



    override fun onSwitchAlarmClockSuccess(isOpen:Boolean,distanceNextAlarmTime: String) {
        if (isOpen){
            showToast("距离下次闹钟还剩$distanceNextAlarmTime")
        }
        initData()
    }

    override fun onDeleteAlarmClockSuccess(position: Int) {
        alarmClockAdapter.removeAt(position)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ALARM_CLOCK_ADD_ACTIVITY_REQUEST_CODE){
            initData()
        }
    }
}