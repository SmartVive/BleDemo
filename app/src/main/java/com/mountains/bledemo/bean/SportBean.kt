package com.mountains.bledemo.bean

import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.weiget.IHistogramData
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import java.util.*

class SportBean(
    val steps: Int,
    val mileage: Int,
    val calorie: Int
) {

    open class BaseSportBean: LitePalSupport,Comparable <BaseSportBean>,IHistogramData {
        val id:Long = 0
        @Column(unique = true)
        var dateTime: Long = 0L
        var index: Int = 0
        var value: Int = 0

        constructor()

        constructor(dateTime: Long,index: Int,value: Int){
            this.dateTime = dateTime
            this.index = index
            this.value = value
        }

        override fun compareTo(other: BaseSportBean): Int {
            return (other.dateTime - dateTime).toInt()
        }

        override fun getHistogramValue(): Int {
            return value
        }

        override fun getHistogramTime(): Long {
            val histogramTime:Long
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dateTime
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val sencond = calendar.get(Calendar.SECOND)
            histogramTime = hour*60L*60L + minute*60L + sencond
            return histogramTime
        }

    }

    class StepBean : BaseSportBean{
        constructor():super()
        constructor(dateTime: Long, index: Int, value: Int):super( dateTime, index, value)
    }

    class DistanceBean : BaseSportBean{
        constructor():super()
        constructor(dateTime: Long, index: Int, value: Int):super( dateTime, index, value)
    }

    class CalorieBean : BaseSportBean{
        constructor():super()
        constructor(dateTime: Long, index: Int, value: Int):super( dateTime, index, value)
    }

}