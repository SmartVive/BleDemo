package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.mountains.bledemo.util.DisplayUtil

class HistogramView : View {
    //y轴标签个数
    private var yAxisLabelCount = 7
    //x轴标签个数
    private var xAxisLabelCount = 7
    //x、y轴画笔
    lateinit var axisPaint: Paint
    //x、y轴宽度
    var axisWidth = 2f
    //x、y轴颜色
    var axisColor = Color.DKGRAY

    //xy轴边
    var axisMarginLeft = 60f
    var axisMarginRight = 60f
    var axisMarginTop = 60f
    var axisMarginBottom = 60f

    //条形数量，当多个数据在同一个时间段则会计算多个数据的平均值
    private var barCount = 48

    //y轴默认最大最小值
    private var yAxisMaxValue = 100f
    private var yAxisMinValue = 0f
    private var isYAxisAutoValue = true
    //最大值与轴上最大值的顶部间距（以最大值的百分比为单位）
    private var yAxisSpaceTop = 0.1f
    //最小值与轴上最小值的底部间距（以最小值的百分比为单位）
    private var yAxisSpaceBottom = 0.1f

    //单位秒
    var xAxisStartTime: Float = 0f
    var xAxisEndTime: Float = 86400f

    var datas: MutableList<IHistogramData> = mutableListOf()

    var textOffset = 0f

    lateinit var valuePaint: Paint

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        axisPaint = Paint()
        axisPaint.isAntiAlias = true
        axisPaint.setColor(axisColor)
        axisPaint.strokeWidth = axisWidth
        axisPaint.textSize = DisplayUtil.dp2px(context, 12f).toFloat()
        axisPaint.textAlign = Paint.Align.CENTER
        val fontMetrics = Paint.FontMetrics()
        axisPaint.getFontMetrics(fontMetrics)
        textOffset = (fontMetrics.descent + fontMetrics.ascent) / 2

        valuePaint = Paint()
        valuePaint.isAntiAlias = true
        valuePaint.setColor(Color.RED)
        valuePaint.strokeWidth = 1f


        /*for (i in 0 .. 60){
            val random = Random()
            val value = (random.nextDouble() * 100 + 30).toInt()
            val histogramEntity = HistogramEntity(value, i * 1440L)
            datas.add(histogramEntity)
        }*/

