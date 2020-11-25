package com.mountains.bledemo.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mountains.bledemo.R
import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.util.CalendarUtil

class HeartRateAdapter(layoutResId:Int,data:MutableList<HeartRateBean>) : BaseQuickAdapter<HeartRateBean,BaseViewHolder>(layoutResId,data) {
    override fun convert(holder: BaseViewHolder, item: HeartRateBean) {
        holder.setText(R.id.tvTime, CalendarUtil.format("HH:mm",item.dateTime))
        holder.setText(R.id.tvValue,"${item.value} bpm")

        if (item.value < 60){
            //心率过低
            holder.setTextColorRes(R.id.tvValue,android.R.color.holo_orange_dark)
        }else if(item.value > 100){
            //心率过高
            holder.setTextColorRes(R.id.tvValue,android.R.color.holo_red_light)
        }else{
            //心率正常
            holder.setTextColorRes(R.id.tvValue,R.color.mainTextColor)
        }
    }
}