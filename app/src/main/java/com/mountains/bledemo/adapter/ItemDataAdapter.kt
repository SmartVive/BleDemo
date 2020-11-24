package com.mountains.bledemo.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mountains.bledemo.R
import com.mountains.bledemo.bean.CardItemData
import kotlinx.android.synthetic.main.item_card.view.*

class ItemDataAdapter(layoutResId:Int,data:MutableList<CardItemData>) : BaseQuickAdapter<CardItemData,BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: CardItemData) {
        holder.setImageResource(R.id.ivIcon,item.iconSrc)
        if (item.value != null){
            holder.setVisible(R.id.tvValue,true)
            holder.setText(R.id.tvValue,item.value)
        }else{
            holder.setVisible(R.id.tvValue,false)
        }

        if (item.time != null){
            holder.setVisible(R.id.tvTime,true)
            holder.setText(R.id.tvTime,"最后一次:${item.time}")
        }else{
            holder.setVisible(R.id.tvTime,false)
        }
        holder.setText(R.id.tvName,item.name)
    }
}