package com.mountains.bledemo.adapter

import android.widget.Switch
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mountains.bledemo.R
import com.mountains.bledemo.bean.AlarmClockBean
import java.lang.StringBuilder

class AlarmClockAdapter(layoutResId:Int,data:MutableList<AlarmClockBean>) : BaseQuickAdapter<AlarmClockBean,BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: AlarmClockBean) {
        val weeks = arrayOf("周一","周二","周三","周四","周五","周六","周日")
        val time = "${item.hour}:${item.minute}"
        val repeat = item.repeat.let {
            val sb = StringBuilder()
            if (it[1] == 1){
                sb.append("周一,")
            }
            if (it[2]  == 1){
                sb.append("周二,")
            }
            if (it[3]  == 1){
                sb.append("周三,")
            }
            if (it[4] == 1){
                sb.append("周四,")
            }
            if (it[5] == 1){
                sb.append("周五,")
            }
            if (it[6] == 1){
                sb.append("周六,")
            }
            if (it[0] == 1){
                sb.append("周日,")
            }
            if (sb.isNotEmpty()){
                sb.deleteCharAt(sb.length-1)
            }
            sb.toString()
        }
        holder.setText(R.id.tvTime,time)
        holder.setText(R.id.tvRepeat,repeat)
        val switch = holder.getView<Switch>(R.id.switchOpen)
        switch.isChecked = item.isOpen
    }
}