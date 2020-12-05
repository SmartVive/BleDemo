package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import com.mountains.bledemo.R

class SimpleHistogramView : BaseHistogramView {
    private val dataList = mutableListOf<IHistogramData>()
    private var barData:FloatArray? = null

    //自动设置辅助线最大最小值
    var isGuideAutoLabel : Boolean
    //最大值与轴上最大值的顶部间距（以最大值的百分比为单位）,只有当isGuideAutoLabel==true才生效
    var guideLabelSpaceTop: Float
    //最小值与轴上最小值的底部间距（以最小值的百分比为单位）,只有当isGuideAutoLabel==true才生效
    var guideLabelSpaceBottom: Float

    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        val attributeSet = context.obtainStyledAttributes(attrs, R.styleable.SimpleHistogramView)
        isGuideAutoLabel = attributeSet.getBoolean(R.styleable.SimpleHistogramView_SimpleHistogramView_isGuideAutoLabel, true)
        guideLabelSpaceTop = attributeSet.getFloat(R.styleable.SimpleHistogramView_SimpleHistogramView_guideLabelSpaceTop, 0.1f)
        guideLabelSpaceBottom = attributeSet.getFloat(R.styleable.SimpleHistogramView_SimpleHistogramView_guideLabelSpaceBottom, 0.1f)
        attributeSet.recycle()
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
     * 画popup
     */
   /* override fun drawPopup(canvas: Canvas) {
        //当未触摸时不显示
        if (touchX == -1f) {
            return
        }

        //当前选择的时间
        val selectTime = xLabelBeginTime + ((xLabelEndTime - xLabelBeginTime) / (xLabelStopX - xLabelStartX) * (touchX - xLabelStartX)).toLong()
        val index = getIndexByTime(selectTime) ?: return
        val text: String = getPopupText(index)
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


    /**
     * 初始化辅助线标签
     */
    fun initGuideLabel() {
        if (isGuideAutoLabel && dataList.isNotEmpty()) {
            //遍历y轴数据，计算y轴最大值最小值
            val max = dataList.maxBy { it.getHistogramValue() }!!.getHistogramValue()
            val min = dataList.minBy { it.getHistogramValue() }!!.getHistogramValue()
            guideLabelMaximum = (max + (max * guideLabelSpaceTop)).toInt()
            guideLabelMinimum = (min - (min * guideLabelSpaceBottom)).toInt()
        }
    }

    /**
     * 初始化条形数据
     */
    private fun initBarData() {
        barData = FloatArray(barCount)
        barData?.let {
            for (i in it.indices) {
                val beginTime = getBarBeginTime(i)
                val endTime = getBarEndTime(i)

                //多个数值用一个条形显示时，显示平均值
                var sumValue = 0f
                var count = 0
                for (data in dataList) {
                    if (data.getHistogramTime() in beginTime until endTime) {
                        sumValue += data.getHistogramValue()
                        count++
                    }
                }
                it[i] = (sumValue / count)
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

    fun loadData(data: List<IHistogramData>){
        dataList.clear()
        dataList.addAll(data)

        initGuideLabel()
        initBarData()
        postInvalidate()
    }


}