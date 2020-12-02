package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.mountains.bledemo.R
import com.mountains.bledemo.util.DisplayUtil

open class HistogramView2 : View {
    //x轴画笔
    private lateinit var xAxisPaint: Paint
    //x轴画笔颜色
    var axisColor = Color.DKGRAY
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
    //标签字体大小
    var labelSize:Float = 0f
    //标签文体偏移
    private var labelTextOffset = 0f


    //条形画笔
    lateinit var barPaint: Paint
    //条形颜色
    var barColor = Color.RED
    //条形左右边间距（以条形宽度的百分比为单位）
    protected var barSpace = 0.2f

    //y轴标签个数
    var guideLabelCount = 5
    //x轴标签个数
    var xLabelCount = 7
    //条形数量，当多个数据在同一个时间段则会计算多个数据的平均值
    protected var barCount = 48
    //辅助线的最大最小值
    private var guideLabelMaximum = 100
    private var guideLabelMinimum = 0
    //自动设置辅助线最大最小值
    private var isGuideAutoLabel = true
    //最大值与轴上最大值的顶部间距（以最大值的百分比为单位）,只有当isGuideAutoLabel==true才生效
    private var guideLabelSpaceTop = 0.1f
    //最小值与轴上最小值的底部间距（以最小值的百分比为单位）,只有当isGuideAutoLabel==true才生效
    private var guideLabelSpaceBottom = 0.1f


    //分割线长度
    var xDividerHeight = 10f

    //x轴标签边距
    var xLabelMarginLeft = 20f
    var xLabelMarginRight = 20f

    var axisMarginLeft = 80f
    var axisMarginRight = 80f
    var axisMarginTop = 80f
    var axisMarginBottom = 80f

    //单位秒
    var xLabelStartTime: Int = 0
    var xLabelEndTime: Int = 86400


    //条形数据
    protected var barData:FloatArray? = null







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


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val attributeSet = context.obtainStyledAttributes(attrs, R.styleable.HistogramView2)
        axisColor = attributeSet.getColor(R.styleable.HistogramView2_axisColor,Color.DKGRAY)
        axisWidth = attributeSet.getDimension(R.styleable.HistogramView2_axisWidth,2f)
        guideColor = attributeSet.getColor(R.styleable.HistogramView2_guideColor,Color.LTGRAY)
        guideWidth = attributeSet.getDimension(R.styleable.HistogramView2_guideColor,2f)
        labelColor = attributeSet.getColor(R.styleable.HistogramView2_labelColor,Color.LTGRAY)
        labelSize = attributeSet.getDimension(R.styleable.HistogramView2_labelSize,DisplayUtil.dp2px(context, 12f).toFloat())
        barColor = attributeSet.getColor(R.styleable.HistogramView2_barColor,Color.RED)
        barSpace = attributeSet.getFloat(R.styleable.HistogramView2_barSpace,0.2f)
        guideLabelCount = attributeSet.getInt(R.styleable.HistogramView2_guideLabelCount,5)
        xLabelCount = attributeSet.getInt(R.styleable.HistogramView2_xLabelCount,7)
        barCount = attributeSet.getInt(R.styleable.HistogramView2_barCount,48)
        guideLabelMaximum = attributeSet.getInt(R.styleable.HistogramView2_guideLabelMaximum,100)
        guideLabelMinimum = attributeSet.getInt(R.styleable.HistogramView2_guideLabelMinimum,0)
        isGuideAutoLabel = attributeSet.getBoolean(R.styleable.HistogramView2_isGuideAutoLabel,true)
        guideLabelSpaceTop = attributeSet.getFloat(R.styleable.HistogramView2_guideLabelSpaceTop,0.1f)
        guideLabelSpaceBottom = attributeSet.getFloat(R.styleable.HistogramView2_guideLabelSpaceBottom,0.1f)

