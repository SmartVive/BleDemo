package com.mountains.bledemo.adapter

import android.widget.CompoundButton
import android.widget.Switch
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mountains.bledemo.R
import com.mountains.bledemo.bean.AlarmClockBean
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.weiget.NoSlideSwitch
import java.lang.StringBuilder
import java.util.*

class AlarmClockAdapter(layoutResId: Int, data: MutableList<AlarmClockBean>) : BaseQuickAdapter<AlarmClockBean, BaseViewHolder>(layoutResId, data) {
    private var onAlarmClockSwitchListener : OnAlarmClockSwitchListener? = null

    init {
        addChildClickViewIds(R.id.contentView)
        addChildClickViewIds(R.id.rightMenuView)
    }

    override fun convert(holder: BaseViewHolder, item: AlarmClockBean) {
        val weeks = arrayOf( "周日","周一", "周二", "周三", "周四", "周五", "周六")
        val hourString = if (item.hour < 10){
            "0${item.hour}"
        }else{
            "${item.hour}"
        }
        val minuteString = if (item.minute < 10){
            "0${item.minute}"
        }else{
            "${item.minute}"
        }
        val time = "$hourString:$minuteString"
        val repeat = item.repeat.let {
            val sb = StringBuilder()
            if (item.isAllDayAlarm()){
                //每天闹钟
                sb.append("每天")
            }else if (!item.isSingleAlarm()) {
                for (i in 1 .. it.size){
                    if (it[i%7] == 1){
                        sb.append("${weeks[i%7]},")
                    }
                }
                sb.deleteCharAt(sb.length - 1)
            } else {
                //单次闹钟,判断是否已经执行过
                val alarmClockCalendar = CalendarUtil.getCalendar(item.date)
                val currentCalendar = CalendarUtil.getCalendar(System.currentTimeMillis())

                val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = currentCalendar.get(Calendar.MINUTE)
                val alarmClockHour = alarmClockCalendar.get(Calendar.HOUR_OF_DAY)
                val alarmClockMinute = alarmClockCalendar.get(Calendar.MINUTE)

                if (currentHour*60+currentMinute < alarmClockHour*60+alarmClockMinute) {
                    sb.append("今天")
                } else {
                    sb.append("明天")
                }

            }
            sb.toString()
        }
        holder.setText(R.id.tvTime, time)
        holder.setText(R.id.tvRepeat, repeat)
        val switch = holder.getView<NoSlideSwitch>(R.id.switchOpen)
        switch.isChecked = item.isOpen

        switch.setOnWantCheckedChangeListener(object : NoSlideSwitch.OnWantCheckedChangeListener{
            override fun onWantCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                onAlarmClockSwitchListener?.onAlarmClockSwitch(buttonView,isChecked,holder.adapterPosition)
            }
        })


        if (item.isOpen){
            holder.setTextColorRes(R.id.tvTime,R.color.mainTextColor)
            holder.setTextColorRes(R.id.tvRepeat,R.color.subTextColor)
        }else{
            holder.setTextColorRes(R.id.tvTime,R.color.disableTextColor)
            holder.setTextColorRes(R.id.tvRepeat,R.color.disableTextColor)
        }
    }

    fun setOnAlarmClockSwitchListener(listener: OnAlarmClockSwitchListener){
        onAlarmClockSwitchListener = listener
    }

    interface OnAlarmClockSwitchListener{
        fun onAlarmClockSwitch(buttonView: CompoundButton,isChecked:Boolean,position:Int)
    }
}