        axisMarginLeft = axisPaint.measureText("9999")
        axisMarginBottom = (fontMetrics.descent - fontMetrics.ascent) * 2
        axisMarginTop = axisMarginBottom
        axisMarginRight = axisMarginLeft
    }


    var xAxisLeft: Float = 0f
    var xAxisRight: Float = 0f
    var xAxisTop: Float = 0f

    var yAxisLeft: Float = 0f
    var yAxisTop: Float = 0f
    var yAxisBottom: Float = 0f

    //x轴长度
    var xAxisWidth = 0f
    //y轴长度
    var yAxisHeight = 0f

    //x轴分割线两点距离
    var xDivideDistance = 0f
    //y轴分割线两点距离
    var yDivideDistance = 0f

    //x轴两个变量距离
    var xValueDistance = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xAxisLeft = axisMarginLeft
        xAxisRight = measuredWidth - axisMarginRight
        xAxisTop = measuredHeight - axisMarginBottom

        yAxisLeft = axisMarginLeft
        yAxisTop = axisMarginTop
        yAxisBottom = measuredHeight - axisMarginBottom

        xAxisWidth = xAxisRight - xAxisLeft

        yAxisHeight = yAxisBottom - yAxisTop

        //x轴分割线两点距离
        xDivideDistance = xAxisWidth / (xAxisLabelCount - 1)
        //y轴分割线两点距离
        yDivideDistance = yAxisHeight / (yAxisLabelCount - 1)

        //x轴两个变量距离
        xValueDistance = xAxisWidth / (xAxisEndTime - xAxisStartTime)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //画x轴
        canvas.drawLine(xAxisLeft, xAxisTop, xAxisRight, xAxisTop, axisPaint)

        //画y轴
        canvas.drawLine(yAxisLeft, yAxisTop, yAxisLeft, yAxisBottom, axisPaint)

        //画x轴分割线
        for (i in 0 until xAxisLabelCount) {
            val startX = getXAxisDivideX(i)
            val startY = xAxisTop - 10
            val endX = startX
            val endY = xAxisTop
            canvas.drawLine(startX, startY, endX, endY, axisPaint)
        }

        //画x轴标签
        for (i in 0 until xAxisLabelCount) {
            val x = getXAxisLabelX(i)
            val y = xAxisTop + axisMarginBottom/2 - textOffset
            val second = (xAxisEndTime - xAxisStartTime) / (xAxisLabelCount - 1) * i
            canvas.drawText(second2Hour(second.toInt()), x, y, axisPaint)
        }


        //画y轴分割线
        for (i in 0 until yAxisLabelCount) {
            val startX = yAxisLeft
            val startY = yAxisBottom - i * yDivideDistance
            val endX = startX + 10
            val endY = startY
            canvas.drawLine(startX, startY, endX, endY, axisPaint)
        }


        //遍历y轴数据，计算y轴最大值最小值
        var max: Float
        var min: Float
        if (isYAxisAutoValue && datas.isNotEmpty()) {
            //自动测量y轴最大最小值
            max = Float.MIN_VALUE
            min = Float.MAX_VALUE

            for (data in datas) {
                max = Math.max(max, data.getHistogramValue().toFloat())
                min = Math.min(min, data.getHistogramValue().toFloat())
            }
        } else {
            max = yAxisMaxValue
            min = yAxisMinValue
        }

        max += max * yAxisSpaceTop
        min -= min * yAxisSpaceBottom


        //y值数据间隔
        val yLabelDistance = (max - min) / (yAxisLabelCount - 1)
        //画y轴标签
        for (i in 0 until yAxisLabelCount) {
            val x = axisMarginLeft/2f
            val y = yAxisBottom - i * yDivideDistance - textOffset
            val value = (yLabelDistance * i + min).toInt().toString()
            canvas.drawText(value, x, y, axisPaint)
        }


        //画条形数据
        for (i in 0 until barCount) {
            val startTime = i * ((xAxisEndTime - xAxisStartTime) / barCount)
            val endTime = (i + 1) * ((xAxisEndTime - xAxisStartTime) / barCount)

            var sumValue = 0f
            var count = 0
            for (data in datas) {
                if (startTime <= data.getHistogramTime() && endTime > data.getHistogramTime()) {
                    sumValue += data.getHistogramValue()
                    count++
                }
            }

            //多个数值用一条线显示时，显示平均值
            val value = sumValue / count
            val y = yAxisBottom - yDivideDistance * ((value - min) / yLabelDistance)
            var left = xAxisLeft + startTime * xValueDistance
            var right = xAxisLeft + endTime * xValueDistance

            left += (right - left) * 0.3f
            right -= (right - left) * 0.3f
            //Logger.e("$left,$right")
            canvas.drawRect(left, y, right, xAxisTop, valuePaint)
        }

    }

    /**
     * y轴标签个数
     */
    fun setYAxisLabelCount(count:Int){
        yAxisLabelCount = count
    }

    /**
     * x轴标签个数
     */
    fun setXAxisLabelCount(count:Int){
        xAxisLabelCount = count
    }

    /**
     * y值是否自动计算
     */
    fun setAutoYAxisValue(isAuto: Boolean) {
        isYAxisAutoValue = isAuto
    }

    /**
     * 设置y轴最小值
     */
    fun setYAxisMinValue(minValue: Float) {
        yAxisMinValue = minValue
    }

    /**
     * 设置y轴最大值
     */
    fun setYAxisMaxValue(maxValue: Float) {
        yAxisMaxValue = maxValue
    }

    /**
     * 设置最大值与轴上最大值的顶部间距（以最高值的百分比为单位）
     */
    fun yAxisSpaceTop(spaceTop:Float){
        yAxisSpaceTop = yAxisTop
    }

    /**
     * 设置最小值与轴上最小值的顶部间距（以最高值的百分比为单位）
     */
    fun yAxisSpaceBottom(spaceBottom:Float){
        yAxisSpaceBottom = spaceBottom
    }

    /**
     * 设置条形数量
     */
    fun setBarCount(count: Int){
        barCount = count
    }

    private fun second2Hour(second: Int): String {
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

    /**获取x轴第num个变量横坐标
     * 取两个分割线中间横坐标
     */
    private fun getXAxisLabelX(num: Int): Float {
        return xAxisLeft + num * xDivideDistance + 0.5f * xValueDistance
    }


    /**
     * 获取第num个分割线横坐标
     */
    private fun getXAxisDivideX(num: Int): Float {
        return num * xDivideDistance + xAxisLeft
    }

    fun loadData(data: List<IHistogramData>) {
        datas.clear()
        datas.addAll(data)
        postInvalidate()
    }


}