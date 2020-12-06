package com.mountains.bledemo.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mountains.bledemo.R
import com.mountains.bledemo.bean.SportBean
import com.mountains.bledemo.util.CalendarUtil

class StepAdapter(layoutResId:Int,data:MutableList<SportBean.StepBean>) : BaseQuickAdapter<SportBean.StepBean,BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: SportBean.StepBean) {
        holder.setText(R.id.tvTime,CalendarUtil.format("HH:mm",item.dateTime))
        holder.setText(R.id.tvValue,"${item.value}步")
    }
}