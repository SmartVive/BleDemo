package com.mountains.bledemo.adapter

import android.text.Html
import android.text.SpannableString
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mountains.bledemo.R
import com.mountains.bledemo.bean.BloodPressureBean
import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.util.CalendarUtil
import java.lang.StringBuilder

class BloodPressureAdapter(layoutResId:Int, data:MutableList<BloodPressureBean>) : BaseQuickAdapter<BloodPressureBean,BaseViewHolder>(layoutResId,data) {
    override fun convert(holder: BaseViewHolder, item: BloodPressureBean) {
        holder.setText(R.id.tvTime, CalendarUtil.format("HH:mm",item.dateTime))

        val sb = StringBuilder()

        val lowColor = ContextCompat.getColor(context, android.R.color.holo_orange_dark)
        val highColor = ContextCompat.getColor(context, android.R.color.holo_red_light)
        if (item.bloodDiastolic < 60){
            //舒张压过低
            sb.append("<font color=\"$lowColor\">${item.bloodDiastolic}</font>")
        }else if (item.bloodDiastolic > 90){
            //舒张压过高
            sb.append("<font color=\"$highColor\">${item.bloodDiastolic}</font>")
        }else{
            sb.append("${item.bloodDiastolic}")
        }

        sb.append(" / ")

        if (item.bloodSystolic < 90){
            //舒张压过低
            sb.append("<font color=\"$lowColor\">${item.bloodSystolic}</font>")
        }else if (item.bloodSystolic > 140){
            //舒张压过高
            sb.append("<font color=\"$highColor\">${item.bloodSystolic}</font>")
        }else{
            sb.append("${item.bloodSystolic}")
        }

        sb.append(" mmHg")
        holder.setText(R.id.tvValue, Html.fromHtml(sb.toString()))
    }
}