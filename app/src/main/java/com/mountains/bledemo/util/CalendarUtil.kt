package com.mountains.bledemo.util

import java.text.SimpleDateFormat
import java.util.*

object CalendarUtil {

    fun getTodayCalendar() : Calendar{
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY,0)
        calendar.set(Calendar.MINUTE,0)
        calendar.set(Calendar.SECOND,0)
        calendar.set(Calendar.MILLISECOND,0)
        return calendar
    }

    fun getTomorrowCalendar() : Calendar{
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY,0)
        calendar.set(Calendar.MINUTE,0)
        calendar.set(Calendar.SECOND,0)
        calendar.set(Calendar.MILLISECOND,0)
        calendar.add(Calendar.DAY_OF_MONTH,1)
        return calendar
    }

    fun getYesterdayCalendar() : Calendar{
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY,0)
        calendar.set(Calendar.MINUTE,0)
        calendar.set(Calendar.SECOND,0)
        calendar.set(Calendar.MILLISECOND,0)
        calendar.add(Calendar.DAY_OF_MONTH,-1)
        return calendar
    }

    fun format(format: String,calendar: Calendar):String{
        return format(format,calendar.timeInMillis)
    }

    fun format(format: String,timeInMills:Long):String{
        return SimpleDateFormat(format, Locale.ENGLISH).format(timeInMills)
    }

    fun convertTimeToIndex(calendar: Calendar, mEveryMinutes: Int): Int {
        return convertTimeToIndex(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), mEveryMinutes)
    }

    fun convertTimeToIndex(hour: Int, minute: Int, mEveryMinutes: Int): Int {
        return ((60 / mEveryMinutes) * hour) + (minute / mEveryMinutes)
    }
}