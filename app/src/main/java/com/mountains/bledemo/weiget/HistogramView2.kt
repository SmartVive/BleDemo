package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.mountains.bledemo.R
import com.mountains.bledemo.util.DisplayUtil
import com.orhanobut.logger.Logger

open class HistogramView2 : View {
    //x轴画笔
    private lateinit var xAxisPaint: Paint
    //x轴画笔颜色
    @ColorInt
    var axisColor:Int
    //x轴画笔宽度
    var axisWidth :Float

    //y轴辅助线
    private lateinit var guidePaint: Paint
    //y轴辅助线颜色
    @ColorInt
    var guideColor:Int
    //y轴辅助线宽度
    var guideWidth :Float

    //标签画笔
    private lateinit var labelPaint:Paint
    //标签颜色
    @ColorInt
    var labelColor :Int
    //标签字体大小
    var labelSize:Float
    //标签文体偏移
    private var labelTextOffset = 0f


    //条形画笔
    lateinit var barPaint: Paint
    //条形颜色
    @ColorInt
    var barColor:Int
    //条形左右边间距（以条形宽度的百分比为单位）
    protected var barSpace :Float

    //y轴标签个数
    var guideLabelCount :Int
    //x轴标签个数
    var xLabelCount :Int
    //条形数量，当多个数据在同一个时间段则会计算多个数据的平均值
    protected var barCount :Int
    //辅助线的最大最小值
    private var guideLabelMaximum :Int
    private var guideLabelMinimum :Int
    //自动设置辅助线最大最小值
    private var isGuideAutoLabel = true
    //最大值与轴上最大值的顶部间距（以最大值的百分比为单位）,只有当isGuideAutoLabel==true才生效
    private var guideLabelSpaceTop :Float
    //最小值与轴上最小值的底部间距（以最小值的百分比为单位）,只有当isGuideAutoLabel==true才生效
    private var guideLabelSpaceBottom :Float


    //分割线长度
    var xDividerHeight = 10f

    //x轴标签边距
    var xLabelMargin:Float

    var axisMarginLeft :Float
    var axisMarginRight :Float
    var axisMarginTop :Float
    var axisMarginBottom :Float

    //单位秒
    var xLabelBeginTime: Int
    var xLabelEndTime: Int

    //popup画笔
    private lateinit var popupPaint:Paint
    //popup文字画笔
    private lateinit var popupTextPaint:Paint
    @ColorInt
    var popupColor:Int = Color.parseColor("#f76955")
    var popupHeight = 80f
    @ColorInt
    var popupTextColor : Int = Color.WHITE
    var popupTextMargin: Float = 20f


    //条形数据
    protected var barData:FloatArray? = null






    //触摸位置
    private var touchX = -1f
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

    private val popupTextRect = Rect()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val attributeSet = context.obtainStyledAttributes(attrs, R.styleable.HistogramView2)
        axisColor = attributeSet.getColor(R.styleable.HistogramView2_HistogramView_axisColor,Color.DKGRAY)
        axisWidth = attributeSet.getDimension(R.styleable.HistogramView2_HistogramView_axisWidth,2f)
        guideColor = attributeSet.getColor(R.styleable.HistogramView2_HistogramView_guideColor,Color.LTGRAY)
        guideWidth = attributeSet.getDimension(R.styleable.HistogramView2_HistogramView_guideColor,2f)
        labelColor = attributeSet.getColor(R.styleable.HistogramView2_HistogramView_labelColor,Color.LTGRAY)
        labelSize = attributeSet.getDimension(R.styleable.HistogramView2_HistogramView_labelSize,DisplayUtil.dp2px(context, 12f).toFloat())
        barColor = attributeSet.getColor(R.styleable.HistogramView2_HistogramView_barColor, Color.parseColor("#f76955"))
        barSpace = attributeSet.getFloat(R.styleable.HistogramView2_HistogramView_barSpace,0.2f)
        guideLabelCount = attributeSet.getInt(R.styleable.HistogramView2_HistogramView_guideLabelCount,5)
        xLabelCount = attributeSet.getInt(R.styleable.HistogramView2_HistogramView_xLabelCount,7)
        barCount = attributeSet.getInt(R.styleable.HistogramView2_HistogramView_barCount,48)
        guideLabelMaximum = attributeSet.getInt(R.styleable.HistogramView2_HistogramView_guideLabelMaximum,100)
        guideLabelMinimum = attributeSet.getInt(R.styleable.HistogramView2_HistogramView_guideLabelMinimum,0)
        isGuideAutoLabel = attributeSet.getBoolean(R.styleable.HistogramView2_HistogramView_isGuideAutoLabel,true)
        guideLabelSpaceTop = attributeSet.getFloat(R.styleable.HistogramView2_HistogramView_guideLabelSpaceTop,0.1f)
        guideLabelSpaceBottom = attributeSet.getFloat(R.styleable.HistogramView2_HistogramView_guideLabelSpaceBottom,0.1f)
        xLabelMargin = attributeSet.getDimension(R.styleable.HistogramView2_HistogramView_xLabelMargin,DisplayUtil.dp2px(context, 10f).toFloat())

