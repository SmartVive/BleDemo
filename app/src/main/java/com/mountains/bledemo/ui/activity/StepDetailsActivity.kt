package com.mountains.bledemo.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.SportBean
import com.mountains.bledemo.presenter.StepDetailsPresenter
import com.mountains.bledemo.view.StepDetailsView
import kotlinx.android.synthetic.main.activity_step_details.*
import java.util.*

class StepDetailsActivity : BaseActivity<StepDetailsPresenter>(),StepDetailsView {

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

    }

    private fun initData(){
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startTime:Long = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 30)
        val endTime:Long = calendar.timeInMillis
        presenter.getStepsData(startTime,endTime)
    }


    override fun onStepsData(stepsData: List<SportBean.StepBean>) {
        histogramView.loadData(stepsData)
    }

}