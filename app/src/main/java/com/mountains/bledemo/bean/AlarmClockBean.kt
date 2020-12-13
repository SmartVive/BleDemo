package com.mountains.bledemo.bean

import android.os.Parcel
import android.os.Parcelable
import com.mountains.bledemo.util.CalendarUtil
import org.litepal.crud.LitePalSupport
import java.util.*

class AlarmClockBean() : LitePalSupport(), Parcelable {
    var id: Long = 0

    var index = 0

    var hour: Int = 0
        set(value) {
            field = value
            date = getAlarmClockDate()
        }

    var minute: Int = 0
        set(value) {
            field = value
            date = getAlarmClockDate()
        }

    var repeat = mutableListOf<Int>()
        set(value) {
            field = value
            date = getAlarmClockDate()
        }

    var isOpen = false
        get() {
            if (isSingleAlarm() && date <= System.currentTimeMillis()) {
                //单次闹钟已经执行过
                return false
            }
            return field
        }

    var mac: String? = ""

    //闹钟的日期，用来判断闹铃是否执行过
    var date: Long = 0
        private set


    //距离下次闹钟时间
    fun getDistanceNextAlarmTime(): Long {
        return date - System.currentTimeMillis()
    }

    fun getDistanceNextAlarmTimeString(): String {
        val distanceNextAlarmTime = getDistanceNextAlarmTime()
        val day = distanceNextAlarmTime / 1000 / 60 / 60 / 24
        val hour = distanceNextAlarmTime / 1000 / 60 / 60 % 24
        val minute = distanceNextAlarmTime / 1000 / 60 % 60

        if (day > 0) {
            return "${day}天${hour}小时${minute}分钟"
        } else if (hour > 0) {
            return "${hour}小时${minute}分钟"
        } else if (minute > 0) {
            return "${minute}分钟"
        } else {
            return "不到1分钟"
        }
    }

    override fun save(): Boolean {
        date = getAlarmClockDate()
        return super.save()
    }

    override fun update(id: Long): Int {
        date = getAlarmClockDate()
        return super.update(id)
    }

    override fun saveOrUpdate(vararg conditions: String?): Boolean {
        date = getAlarmClockDate()
        return super.saveOrUpdate(*conditions)
    }

    private fun getAlarmClockDate(): Long {
        val currentCalendar = CalendarUtil.getCurrentCalendar()
        val alarmClockCalendar = CalendarUtil.getTodayBeginCalendar()
        alarmClockCalendar.set(Calendar.HOUR_OF_DAY, hour)
        alarmClockCalendar.set(Calendar.MINUTE, minute)

        if (!isSingleAlarm()) {
            for (i in 0 until 7) {
                val week = alarmClockCalendar.get(Calendar.DAY_OF_WEEK)
                if (repeat[week - 1] == 1 && alarmClockCalendar.timeInMillis > currentCalendar.timeInMillis) {
                    break
                } else {
                    alarmClockCalendar.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
        } else {
            if (currentCalendar.timeInMillis >= alarmClockCalendar.timeInMillis) {
                //今天的时间已超过，设置为明天的闹钟
                alarmClockCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        return alarmClockCalendar.timeInMillis
    }

    /**
     * 是否为单次闹钟
     */
    fun isSingleAlarm(): Boolean {
        repeat.forEach {
            if (it == 1) {
                return false
            }
        }
        return true
    }

    /**
     * 是否为每天闹钟
     */
    fun isAllDayAlarm(): Boolean {
        repeat.forEach {
            if (it == 0) {
                return false
            }
        }
        return true
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        index = parcel.readInt()
        hour = parcel.readInt()
        minute = parcel.readInt()
        isOpen = parcel.readByte() != 0.toByte()
        mac = parcel.readString()
        date = parcel.readLong()
        parcel.readList(repeat, null)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeInt(index)
        parcel.writeInt(hour)
        parcel.writeInt(minute)
        parcel.writeByte(if (isOpen) 1 else 0)
        parcel.writeString(mac)
        parcel.writeLong(date)
        parcel.writeList(repeat)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AlarmClockBean> {
        override fun createFromParcel(parcel: Parcel): AlarmClockBean {
            return AlarmClockBean(parcel)
        }

        override fun newArray(size: Int): Array<AlarmClockBean?> {
            return arrayOfNulls(size)
        }
    }


}