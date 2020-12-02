package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.mountains.bledemo.util.DisplayUtil

open class HistogramView : View {
    //y轴标签个数
    private var yAxisLabelCount = 5
    //x轴标签个数
    private var xAxisLabelCount = 7
    //x、y轴画笔
    lateinit var axisPaint: Paint
    //x、y轴宽度
    var axisWidth = 2f
    //x、y轴颜色
    var axisColor = Color.DKGRAY

    //分割线的长度
    var dividerLength = 10

    //xy轴边
    var axisMarginLeft = 60f
    var axisMarginRight = 60f
    var axisMarginTop = 60f
    var axisMarginBottom = 60f

    //条形数量，当多个数据在同一个时间段则会计算多个数据的平均值
    private var barCount = 48

    //y轴默认最大最小值
    private var yAxisMaximum = 100f
    private var yAxisMinimum = 0f
    private var isYAxisAutoLabel = true
    //最大值与轴上最大值的顶部间距（以最大值的百分比为单位）
    private var yAxisSpaceTop = 0.1f
    //最小值与轴上最小值的底部间距（以最小值的百分比为单位）
    private var yAxisSpaceBottom = 0.1f

    //条形左边间距（以条形宽度的百分比为单位）
    private var barSpaceLeft = 0.2f
    //条形右边间距（以条形宽度的百分比为单位）
    private var barSpaceRight = 0.2f

    //单位秒
    var xAxisStartTime: Int = 0
    var xAxisEndTime: Int = 86400

    var datas: MutableList<IHistogramData> = mutableListOf()
    
    private var barData:FloatArray? = null

    var textOffset = 0f

    /**
     * 条形画笔
     */
    lateinit var barPaint: Paint



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

        barPaint = Paint()
        barPaint.isAntiAlias = true
        barPaint.setColor(Color.RED)
        barPaint.strokeWidth = 1f


        /*for (i in 0 .. 60){
            val random = Random()
            val value = (random.nextDouble() * 100 + 30).toInt()
            val histogramEntity = HistogramEntity(value, i * 1440L)
            datas.add(histogramEntity)
        }*/

