package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

class BloodPressureHistogramView : HistogramView {
    private var bloodPressureDatas : MutableList<IBloodPressureHistogramData> = mutableListOf()
    private var bloodPressureBarData : Array<FloatArray>? = null

    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){

    }

    override fun drawBar(canvas: Canvas) {
        for (i in 0 until barCount) {
            bloodPressureBarData?.let {
                val avgBloodDiastolic = it[i][0]
                val avgBloodSystolic = it[i][1]
                val top = getBarTop(avgBloodSystolic)
                var left = getBarLeft(i)
                var right = getBarRight(i)
                val bottom = getBarTop(avgBloodDiastolic)

                left += (right - left) * barSpace
                right -= (right - left) * barSpace
                canvas.drawRect(left, top, right, bottom, barPaint)
            }
        }
    }

    fun loadBloodPressureData(data:List<IBloodPressureHistogramData>){
        bloodPressureDatas.clear()
        bloodPressureDatas.addAll(data)

        //initYAxisLabel()
        initBloodPressureBarData()
        //initAxisMargin()
        postInvalidate()
    }

    fun initBloodPressureBarData(){
        bloodPressureBarData = Array(barCount) {FloatArray(2)}
        bloodPressureBarData?.let {
            for (i in it.indices) {
                val startTime = getBarBeginTime(i)
                val endTime = getBarEndTime(i)

                //多个数值用一个条形显示时，显示平均值
                var sumBloodDiastolic = 0f
                var sumBloodSystolic = 0f
                var count = 0
                for (data in bloodPressureDatas) {
                    if (data.getHistogramTime() in startTime until endTime) {
                        sumBloodDiastolic += data.getHistogramBloodDiastolic()
                        sumBloodSystolic += data.getHistogramBloodSystolic()
                        count++
                    }
                }
                it[i] = floatArrayOf(sumBloodDiastolic/count,sumBloodSystolic/count)
            }
        }
    }

    interface IBloodPressureHistogramData{
        fun getHistogramBloodDiastolic():Int
        fun getHistogramBloodSystolic():Int
        fun getHistogramTime():Long
    }
}