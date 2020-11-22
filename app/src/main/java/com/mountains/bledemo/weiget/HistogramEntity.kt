package com.mountains.bledemo.weiget

class HistogramEntity(val value:Int,val time:Long):IHistogramData{


    override fun getHistogramValue(): Int {
        return value
    }

    override fun getHistogramTime(): Long {
        return time
    }

}