package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet

class BloodPressureHistogramView2 : BaseHistogramView<BloodPressureHistogramView.IBloodPressureHistogramData> {
    private var barData : Array<FloatArray>? = null

    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    /**
     * popup文字
     */
    override fun getPopupText(selectTime: Long): String {
        val index = getIndexByTime(selectTime) ?: return "暂无记录"

        val avgBloodDiastolic = barData!![index][0]
        val avgBloodSystolic = barData!![index][1]
        val beginTime = timeFormat(getBarBeginTime(index))
        val endTime = timeFormat(getBarEndTime(index))

        val text: String = if (avgBloodDiastolic.isNaN() || avgBloodSystolic.isNaN()) {
            "暂无记录 $beginTime-$endTime"
        } else {
            "${avgBloodDiastolic.toInt()}/${avgBloodSystolic.toInt()} $dataUnit $beginTime-$endTime"
        }
        return text
    }


    override fun drawBar(canvas: Canvas) {
        for (i in 0 until barCount) {
            barData?.let {
                val avgBloodDiastolic = it[i][0]
                val avgBloodSystolic = it[i][1]
                val top = getBarY(avgBloodSystolic)
                var left = getBarLeft(i)
                var right = getBarRight(i)
                val bottom = getBarY(avgBloodDiastolic)

                left += (right - left) * barSpace
                right -= (right - left) * barSpace
                val path =  Path()
                val radius = floatArrayOf(barTopLeftRadius,barTopLeftRadius,barTopRightRadius,
                    barTopRightRadius,barBottomLeftRadius,barBottomLeftRadius,barBottomRightRadius,barBottomRightRadius)
                path.addRoundRect(left, top, right, bottom,radius, Path.Direction.CW)
                canvas.drawPath(path,barPaint)
            }
        }
    }

    override fun getBarMaxValue(): Float? {
        return barData?.maxBy { it[1] }?.get(1)
    }

    override fun getBarMinValue(): Float? {
        return barData?.minBy { it[0] }?.get(0)
    }


    /**
     * 初始化条形数据
     */
    override fun initBarData() {
        barData = Array(barCount) {index->
            val startTime = getBarBeginTime(index)
            val endTime = getBarEndTime(index)

            //多个数值用一个条形显示时，显示平均值
            //所在时间段的所有数据
            val filterList = dataList.filter { it.getHistogramTime() in startTime until endTime }
            if (filterList.isEmpty()){
                floatArrayOf(Float.NaN,Float.NaN)
            }else{
                val avgBloodDiastolic = (filterList.sumBy { it.getHistogramBloodDiastolic() } / filterList.size).toFloat()
                val avgBloodSystolic = (filterList.sumBy { it.getHistogramBloodSystolic() } / filterList.size).toFloat()
                floatArrayOf(avgBloodDiastolic,avgBloodSystolic)
            }

        }
    }

}