        axisMarginLeft = attributeSet.getDimension(R.styleable.HistogramView2_axisMarginLeft,DisplayUtil.dp2px(context,30f).toFloat())
        axisMarginRight = attributeSet.getDimension(R.styleable.HistogramView2_axisMarginRight,DisplayUtil.dp2px(context,30f).toFloat())
        axisMarginTop = attributeSet.getDimension(R.styleable.HistogramView2_axisMarginTop,DisplayUtil.dp2px(context,30f).toFloat())
        axisMarginBottom = attributeSet.getDimension(R.styleable.HistogramView2_axisMarginBottom,DisplayUtil.dp2px(context,30f).toFloat())
        xLabelStartTime = attributeSet.getInt(R.styleable.HistogramView2_xLabelStartTime,0)
        xLabelEndTime = attributeSet.getInt(R.styleable.HistogramView2_xLabelEndTime,86400)
        attributeSet.recycle()
        init()
    }

    private fun init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        xAxisPaint = Paint()
        xAxisPaint.isAntiAlias = true
        xAxisPaint.color = axisColor
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
        labelPaint.textSize = labelSize
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
        xDivideDistance = xLabelWidth / (xLabelCount -1)
        yLabelHeight = measuredHeight - axisMarginBottom - axisMarginTop

        xBarDistance = xLabelWidth / (barCount)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画x轴
        drawXAxis(canvas)

        //画辅助线
        drawGuide(canvas)

        //画x轴标签分割线
        drawXDivider(canvas)

        //画x轴标签
        drawXLabel(canvas)

        //画辅助线标签
        drawGuideLabel(canvas)

        //画条线数据
        drawBar(canvas)
    }

    /**
     * 画x轴
     */
    fun drawXAxis(canvas: Canvas){
        canvas.drawLine(xAxisStartX,xAxisStartY,xAxisStopX,xAxisStopY,xAxisPaint)
    }

    /**
     * 画辅助线（虚线）
     */
    open fun drawGuide(canvas: Canvas){
        for(i in 1 until guideLabelCount){
            val startX = getGuideStartX()
            val startY = getGuideY(i)
            val stopX = getGuideStopX()
            val stopY = startY
            canvas.drawLine(startX,startY,stopX,stopY,guidePaint)
        }
    }

    /**
     * 画x轴分割线
     */
    open fun drawXDivider(canvas: Canvas){
        for (i in 0 until xLabelCount) {
            val startX = getXDividerStartX(i)
            val startY = getXDividerStartY()
            val endX = getXDividerStopX(i)
            val endY = getXDividerStopY()
            canvas.drawLine(startX, startY, endX, endY, xAxisPaint)
        }
    }

    /**
     * 画x轴标签
     */
    open fun drawXLabel(canvas: Canvas){
        for (i in 0 until xLabelCount) {
            val x = getXLabelX(i)
            val y = getXLabelY()
            canvas.drawText(getXLabelText(i), x, y, labelPaint)
        }
    }

    /**
     * 画辅助线标签
     */
    open fun drawGuideLabel(canvas: Canvas){
        for (i in 0 until guideLabelCount) {
            val x = getGuideLabelX()
            val y = getGuideLabelY(i)
            val value = getGuideLabelText(i)
            canvas.drawText(value, x, y, labelPaint)
        }
    }

    /**
     * 画条形数据
     */
    open fun drawBar(canvas: Canvas){
        for (i in 0 until barCount) {
            barData?.let {
                val avg = it[i]
                val top = getBarTop(avg)
                var left = getBarLeft(i)
                var right = getBarRight(i)
                val bottom = getBarBottom()

                left += (right - left) * barSpace
                right -= (right - left) * barSpace
                canvas.drawRect(left, top, right, bottom, barPaint)
            }
        }
    }


    /**
     * 获取辅助线startX
     */
    open fun getGuideStartX():Float{
        return xAxisStartX
    }

    /**
     * 获取辅助线stopX
     */
    open fun getGuideStopX():Float{
        return xAxisStopX
    }

    /**
     * 获取辅助线纵坐标
     */
    open fun getGuideY(index:Int):Float{
        return xAxisStartY - (measuredHeight - axisMarginTop - axisMarginBottom) / (guideLabelCount - 1) * index
    }


    /**
     * 获取x轴分割线startX
     */
    open fun getXDividerStartX(index: Int): Float {
        return index * xDivideDistance + xLabelStartX
    }

    /**
     * 获取x轴分割线startY
     */
    open fun getXDividerStartY(): Float {
        return xAxisStartY
    }


    /**
     * 获取x轴分割线stopX
     */
    open fun getXDividerStopX(index: Int): Float {
        return getXDividerStartX(index)
    }

    /**
     * 获取x轴分割线stopY
     */
    open fun getXDividerStopY(): Float {
        return getXDividerStartY() - xDividerHeight
    }

    /**
     * 获取x轴标签横坐标
     * 取两个bar中间横坐标
     */
    open fun getXLabelX(index: Int): Float {
        return xLabelStartX + index * xDivideDistance
    }

    /**
     * 获取x轴标签纵坐标
     */
    open fun getXLabelY(): Float {
        return xAxisStopY + axisMarginBottom/2
    }

    /**
     * 获取x轴标签文字
     */
    open fun getXLabelText(index: Int):String{
        return timeFormat(getXAxisLabelTime(index))
    }

    /**
     * 获取x轴标签时间
     */
    open fun getXAxisLabelTime(index:Int):Int{
        return (xLabelEndTime - xLabelStartTime) / (xLabelCount - 1) * index
    }


    /**
     * 获取y轴标签横坐标
     */
    open fun getGuideLabelX():Float{
        return axisMarginLeft/2f
    }

    /**
     * 获取y轴标签纵坐标
     */
    open fun getGuideLabelY(index: Int):Float{
        return  getGuideY(index)  - labelTextOffset
    }

    /**
     * 获取y轴标签文字
     */
    open fun getGuideLabelText(index: Int):String{
        //两个label相差的值
        val yLabelDiffer = (guideLabelMaximum - guideLabelMinimum) / (guideLabelCount - 1)
        return (guideLabelMinimum + yLabelDiffer * index).toString()
    }

    /**
     * 获取条形top
     */
    open fun getBarTop(value:Float):Float{
        val differ =  guideLabelMaximum - guideLabelMinimum
        return xAxisStopY - ((value-guideLabelMinimum)/differ)*yLabelHeight
    }

    /**
     * 获取条形left
     */
    open fun getBarLeft(index:Int):Float{
        return xLabelStartX + index * xBarDistance
    }

    /**
     * 获取条形Right
     */
    open fun getBarRight(index:Int):Float{
        return xLabelStartX + (index+1) * xBarDistance
    }

    /**
     * 获取条形Bottom
     */
    open fun getBarBottom():Float{
        return xAxisStopY
    }


    /**
     * 获取条形的开始时间
     */
    fun getBarStartTime(index: Int):Int{
        return index * ((xLabelEndTime - xLabelStartTime) / barCount)
    }

    /**
     * 获取条形的结束时间
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

    /**
     * 初始化辅助线标签
     */
    fun initGuideLabel(){
        if (isGuideAutoLabel && datas.isNotEmpty()){
            //遍历y轴数据，计算y轴最大值最小值
            val max = datas.maxBy { it.getHistogramValue() }!!.getHistogramValue()
            val min = datas.minBy { it.getHistogramValue() }!!.getHistogramValue()
            guideLabelMaximum = (max + (max * guideLabelSpaceTop)).toInt()
            guideLabelMinimum = (min - (min * guideLabelSpaceBottom)).toInt()
        }
    }

    var datas: MutableList<IHistogramData> = mutableListOf()
    fun loadData(data: List<IHistogramData>) {
        datas.clear()
        datas.addAll(data)

        initGuideLabel()
        initBarData()
        //initAxisMargin()
        postInvalidate()
    }
}