package com.mountains.bledemo.bean

import com.mountains.bledemo.weiget.BloodPressureHistogramView
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import java.util.*

class BloodPressureBean : LitePalSupport,Comparable<BloodPressureBean>, BloodPressureHistogramView.IBloodPressureHistogramData {
    val id:Long = 0
    @Column(unique = true)
    var dateTime: Long = 0L
    var index: Int = 0
    private val calendar by lazy { Calendar.getInstance() }
    //舒张压
    var bloodDiastolic: Int = 0
    //收缩压
    var bloodSystolic: Int = 0

    constructor()

    constructor(dateTime: Long,index: Int,bloodDiastolic: Int,bloodSystolic :Int){
        this.dateTime = dateTime
        this.index = index
        this.bloodDiastolic = bloodDiastolic
        this.bloodSystolic = bloodSystolic
    }

    override fun compareTo(other: BloodPressureBean): Int {
        return (other.dateTime - dateTime).toInt()
    }

    override fun getHistogramBloodDiastolic(): Int {
        return bloodDiastolic
    }

    override fun getHistogramBloodSystolic(): Int {
        return bloodSystolic
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