        initAxisMargin()
    }

    private fun initAxisMargin(){
        val fontMetrics = axisPaint.fontMetrics
        axisMarginLeft = axisPaint.measureText(getYAxisLabelText(yAxisLabelCount-1)) * 1.3f
        axisMarginBottom = (fontMetrics.descent - fontMetrics.ascent) * 1.5f
        axisMarginTop = axisMarginBottom
        axisMarginRight = axisMarginLeft
    }

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
        drawXAxis(canvas)
        //画y轴
        drawYAxis(canvas)
        //画x轴分割线
        drawXAxisDivider(canvas)
        //画x轴标签
        drawXAxisLabel(canvas)
        //画y轴分割线
        drawYAxisDivider(canvas)
        //画y轴标签
        drawYAxisLabel(canvas)
        //画条形数据
        drawBar(canvas)
    }

    /**
     * 画x轴
     */
    open fun drawXAxis(canvas: Canvas) {
        canvas.drawLine(xAxisLeft, xAxisTop, xAxisRight, xAxisTop, axisPaint)
    }

    /**
     * 画y轴
     */
    open fun drawYAxis(canvas: Canvas) {
        canvas.drawLine(yAxisLeft, yAxisTop, yAxisLeft, yAxisBottom, axisPaint)
    }

    /**
     * 画x轴分割线
     */
    open fun drawXAxisDivider(canvas: Canvas){
        for (i in 0 until xAxisLabelCount) {
            val startX = getXAxisDividerStartX(i)
            val startY = getXAxisDividerStartY()
            val endX = getXAxisDividerEndX(i)
            val endY = getXAxisDividerEndY()
            canvas.drawLine(startX, startY, endX, endY, axisPaint)
        }
    }

    /**
     * 画x轴标签
     */
    open fun drawXAxisLabel(canvas: Canvas){
        for (i in 0 until xAxisLabelCount) {
            val x = getXAxisLabelX(i)
            val y = getXAxisLabelY()
            canvas.drawText(getXAxisLabelText(i), x, y, axisPaint)
        }
    }

    /**
     * 画y轴分割线
     */
    open fun drawYAxisDivider(canvas: Canvas){
        for (i in 0 until yAxisLabelCount) {
            val startX = getYAxisDividerStartX()
            val startY = getYAxisDividerStartY(i)
            val endX = getYAxisDividerEndX()
            val endY = getYAxisDividerEndY(i)
            canvas.drawLine(startX, startY, endX, endY, axisPaint)
        }
    }

    /**
     * 画y轴标签
     */
    open fun drawYAxisLabel(canvas: Canvas){
        for (i in 0 until yAxisLabelCount) {
            val x = getYAxisLabelX()
            val y = getYAxisLabelY(i)
            val value = getYAxisLabelText(i)
            canvas.drawText(value, x, y, axisPaint)
        }
    }

    /**
     * 画条形数据
     */
    open fun drawBar(canvas: Canvas){
        for (i in 0 until barCount) {
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
        isYAxisAutoLabel = isAuto
    }

    /**
     * 设置y轴最小值
     */
    fun setYAxisMinValue(minValue: Float) {
        yAxisMinimum = minValue
    }

    /**
     * 设置y轴最大值
     */
    fun setYAxisMaxValue(maxValue: Float) {
        yAxisMaximum = maxValue
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

    /**
     * 获取条形数量
     */
    fun getBarCount():Int{
        return barCount
    }


    private fun secondFormatHour(second: Int): String {
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
     * 获取x轴第index个标签横坐标
     * 取两个分割线中间横坐标
     */
    private fun getXAxisLabelX(index: Int): Float {
        return xAxisLeft + index * xDivideDistance + 0.5f * xValueDistance
    }

    /**
     * 获取x轴标签纵坐标
     */
    private fun getXAxisLabelY(): Float {
        return xAxisTop + axisMarginBottom/2 - textOffset
    }

    /**
     * 获取x轴第index个标签文字
     */
    private fun getXAxisLabelText(index: Int):String{
        return secondFormatHour(getXAxisLabelTime(index))
    }

    /**
     * 获取x轴第index个标签时间
     */
    private fun getXAxisLabelTime(index:Int):Int{
        return (xAxisEndTime - xAxisStartTime) / (xAxisLabelCount - 1) *index
    }


    /**
     * 获取y轴标签横坐标
     */
    private fun getYAxisLabelX():Float{
        return axisMarginLeft/2f
    }

    /**
     * 获取y轴第index个标签纵坐标
     */
    private fun getYAxisLabelY(index: Int):Float{
        return  yAxisBottom - index * yDivideDistance - textOffset
    }

    /**
     * 获取y轴第index个标签文字
     */
    private fun getYAxisLabelText(index: Int):String{
        //两个label相差的值
        val yLabelDiffer = (getYAxisRealLabelMax() - getYAxisRealLabelMin()) / (yAxisLabelCount - 1)
        return (getYAxisRealLabelMin() + yLabelDiffer * index).toString()
    }

    /**
     * 获取x轴第index个分割线startX
     */
    private fun getXAxisDividerStartX(index: Int): Float {
        return index * xDivideDistance + xAxisLeft
    }

    /**
     * 获取x轴分割线startY
     */
    private fun getXAxisDividerStartY(): Float {
        return xAxisTop
    }


    /**
     * 获取x轴第index个分割线endX
     * 和XAxisDividerStartX一致
     */
    private fun getXAxisDividerEndX(index: Int): Float {
        return getXAxisDividerStartX(index)
    }

    /**
     * 获取x轴分割线endY
     */
    private fun getXAxisDividerEndY(): Float {
        return getXAxisDividerStartY() - dividerLength
    }

    /**
     * 获取y轴第index个分割线startY
     */
    private fun getYAxisDividerStartY(index: Int): Float {
        return yAxisBottom - index * yDivideDistance
    }

    /**
     * 获取y轴分割线startX
     */
    private fun getYAxisDividerStartX(): Float {
        return yAxisLeft
    }


    /**
     * 获取y轴第index个分割线endX
     * 和yAxisDividerEndX一致
     */
    private fun getYAxisDividerEndY(index: Int): Float {
        return getYAxisDividerStartY(index)
    }

    /**
     * 获取y轴分割线endY
     */
    private fun getYAxisDividerEndX(): Float {
        return getYAxisDividerStartX() + dividerLength
    }


    /**
     * 初始化y轴标签值
     */
    private fun initYAxisLabel(){
        var max = Float.MIN_VALUE
        var min = Float.MAX_VALUE
        if (isYAxisAutoLabel && datas.isNotEmpty()){
            //遍历y轴数据，计算y轴最大值最小值
            for (data in datas) {
                max = Math.max(max, data.getHistogramValue().toFloat())
                min = Math.min(min, data.getHistogramValue().toFloat())
            }
            yAxisMaximum = max
            yAxisMinimum = min
        }
    }

    fun getYAxisRealLabelMax():Int{
        return (yAxisMaximum + yAxisMaximum*yAxisSpaceTop).toInt()
    }

    fun getYAxisRealLabelMin():Int{
        return (yAxisMinimum - yAxisMinimum*yAxisSpaceBottom).toInt()
    }


    /**
     * 获取第index个条形的开始时间
     */
    fun getBarStartTime(index: Int):Int{
        return index * ((xAxisEndTime - xAxisStartTime) / barCount)
    }

    /**
     * 获取第index个条形的结束时间
     */
    fun getBarEndTime(index: Int):Int{
        return (index+1) * ((xAxisEndTime - xAxisStartTime) / barCount)
    }

    /**
     * 获取值为value的条形top
     */
    fun getBarTop(value:Float):Float{
        val yAxisRealLabelMax = getYAxisRealLabelMax()
        val yAxisRealLabelMin = getYAxisRealLabelMin()
        val differ =  yAxisRealLabelMax - yAxisRealLabelMin
        return yAxisBottom - ((value-yAxisRealLabelMin)/differ)*yAxisHeight
    }

    /**
     * 获取值为value的条形left
     */
    fun getBarLeft(index:Int):Float{
        val startTime = getBarStartTime(index)
        return xAxisLeft + startTime * xValueDistance
    }

    /**
     * 获取值为value的条形Right
     */
    fun getBarRight(index:Int):Float{
        val endTime = getBarEndTime(index)
        return xAxisLeft + endTime * xValueDistance
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

    fun loadData(data: List<IHistogramData>) {
        datas.clear()
        datas.addAll(data)

        initYAxisLabel()
        initBarData()
        initAxisMargin()
        postInvalidate()
    }


}