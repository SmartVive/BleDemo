package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

class BloodPressureHistogramView : HistogramView {
    constructor(context: Context?) : this(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){

    }

    override fun drawBar(canvas: Canvas) {
        for (i in 0 until getBarCount()) {
            barData?.let {
                val avg = it[i]
                //val y = yAxisBottom - yDivideDistance * ((avg - labelMin) / yLabelDistance)
                val top = getBarTop(avg)
                var left = getBarLeft(i)
                var right = getBarRight(i)
                var bottom = xAxisTop

                left += (right - left) * barSpaceLeft
                right -= (right - left) * barSpaceRight
                //Logger.e("$left,$right")
                canvas.drawRect(left, top, right, bottom, barPaint)
            }
        }
    }

    interface IBloodPressureHistogramData{
        fun getHistogramBloodDiastolic():Int
        fun getHistogramBloodSystolic():Int
        fun getHistogramTime():Long
    }
}