        axisMarginLeft = attributeSet.getDimension(R.styleable.HistogramView2_HistogramView_axisMarginLeft,DisplayUtil.dp2px(context,30f).toFloat())
        axisMarginRight = attributeSet.getDimension(R.styleable.HistogramView2_HistogramView_axisMarginRight,DisplayUtil.dp2px(context,30f).toFloat())
        axisMarginTop = attributeSet.getDimension(R.styleable.HistogramView2_HistogramView_axisMarginTop,DisplayUtil.dp2px(context,30f).toFloat())
        axisMarginBottom = attributeSet.getDimension(R.styleable.HistogramView2_HistogramView_axisMarginBottom,DisplayUtil.dp2px(context,30f).toFloat())
        xLabelBeginTime = attributeSet.getInt(R.styleable.HistogramView2_HistogramView_xLabelBeginTime,0)
        xLabelEndTime = attributeSet.getInt(R.styleable.HistogramView2_HistogramView_xLabelEndTime,86400)
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

        popupPaint = Paint()
        popupPaint.isAntiAlias = true
        popupPaint.strokeWidth = 2f
        popupPaint.color = popupColor
        popupPaint.style = Paint.Style.FILL

        popupTextPaint = Paint()
        popupTextPaint.isAntiAlias = true
        popupTextPaint.color = popupTextColor
        popupTextPaint.textSize = DisplayUtil.dp2px(context, 14f).toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xAxisStartX = axisMarginLeft
        xAxisStartY = measuredHeight - axisMarginBottom
        xAxisStopX = measuredWidth - axisMarginRight
        xAxisStopY = measuredHeight - axisMarginBottom

        xLabelStartX = xAxisStartX + xLabelMargin
        xLabelStopX = xAxisStopX - xLabelMargin

        xAxisWidth = xAxisStopY - xAxisStartX
        xLabelWidth = xLabelStopX - xLabelStartX
        xDivideDistance = xLabelWidth / (xLabelCount -1)
        yLabelHeight = measuredHeight - axisMarginBottom - axisMarginTop

        xBarDistance = xLabelWidth / (barCount)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE->{
                parent.requestDisallowInterceptTouchEvent(true)
                touchX = event.x
                //边界处理
                if (touchX <  xLabelStartX){
                    touchX = xLabelStartX
                }else if (touchX > xLabelStopX){
                    touchX = xLabelStopX
                }
            }
            MotionEvent.ACTION_UP->{
                parent.requestDisallowInterceptTouchEvent(false)
                touchX = -1f
            }
        }
        invalidate()
        return true
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

        //画popup
        if (touchX != -1f){
            canvas.drawLine(touchX,xAxisStopY,touchX,100f,popupPaint)


            val selectTime =  xLabelBeginTime + ((xLabelEndTime - xLabelBeginTime) / (xLabelStopX - xLabelStartX) * (touchX-xLabelStartX)).toLong()
            val index = getIndexByTime(selectTime) ?: return
            val value = barData!![index]
            val beginTime = timeFormat(getBarBeginTime(index))
            val endTime = timeFormat(getBarEndTime(index))
            val text = "$value bpm $beginTime-$endTime"
            //Logger.e(text)
            popupTextPaint.getTextBounds(text,0,text.length,popupTextRect)
            val textWidth = popupTextRect.width()
            val textHeight = popupTextRect.bottom - popupTextRect.top


            val popupWidth = textWidth + popupTextMargin*2
            val popupLeft :Float
            val popupRight :Float
            val popupTop = axisMarginTop / 2f
            val popupBottom = popupTop + popupHeight
            if (touchX - popupWidth / 2f < 0) {
                popupLeft = 0f
                popupRight = popupLeft + popupWidth
            }else if (touchX + popupWidth / 2f > measuredWidth){
                popupRight = measuredWidth.toFloat()
                popupLeft = popupRight - popupWidth
            }else{
                popupLeft  = touchX - popupWidth / 2f
                popupRight = touchX + popupWidth / 2f
            }

            val textX = popupLeft + popupTextMargin
            val textY = popupTop + (popupHeight) / 2f + textHeight/2f

            canvas.drawRoundRect(popupLeft,popupTop,popupRight,popupBottom,8f,8f,popupPaint)
            canvas.drawText(text,textX,textY,popupTextPaint)
        }
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
        return (xLabelEndTime - xLabelBeginTime) / (xLabelCount - 1) * index
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
    fun getBarBeginTime(index: Int):Int{
        return index * ((xLabelEndTime - xLabelBeginTime) / barCount)
    }

    /**
     * 获取条形的结束时间
     */
    fun getBarEndTime(index: Int):Int{
        return (index+1) * ((xLabelEndTime - xLabelBeginTime) / barCount)
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
     * 根据时间戳获取数据
     */
    private fun getIndexByTime(time:Long):Int?{
        barData?.forEachIndexed { index, value ->
            val beginTime = getBarBeginTime(index)
            val endTime = getBarEndTime(index)
            if(time in beginTime..endTime){
                return index
            }
        }
        return null
    }

    /**
     * 初始化条形数据
     */
    private fun initBarData(){
        barData = FloatArray(barCount)
        barData?.let {
            for (i in it.indices) {
                val beginTime = getBarBeginTime(i)
                val endTime = getBarEndTime(i)

                //多个数值用一个条形显示时，显示平均值
                var sumValue = 0f
                var count = 0
                for (data in datas) {
                    if (data.getHistogramTime() in beginTime until endTime) {
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