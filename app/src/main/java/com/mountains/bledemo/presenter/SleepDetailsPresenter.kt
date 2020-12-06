package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.SleepBean
import com.mountains.bledemo.view.SleepDetailsView
import org.litepal.LitePal
import org.litepal.extension.find

class SleepDetailsPresenter : BasePresenter<SleepDetailsView>() {

    fun getSleepData(beginTime:Long,endTime:Long){
        val sleepData = LitePal.where("beginDateTime >= ? and endDateTime <= ?", "$beginTime", "$endTime").order("beginDateTime desc").find<SleepBean>(true)
        if (sleepData.isEmpty()){
            view?.onSleepData(emptyList(),"--","--","--")
        }else{
            val deepDuration = dateFormat(sleepData.first().deep)
            val lightDuration = dateFormat(sleepData.first().light)
            val somberDuration = dateFormat(sleepData.first().sober)
            view?.onSleepData(sleepData.first().sleepData,deepDuration,lightDuration,somberDuration)
        }
    }

    private fun dateFormat(min:Int):String{
        val hourStr = (min / 60)
        val minStr = (min  % 60)

        if (hourStr == 0){
            return "${minStr}分钟"
        }
        return "${hourStr}小时${minStr}分钟"
    }
}