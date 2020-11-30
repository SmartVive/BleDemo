package com.mountains.bledemo.bean

import com.mountains.bledemo.weiget.IHistogramData
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import java.util.*

class BloodOxygenBean : LitePalSupport,Comparable<BloodOxygenBean>, IHistogramData {
    val id:Long = 0
    @Column(unique = true)
    var dateTime: Long = 0L
    var index: Int = 0
    var value: Int = 0
    private val calendar by lazy { Calendar.getInstance() }

    constructor()

    constructor(dateTime: Long,index: Int,value: Int){
        this.dateTime = dateTime
        this.index = index
        this.value = value
    }

    override fun compareTo(other: BloodOxygenBean): Int {
        return (other.dateTime - dateTime).toInt()
    }

    override fun getHistogramValue(): Int {
        return value
    }

    override fun getHistogramTime(): Long {
        val histogramTime:Long
        calendar.timeInMillis = dateTime
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        histogramTime = hour*60L*60L + minute*60L + second
        return histogramTime
    }
}