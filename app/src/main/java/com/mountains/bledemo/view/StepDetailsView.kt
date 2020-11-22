package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView
import com.mountains.bledemo.bean.SportBean

interface StepDetailsView : BaseView {

    fun onStepsData(stepsData:List<SportBean.StepBean>)
}