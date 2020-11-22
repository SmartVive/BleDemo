package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.SportBean
import com.mountains.bledemo.view.StepDetailsView
import org.litepal.LitePal
import org.litepal.extension.find

class StepDetailsPresenter : BasePresenter<StepDetailsView>() {

    fun getStepsData(startTime:Long,endTime:Long){
        val stepsData = LitePal.where("datetime between ? and ?", "$startTime", "$endTime").order("datetime desc")
            .find<SportBean.StepBean>()
        view?.onStepsData(stepsData)
    }
}