package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.SleepBean
import com.mountains.bledemo.view.SleepDetailsView
import org.litepal.LitePal
import org.litepal.extension.find

class SleepDetailsPresenter : BasePresenter<SleepDetailsView>() {

    fun getSleepData(beginTime:Long,endTime:Long){
        val sleepData = LitePal.where("beginDateTime >= ? and endDateTime <= ?", "$beginTime", "$endTime").order("beginDateTime desc").find<SleepBean>(true)
        if (sleepData.isNotEmpty()){
            view?.onSleepData(sleepData.first())
        }else{
            view?.onSleepData(null)
        }
    }


}