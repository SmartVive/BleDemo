package com.mountains.bledemo.weiget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import com.mountains.bledemo.R

class SimpleHistogramView : BaseHistogramView<IHistogramData> {
    private var barData:FloatArray? = null

    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){

    }

    /**
     * 画条形数据
     */
    override fun drawBar(canvas: Canvas) {
        for (i in 0 until barCount) {
            barData?.let {
                val avg = it[i]
                val top = getBarY(avg)
                var left = getBarLeft(i)
                var right = getBarRight(i)
                val bottom = xAxisStopY

                left += (right - left) * barSpace
                right -= (right - left) * barSpace
                //canvas.drawRect(left, top, right, bottom, barPaint)
                val path =  Path()
                val radius = floatArrayOf(barTopLeftRadius,barTopLeftRadius,barTopRightRadius,
                    barTopRightRadius,barBottomLeftRadius,barBottomLeftRadius,barBottomRightRadius,barBottomRightRadius)
                path.addRoundRect(left, top, right, bottom,radius, Path.Direction.CW)
                canvas.drawPath(path,barPaint)
            }
        }
    }


    /**
     * popup文字
     */
    override fun getPopupText(selectTime: Long): String {
        val index = getIndexByTime(selectTime) ?: return "暂无记录"
        val beginTime = timeFormat(getBarBeginTime(index))
        val endTime = timeFormat(getBarEndTime(index))
        val value = barData!![index]
        val text: String = if (value.isNaN()) {
            "暂无记录 $beginTime-$endTime"
        } else {
            "${value.toInt()} $dataUnit $beginTime-$endTime"
        }
        return text
    }

   /* override fun getBarMaxValue(): Float? {
       return  barData?.filter { !it.isNaN() }?.max()
    }

    override fun getBarMinValue(): Float? {
        return barData?.filter { !it.isNaN() }?.min()
    }*/

    override fun getBarMaxValue(): Float? {
        return 100f
    }

    override fun getBarMinValue(): Float? {
        return 50f
    }

    /**
     * 初始化条形数据
     */
    override fun initBarData() {


        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 500
        valueAnimator.addUpdateListener {
            barData = FloatArray(barCount) {index->
                val beginTime = getBarBeginTime(index)
                val endTime = getBarEndTime(index)

                //多个数值用一个条形显示时，显示平均值
                //所在时间段的所有数据
                val filterList = dataList.filter { it.getHistogramTime() in beginTime until endTime }
                if (filterList.isEmpty()){
                    Float.NaN
                }else{
                    (filterList.sumBy { it.getHistogramValue() } / filterList.size) * it.animatedValue as Float
                }
            }
            postInvalidate()
        }
        valueAnimator.start()
    }



}