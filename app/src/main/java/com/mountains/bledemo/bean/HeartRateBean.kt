package com.mountains.bledemo.bean

import com.mountains.bledemo.weiget.IHistogramData
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import java.util.*

class HeartRateBean : LitePalSupport, Comparable<HeartRateBean>, IHistogramData {
    val id: Long = 0
    var mac: String = ""
    var dateTime: Long = 0L
    var index: Int = 0
    var value: Int = 0
    val calendar by lazy { Calendar.getInstance() }

    constructor()

    constructor(mac: String, dateTime: Long, index: Int, value: Int) {
        this.mac = mac
        this.dateTime = dateTime
        this.index = index
        this.value = value
    }

    override fun compareTo(other: HeartRateBean): Int {
        return (other.dateTime - dateTime).toInt()
    }

    override fun getHistogramValue(): Int {
        return value
    }

    override fun getHistogramTime(): Long {
        val histogramTime: Long
        calendar.timeInMillis = dateTime
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        histogramTime = hour * 60L * 60L + minute * 60L + second
        return histogramTime
    }
}