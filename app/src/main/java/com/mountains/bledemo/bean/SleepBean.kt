package com.mountains.bledemo.bean

import com.mountains.bledemo.bean.SleepBean.SleepData


class SleepBean {
    var beginDateTime: Long = 0
    var endDateTime: Long = 0
    var deep: Int = 0
    var light: Int = 0
    var sleepData: List<SleepData> = mutableListOf()
    var sober: Int = 0

    companion object {
        //浅睡眠
        const val STATUS_LIGHT = 0
        //深睡眠
        const val STATUS_DEEP = 1
        //清醒
        const val STATUS_SOBER = 2
    }


    class SleepData {
        var minutes: Int = 0
        var statuss: Int = 0
        var values: Int = 0
    }
}