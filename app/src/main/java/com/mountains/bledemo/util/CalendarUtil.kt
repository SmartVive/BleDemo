package com.mountains.bledemo.util

import java.text.SimpleDateFormat
import java.util.*

object CalendarUtil {

    /**
     * 获取当前日历
     */
    fun getCurrentCalendar():Calendar{
        return Calendar.getInstance()
    }

    /**
     * 今天00:00:00日历
     */
    fun getTodayBeginCalendar() : Calendar{
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY,0)
        calendar.set(Calendar.MINUTE,0)
        calendar.set(Calendar.SECOND,0)
        calendar.set(Calendar.MILLISECOND,0)
        return calendar
    }

    /**
     * 今天23:59:59日历
     */
    fun getTodayEndCalendar() : Calendar{
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY,23)
        calendar.set(Calendar.MINUTE,59)
        calendar.set(Calendar.SECOND,59)
        calendar.set(Calendar.MILLISECOND,999)
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

    /**
     * 根据时间戳获取当天00:00:00日历
     */
    fun getBeginCalendar(timeInMills: Long) : Calendar{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMills
        setCalendarToBegin(calendar)
        return calendar
    }

    /**
     * 根据时间戳获取当天23:59:59日历
     */
    fun getEndCalendar(timeInMills: Long) : Calendar{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMills
        setCalendarToEnd(calendar)
        return calendar
    }

    /**
     * 设置为当天00:00:00
     */
    fun setCalendarToBegin(calendar: Calendar){
        calendar.set(Calendar.HOUR_OF_DAY,0)
        calendar.set(Calendar.MINUTE,0)
        calendar.set(Calendar.SECOND,0)
        calendar.set(Calendar.MILLISECOND,0)
    }

    /**
     * 设置为当天23:59:59
     */
    fun setCalendarToEnd(calendar: Calendar){
        calendar.set(Calendar.HOUR_OF_DAY,23)
        calendar.set(Calendar.MINUTE,59)
        calendar.set(Calendar.SECOND,59)
        calendar.set(Calendar.MILLISECOND,999)
    }

    /**
     * 判断是否是同一天
     */
    fun isSameDay(timeInMills1: Long,timeInMills2: Long):Boolean{
        val calendar1 = Calendar.getInstance()
        calendar1.timeInMillis = timeInMills1

        val calendar2 = Calendar.getInstance()
        calendar2.timeInMillis = timeInMills2
        return isSameDay(calendar1,calendar2)
    }

    fun isSameDay(calendar1: Calendar,calendar2: Calendar):Boolean{
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) && calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
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