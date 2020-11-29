package com.mountains.bledemo.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import com.mountains.bledemo.R
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.util.DisplayUtil
import kotlinx.android.synthetic.main.fragment_calendar.*

class CalendarDialogFragment : DialogFragment() {
    private var selectTimeMillis:Long = System.currentTimeMillis()
    private var listener : OnCalendarSelectListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar,container,false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val screenWidth = DisplayUtil.getScreenWidth(context)
        val window = dialog!!.window!!
        window.attributes.windowAnimations = R.style.CalendarAnim;
        window.setGravity(Gravity.BOTTOM)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivNextMonth.setOnClickListener {
            calendarView.scrollToNext()
        }

        ivPreMonth.setOnClickListener {
            calendarView.scrollToPre()
        }



        setCalendarRange()
        setSelectCalendar()
        calendarView.setOnCalendarSelectListener(object : CalendarView.OnCalendarSelectListener{
            override fun onCalendarSelect(calendar: Calendar, isClick: Boolean) {
                tvDate.text = "${calendar.year}年${calendar.month}月"
                if (isClick){
                    listener?.onCalendarSelect(calendar.timeInMillis)
                    listener = null
                    dismiss()
                }
            }

            override fun onCalendarOutOfRange(calendar: Calendar?) {

            }

        })

        calendarView.setOnMonthChangeListener { year, month ->
            tvDate.text = "${year}年${month}月"
        }

    }

    private fun setSelectCalendar(){
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = selectTimeMillis
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        calendarView.scrollToCalendar(year, month, day, false)
        tvDate.text = "${year}年${month}月"
    }

    private fun setCalendarRange(){
        val currentCalendar = CalendarUtil.getCurrentCalendar()
        val maxYear = currentCalendar.get(java.util.Calendar.YEAR)
        val maxMonth = currentCalendar.get(java.util.Calendar.MONTH)+1
        val maxDay = currentCalendar.get(java.util.Calendar.DAY_OF_MONTH)
        calendarView.setRange(2000,1,1,maxYear,maxMonth,maxDay)
    }

    fun show(fragmentManager: FragmentManager, tag:String, selectTimeMillis:Long?, listener:OnCalendarSelectListener){
        selectTimeMillis?.let {
            this.selectTimeMillis= selectTimeMillis
        }
        this.listener = listener
        show(fragmentManager, tag)
    }

    interface OnCalendarSelectListener{
        fun onCalendarSelect(calendarTime:Long)
    }
}