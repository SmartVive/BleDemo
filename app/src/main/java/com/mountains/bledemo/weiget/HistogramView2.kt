package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.mountains.bledemo.util.DisplayUtil

open class HistogramView2 : View {
    //x轴画笔
    private lateinit var xAxisPaint: Paint
    //x轴画笔颜色
    var xAxisColor = Color.DKGRAY
    //x轴画笔宽度
    var axisWidth = 2f

    //y轴辅助线
    private lateinit var guidePaint: Paint
    //y轴辅助线颜色
    var guideColor = Color.LTGRAY
    //y轴辅助线宽度
    var guideWidth = 2f

    //标签画笔
    private lateinit var labelPaint:Paint
    //标签颜色
    var labelColor = Color.DKGRAY
    //标签文体偏移
    var labelTextOffset = 0f


    //条形画笔
    lateinit var barPaint: Paint
    //条形颜色
    var barColor = Color.RED
    //条形左边间距（以条形宽度的百分比为单位）
    private var barSpaceLeft = 0.2f
    //条形右边间距（以条形宽度的百分比为单位）
    private var barSpaceRight = 0.2f

    //y轴标签个数
    var yAxisLabelCount = 5
    //x轴标签个数
    var xAxisLabelCount = 7
    //条形数量，当多个数据在同一个时间段则会计算多个数据的平均值
    private var barCount = 48

    //分割线长度
    var dividerLength = 10f

    //x轴标签边距
    var xLabelMarginLeft = 40f
    var xLabelMarginRight = 40f

    var axisMarginLeft = 80f
    var axisMarginRight = 80f
    var axisMarginTop = 80f
    var axisMarginBottom = 80f

    //单位秒
    var xLabelStartTime: Int = 0
    var xLabelEndTime: Int = 86400


    //条形数据
    private var barData:FloatArray? = null






    //x轴坐标
    private var xAxisStartX = 0f
    private var xAxisStartY = 0f
    private var xAxisStopX = 0f
    private var xAxisStopY = 0f

    //x轴坐标
    private var xLabelStartX = 0f
    private var xLabelStopX = 0f

    //x轴总长度
    private var xAxisWidth = 0f
    //y标签总高度
    private var yLabelHeight = 0f
    //x轴标签总长度
    private var xLabelWidth = 0f



    //x轴分割线两点距离
    private var xDivideDistance = 0f
    //x轴两个条形距离
    private var xBarDistance = 0f


    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        xAxisPaint = Paint()
        xAxisPaint.isAntiAlias = true
        xAxisPaint.color = xAxisColor
        xAxisPaint.strokeWidth = axisWidth

        guidePaint = Paint()
        guidePaint.isAntiAlias = true
        guidePaint.color = guideColor
        guidePaint.strokeWidth = guideWidth
        guidePaint.pathEffect = DashPathEffect(floatArrayOf(5f,5f),0f)

        labelPaint = Paint()
        labelPaint.isAntiAlias = true
        labelPaint.color = labelColor
        labelPaint.textAlign = Paint.Align.CENTER
        val fontMetrics = Paint.FontMetrics()
        labelPaint.textSize = DisplayUtil.dp2px(context, 12f).toFloat()
        labelPaint.getFontMetrics(fontMetrics)
        labelTextOffset = (fontMetrics.bottom + fontMetrics.top) / 2

        barPaint = Paint()
        barPaint.isAntiAlias = true
        barPaint.color = barColor
        barPaint.strokeWidth = 1f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xAxisStartX = axisMarginLeft
        xAxisStartY = measuredHeight - axisMarginBottom
        xAxisStopX = measuredWidth - axisMarginRight
        xAxisStopY = measuredHeight - axisMarginBottom

        xLabelStartX = xAxisStartX + xLabelMarginLeft
        xLabelStopX = xAxisStopX - xLabelMarginRight

        xAxisWidth = xAxisStopY - xAxisStartX
        xLabelWidth = xLabelStopX - xLabelStartX
        xDivideDistance = xLabelWidth / (xAxisLabelCount -1)
        yLabelHeight = measuredHeight - axisMarginBottom - axisMarginTop

        xBarDistance = xLabelWidth / (barCount)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画x轴
        canvas.drawLine(xAxisStartX,xAxisStartY,xAxisStopX,xAxisStopY,xAxisPaint)

        //画辅助线
        for(i in 1 until yAxisLabelCount){
            val startX = getGuideStartX()
            val startY = getGuideY(i)
            val stopX = getGuideStopX()
            val stopY = startY
            canvas.drawLine(startX,startY,stopX,stopY,guidePaint)
        }

        //画x轴标签分割线
        for (i in 0 until xAxisLabelCount) {
            val startX = getXDividerStartX(i)
            val startY = getXDividerStartY()
            val endX = getXDividerStopX(i)
            val endY = getXDividerStopY()
            canvas.drawLine(startX, startY, endX, endY, xAxisPaint)
        }

        //画x轴标签
        for (i in 0 until xAxisLabelCount) {
            val x = getXAxisLabelX(i)
            val y = getXAxisLabelY()
            canvas.drawText(getXAxisLabelText(i), x, y, labelPaint)
        }


        //画虚线标签
        for (i in 0 until yAxisLabelCount) {
            val x = getYAxisLabelX()
            val y = getYAxisLabelY(i)
            val value = getYAxisLabelText(i)
            canvas.drawText(value, x, y, labelPaint)
        }

