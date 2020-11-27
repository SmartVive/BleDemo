package com.mountains.bledemo.weiget

class SleepHistogramEntity(val type:Int,val beginTime:Long,val endTime:Long):ISleepHistogramData{
    override fun getSleepType(): Int {
        return type
    }

    override fun getSleepBeginTime(): Long {
        return beginTime
    }

    override fun getSleepEndTime(): Long {
        return endTime
    }

}