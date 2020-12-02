package com.mountains.bledemo.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mountains.bledemo.R
import com.mountains.bledemo.bean.BloodOxygenBean
import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.util.CalendarUtil

class BloodOxygenAdapter(layoutResId:Int, data:MutableList<BloodOxygenBean>) : BaseQuickAdapter<BloodOxygenBean,BaseViewHolder>(layoutResId,data) {
    override fun convert(holder: BaseViewHolder, item: BloodOxygenBean) {
        holder.setText(R.id.tvTime, CalendarUtil.format("HH:mm",item.dateTime))
        holder.setText(R.id.tvValue,"${item.value} %")

        if (item.value < 95){
            //血氧过低
            holder.setTextColorRes(R.id.tvValue,android.R.color.holo_orange_dark)
        }else{
            //血氧正常
            holder.setTextColorRes(R.id.tvValue,R.color.mainTextColor)
        }
    }
}