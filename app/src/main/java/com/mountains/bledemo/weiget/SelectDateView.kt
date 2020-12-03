package com.mountains.bledemo.weiget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.mountains.bledemo.R
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.util.ToastUtil

class SelectDateView : FrameLayout {
    lateinit var tvDate:TextView
    lateinit var ivPreDay:ImageView
    lateinit var ivNextDay:ImageView
    var currentSelectTime:Long = 0
    private var listener:OnDateChangeListener? = null

    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    private fun init(){
        LayoutInflater.from(context).inflate(R.layout.view_select_date,this,true)

        tvDate = findViewById<TextView>(R.id.tvDate)
        ivPreDay = findViewById<ImageView>(R.id.ivPreDay)
        ivNextDay = findViewById<ImageView>(R.id.ivNextDay)

        ivPreDay.setOnClickListener {
            currentSelectTime -= 24*60*60*1000
            setDate(currentSelectTime)
            listener?.onDateChange(currentSelectTime)
        }

        ivNextDay.setOnClickListener {
            if (checkDate(currentSelectTime + 24*60*60*1000)){
                currentSelectTime += 24*60*60*1000
                setDate(currentSelectTime)
                listener?.onDateChange(currentSelectTime)
            }
        }
    }


    private fun checkDate(time:Long):Boolean{
        val tomorrowCalendar = CalendarUtil.getTomorrowCalendar()
        if (time > tomorrowCalendar.timeInMillis){
            ToastUtil.show("不能选择未来日期")
            return false
        }
        return true
    }

    fun setOnDateChangeListener(l:OnDateChangeListener){
        listener = l
    }


    /**
     * 设置tvDate日期
     */
    fun setDate(time:Long){
        currentSelectTime = time
        val yesterdayCalendar = CalendarUtil.getYesterdayCalendar()
        val calendar = CalendarUtil.getCalendar(time)
        val isToday =  CalendarUtil.isToday(calendar)
        val isYesterday = CalendarUtil.isSameDay(calendar, yesterdayCalendar)
        if (isToday){
            tvDate.text = "今天"
        }else if (isYesterday){
            tvDate.text = "昨天"
        }else{
            tvDate.text = CalendarUtil.format("yyyy-MM-dd",calendar)
        }

    }

    interface OnDateChangeListener{
        fun onDateChange(date:Long)
    }
}