        //画条线数据
        for (i in 0 until barCount) {
            barData?.let {
                val avg = it[i]
                val top = getBarTop(avg)
                var left = getBarLeft(i)
                var right = getBarRight(i)
                val bottom = getBarBottom()

                left += (right - left) * barSpaceLeft
                right -= (right - left) * barSpaceRight
                canvas.drawRect(left, top, right, bottom, barPaint)
            }
        }
    }

    /**
     * 获取辅助线startX
     */
    private fun getGuideStartX():Float{
        return xAxisStartX
    }

    /**
     * 获取辅助线stopX
     */
    private fun getGuideStopX():Float{
        return xAxisStopX
    }

    /**
     * 获取辅助线纵坐标
     */
    private fun getGuideY(index:Int):Float{
        return xAxisStartY - (measuredHeight - axisMarginTop - axisMarginBottom) / (yAxisLabelCount - 1) * index
    }


    /**
     * 获取x轴分割线startX
     */
    private fun getXDividerStartX(index: Int): Float {
        return index * xDivideDistance + xLabelStartX
    }

    /**
     * 获取x轴分割线startY
     */
    private fun getXDividerStartY(): Float {
        return xAxisStartY
    }


    /**
     * 获取x轴分割线stopX
     */
    private fun getXDividerStopX(index: Int): Float {
        return getXDividerStartX(index)
    }

    /**
     * 获取x轴分割线stopY
     */
    private fun getXDividerStopY(): Float {
        return getXDividerStartY() - dividerLength
    }

    /**
     * 获取x轴标签横坐标
     * 取两个bar中间横坐标
     */
    private fun getXAxisLabelX(index: Int): Float {
        return xLabelStartX + index * xDivideDistance
    }

    /**
     * 获取x轴标签纵坐标
     */
    private fun getXAxisLabelY(): Float {
        return xAxisStopY + axisMarginBottom/2
    }

    /**
     * 获取x轴标签文字
     */
    private fun getXAxisLabelText(index: Int):String{
        return timeFormat(getXAxisLabelTime(index))
    }

    /**
     * 获取x轴标签时间
     */
    private fun getXAxisLabelTime(index:Int):Int{
        return (xLabelEndTime - xLabelStartTime) / (xAxisLabelCount - 1) * index
    }


    /**
     * 获取y轴标签横坐标
     */
    private fun getYAxisLabelX():Float{
        return axisMarginLeft/2f
    }

    /**
     * 获取y轴标签纵坐标
     */
    private fun getYAxisLabelY(index: Int):Float{
        return  getGuideY(index)  - labelTextOffset
    }

    /**
     * 获取y轴标签文字
     */
    private fun getYAxisLabelText(index: Int):String{
        //两个label相差的值
        val yLabelDiffer = (100 - 0) / (yAxisLabelCount - 1)
        return (0 + yLabelDiffer * index).toString()
    }

    /**
     * 获取值为value的条形top
     */
    fun getBarTop(value:Float):Float{
        val yAxisRealLabelMax = 100
        val yAxisRealLabelMin = 0
        val differ =  yAxisRealLabelMax - yAxisRealLabelMin
        return xAxisStopY - ((value-yAxisRealLabelMin)/differ)*yLabelHeight
    }

    /**
     * 获取值为value的条形left
     */
    fun getBarLeft(index:Int):Float{
        val startTime = getBarStartTime(index)
        return xLabelStartX + index * xBarDistance
    }

    /**
     * 获取值为value的条形Right
     */
    fun getBarRight(index:Int):Float{
        val endTime = getBarEndTime(index)
        return xLabelStartX + (index+1) * xBarDistance
    }

    /**
     * 获取值为value的条形Right
     */
    fun getBarBottom():Float{
        return xAxisStopY
    }


    /**
     * 获取第index个条形的开始时间
     */
    fun getBarStartTime(index: Int):Int{
        return index * ((xLabelEndTime - xLabelStartTime) / barCount)
    }

    /**
     * 获取第index个条形的结束时间
     */
    fun getBarEndTime(index: Int):Int{
        return (index+1) * ((xLabelEndTime - xLabelStartTime) / barCount)
    }

    /**
     * 时间转换
     */
    private fun timeFormat(second: Int): String {
        var hourStr = (second / 60 / 60).toString()
        var minStr = (second / 60 % 60).toString()
        if (hourStr.length == 1) {
            hourStr = "0$hourStr"
        }
        if (minStr.length == 1) {
            minStr = "0$minStr"
        }
        return "$hourStr:$minStr"
    }


    /**
     * 初始化条形数据
     */
    private fun initBarData(){
        barData = FloatArray(barCount)
        barData?.let {
            for (i in it.indices) {
                val startTime = getBarStartTime(i)
                val endTime = getBarEndTime(i)

                //多个数值用一个条形显示时，显示平均值
                var sumValue = 0f
                var count = 0
                for (data in datas) {
                    if (data.getHistogramTime() in startTime until endTime) {
                        sumValue += data.getHistogramValue()
                        count++
                    }
                }
                it[i] = (sumValue/count)
            }
        }
    }


    var datas: MutableList<IHistogramData> = mutableListOf()
    fun loadData(data: List<IHistogramData>) {
        datas.clear()
        datas.addAll(data)

        //initYAxisLabel()
        initBarData()
        //initAxisMargin()
        postInvalidate()
    }
}