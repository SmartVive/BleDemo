package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet

class BloodPressureHistogramView2 : BaseHistogramView {
    private var dataList : MutableList<BloodPressureHistogramView.IBloodPressureHistogramData> = mutableListOf()
    private var barData : Array<FloatArray>? = null

    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    /*override fun drawPopup(canvas: Canvas) {
        //当未触摸时不显示
        if (touchX == -1f) {
            return
        }

        //当前选择的时间
        val selectTime =
            xLabelBeginTime + ((xLabelEndTime - xLabelBeginTime) / (xLabelStopX - xLabelStartX) * (touchX - xLabelStartX)).toLong()
        val index = getIndexByTime(selectTime) ?: return

        val avgBloodDiastolic = barData!![index][0]
        val avgBloodSystolic = barData!![index][1]
        val beginTime = timeFormat(getBarBeginTime(index))
        val endTime = timeFormat(getBarEndTime(index))

        val text: String = if (avgBloodDiastolic.isNaN() || avgBloodSystolic.isNaN()) {
            "暂无记录 $beginTime-$endTime"
        } else {
            "${avgBloodDiastolic.toInt()}/${avgBloodSystolic.toInt()} $dataUnit $beginTime-$endTime"
        }
        popupTextPaint.getTextBounds(text, 0, text.length, popupTextRect)
        val textWidth = popupTextRect.width()
        val textHeight = popupTextRect.height()

        //文字位置
        val textY = popupTop + (popupHeight) / 2f + textHeight / 2f
        val textX: Float
        //边界处理
        if (touchX - textWidth / 2f - popupTextMargin < 0) {
            textX = popupTextMargin
        } else if (touchX + textWidth / 2f + popupTextMargin > measuredWidth) {
            textX = measuredWidth - textWidth - popupTextMargin
        } else {
            textX = touchX - textWidth / 2f
        }

        //popup位置
        val popupLeft = textX - popupTextMargin
        val popupRight = textX + textWidth + popupTextMargin

        //画触摸提示线
        canvas.drawLine(touchX, xAxisStopY, touchX, popupBottom, popupPaint)
        //popup
        canvas.drawRoundRect(popupLeft, popupTop, popupRight, popupBottom, popupRadius, popupRadius, popupPaint)
        //数据文字
        canvas.drawText(text, textX, textY, popupTextPaint)
    }*/

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


    /**
     * 初始化条形数据
     */
    private fun initBarData() {
        barData = Array(barCount) {FloatArray(2)}
        barData?.let {
            for (i in it.indices) {
                val startTime = getBarBeginTime(i)
                val endTime = getBarEndTime(i)

                //多个数值用一个条形显示时，显示平均值
                var sumBloodDiastolic = 0f
                var sumBloodSystolic = 0f
                var count = 0
                for (data in dataList) {
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

    /**
     * 根据时间戳获取数据
     */
    private fun getIndexByTime(time: Long): Int? {
        barData?.forEachIndexed { index, value ->
            val beginTime = getBarBeginTime(index)
            val endTime = getBarEndTime(index)
            if (time in beginTime..endTime) {
                return index
            }
        }
        return null
    }


    fun loadBloodPressureData(data:List<BloodPressureHistogramView.IBloodPressureHistogramData>){
        dataList.clear()
        dataList.addAll(data)

        initBarData()
        postInvalidate()
    }

}