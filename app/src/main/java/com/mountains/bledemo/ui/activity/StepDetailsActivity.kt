package com.mountains.bledemo.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mountains.bledemo.R
import com.mountains.bledemo.adapter.StepAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.SportBean
import com.mountains.bledemo.presenter.StepDetailsPresenter
import com.mountains.bledemo.ui.fragment.CalendarDialogFragment
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.StepDetailsView
import com.mountains.bledemo.weiget.SelectDateView
import kotlinx.android.synthetic.main.activity_step_details.*
import kotlinx.android.synthetic.main.include_date.*

class StepDetailsActivity : BaseActivity<StepDetailsPresenter>(),StepDetailsView {
    val stepList = mutableListOf<SportBean.StepBean>()
    val stepAdapter by lazy { StepAdapter(R.layout.item_step,stepList) }
    var currentSelectTime = System.currentTimeMillis()

    override fun createPresenter(): StepDetailsPresenter {
        return StepDetailsPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_details)
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

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stepAdapter
        }

        selectDateView.setDate(currentSelectTime)
        selectDateView.setOnDateChangeListener(object : SelectDateView.OnDateChangeListener{
            override fun onDateChange(date: Long) {
                currentSelectTime = date
                initData()
            }

        })

    }


    private fun initData(){
        val calendar = CalendarUtil.getCalendar(currentSelectTime)
        CalendarUtil.setCalendarToBegin(calendar)
        val beginTime = calendar.timeInMillis
        CalendarUtil.setCalendarToEnd(calendar)
        val endTime = calendar.timeInMillis
        presenter.getStepsData(beginTime,endTime)
    }


    override fun onStepsData(stepsData: List<SportBean.StepBean>,totalSteps:String,totalDistance:String,totalCalorie:String) {
        histogramView.loadData(stepsData)
        stepList.clear()
        stepList.addAll(stepsData)
        stepAdapter.notifyDataSetChanged()

        tvStep.text = totalSteps
        tvDistance.text = totalDistance
        tvCalorie.text = totalCalorie
    }

}