package com.mountains.bledemo.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mountains.bledemo.R
import com.mountains.bledemo.adapter.StepAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.SportBean
import com.mountains.bledemo.presenter.StepDetailsPresenter
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.StepDetailsView
import kotlinx.android.synthetic.main.activity_step_details.*
import java.util.*

class StepDetailsActivity : BaseActivity<StepDetailsPresenter>(),StepDetailsView {
    val stepList = mutableListOf<SportBean.StepBean>()
    val stepAdapter by lazy { StepAdapter(R.layout.item_step,stepList) }

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
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stepAdapter
        }
        stepAdapter.apply {
            val headView = LayoutInflater.from(getContext()).inflate(R.layout.item_step, recyclerView, false)
            setHeaderView(headView)
        }
    }

    private fun initData(){
        val startTime = CalendarUtil.getTodayBeginCalendar().timeInMillis
        val endTime = CalendarUtil.getTodayEndCalendar().timeInMillis
        presenter.getStepsData(startTime,endTime)
    }


    override fun onStepsData(stepsData: List<SportBean.StepBean>) {
        histogramView.loadData(stepsData)
        stepList.clear()
        stepList.addAll(stepsData.reversed())
        stepAdapter.notifyDataSetChanged()
    }

}