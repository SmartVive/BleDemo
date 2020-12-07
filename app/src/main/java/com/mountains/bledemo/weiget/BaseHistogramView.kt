package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.mountains.bledemo.R

abstract class BaseHistogramView<T> : View {
    protected val dataList = mutableListOf<T>()

    //x轴画笔
    protected lateinit var xAxisPaint: Paint

    //x轴画笔颜色
    @ColorInt
    var axisColor: Int

    //x轴画笔宽度
    var axisWidth: Float

    //y轴辅助线
    protected lateinit var guidePaint: Paint

    //y轴辅助线颜色
    @ColorInt
    var guideColor: Int

    //y轴辅助线宽度
    var guideWidth: Float

    //自动设置辅助线最大最小值
    var isGuideAutoLabel : Boolean
    //最大值与轴上最大值的顶部间距（以最大值的百分比为单位）,只有当isGuideAutoLabel==true才生效
    var guideLabelSpaceTop: Float
    //最小值与轴上最小值的底部间距（以最小值的百分比为单位）,只有当isGuideAutoLabel==true才生效
    var guideLabelSpaceBottom: Float


    //标签画笔
    protected lateinit var labelPaint: Paint

    //标签颜色
    @ColorInt
    var labelColor: Int

    //标签字体大小
    var labelSize: Float

    //标签文体偏移
    private var labelTextOffset = 0f


    //条形画笔
    protected lateinit var barPaint: Paint

    //条形颜色
    @ColorInt
    var barColor: Int

    //条形左右边间距（以条形宽度的百分比为单位）
    protected var barSpace: Float

    //bar圆角
    var barTopLeftRadius: Float
    var barTopRightRadius: Float
    var barBottomLeftRadius: Float
    var barBottomRightRadius: Float

    //y轴标签个数
    var guideLabelCount: Int

    //x轴标签个数
    var xLabelCount: Int

    //条形数量，当多个数据在同一个时间段则会计算多个数据的平均值
    protected var barCount: Int

    //辅助线的最大最小值
    var guideLabelMaximum: Int
    var guideLabelMinimum: Int


    //分割线长度
    var xDividerHeight = 10f

    //x轴标签边距
    var xLabelMargin: Float

    var axisMarginLeft: Float
    var axisMarginRight: Float
    var axisMarginTop: Float
    var axisMarginBottom: Float

    //单位秒
    var xLabelBeginTime: Int
    var xLabelEndTime: Int

    //popup画笔
    protected lateinit var popupPaint: Paint

    //popup文字画笔
    protected lateinit var popupTextPaint: Paint
    var popupTextSize:Float

    @ColorInt
    var popupColor: Int
    var popupHeight: Float

    @ColorInt
    var popupTextColor: Int
    var popupTextMargin: Float

    //popup圆角
    var popupRadius: Float

    //弹窗显示的单位
    var dataUnit: String






    //触摸位置
    protected var touchX = -1f

    //x轴坐标
    protected var xAxisStartX = 0f
    protected var xAxisStartY = 0f
    protected var xAxisStopX = 0f
    protected var xAxisStopY = 0f

    //x轴坐标
    protected var xLabelStartX = 0f
    protected var xLabelStopX = 0f

    //x轴总长度
    protected var xAxisWidth = 0f

    //y标签总高度
    protected var yLabelHeight = 0f

    //x轴标签总长度
    protected var xLabelWidth = 0f

    //popup位置
    protected var popupTop = 0f
    protected var popupBottom = 0f


    //x轴分割线两点距离
    private var xDivideDistance = 0f

    //x轴两个条形距离
    private var xBarDistance = 0f

    protected val popupTextRect = Rect()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val attributeSet = context.obtainStyledAttributes(attrs, R.styleable.HistogramView)
        axisColor = attributeSet.getColor(R.styleable.HistogramView_HistogramView_axisColor, Color.DKGRAY)
        axisWidth = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_axisWidth, 2f)
        guideColor = attributeSet.getColor(R.styleable.HistogramView_HistogramView_guideColor, Color.LTGRAY)
        guideWidth = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_guideWidth, 2f)
        labelColor = attributeSet.getColor(R.styleable.HistogramView_HistogramView_labelColor, Color.LTGRAY)
        labelSize = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_labelSize, dp2px(12f))
        barColor = attributeSet.getColor(R.styleable.HistogramView_HistogramView_barColor, Color.parseColor("#f76955"))
        barSpace = attributeSet.getFloat(R.styleable.HistogramView_HistogramView_barSpace, 0.2f)
        guideLabelCount = attributeSet.getInt(R.styleable.HistogramView_HistogramView_guideLabelCount, 5)
        xLabelCount = attributeSet.getInt(R.styleable.HistogramView_HistogramView_xLabelCount, 7)
        barCount = attributeSet.getInt(R.styleable.HistogramView_HistogramView_barCount, 48)
        guideLabelMaximum = attributeSet.getInt(R.styleable.HistogramView_HistogramView_guideLabelMaximum, 100)
        guideLabelMinimum = attributeSet.getInt(R.styleable.HistogramView_HistogramView_guideLabelMinimum, 0)
        isGuideAutoLabel = attributeSet.getBoolean(R.styleable.HistogramView_HistogramView_isGuideAutoLabel, true)
        guideLabelSpaceTop = attributeSet.getFloat(R.styleable.HistogramView_HistogramView_guideLabelSpaceTop, 0.1f)
        guideLabelSpaceBottom = attributeSet.getFloat(R.styleable.HistogramView_HistogramView_guideLabelSpaceBottom, 0.1f)
        xLabelMargin = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_xLabelMargin, dp2px(10f))
        axisMarginLeft = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_axisMarginLeft, dp2px(30f))
        axisMarginRight = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_axisMarginRight, dp2px(30f))
        axisMarginTop = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_axisMarginTop, dp2px(30f))
        axisMarginBottom = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_axisMarginBottom, dp2px(30f))
        xLabelBeginTime = attributeSet.getInt(R.styleable.HistogramView_HistogramView_xLabelBeginTime, 0)
        xLabelEndTime = attributeSet.getInt(R.styleable.HistogramView_HistogramView_xLabelEndTime, 86400)
        dataUnit = attributeSet.getString(R.styleable.HistogramView_HistogramView_dataUnit) ?: ""
        popupColor = attributeSet.getColor(R.styleable.HistogramView_HistogramView_popupColor, Color.parseColor("#f76955"))
        popupHeight = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_popupHeight, dp2px(36f))
        popupTextColor = attributeSet.getColor(R.styleable.HistogramView_HistogramView_popupTextColor, Color.WHITE)
        popupTextMargin = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_popupTextMargin, dp2px(10f))
        popupRadius = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_popupRadius, dp2px(8f))
        popupTextSize = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_popupTextSize,dp2px(14f))
        barTopLeftRadius = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_barTopLeftRadius,0f)
        barTopRightRadius = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_barTopRightRadius,0f)
        barBottomLeftRadius = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_barBottomLeftRadius,0f)
        barBottomRightRadius = attributeSet.getDimension(R.styleable.HistogramView_HistogramView_barBottomRightRadius,0f)
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
        guidePaint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)

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

        popupPaint = Paint()
        popupPaint.isAntiAlias = true
        popupPaint.strokeWidth = 2f
        popupPaint.color = popupColor
        popupPaint.style = Paint.Style.FILL

        popupTextPaint = Paint()
        popupTextPaint.isAntiAlias = true
        popupTextPaint.color = popupTextColor
        popupTextPaint.textSize = popupTextSize
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
        xDivideDistance = xLabelWidth / (xLabelCount - 1)
        yLabelHeight = measuredHeight - axisMarginBottom - axisMarginTop

        xBarDistance = xLabelWidth / (barCount)

        popupTop = axisMarginTop / 2f
        popupBottom = popupTop + popupHeight
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)
                touchX = event.x
                //边界处理
                if (touchX < xLabelStartX) {
                    touchX = xLabelStartX
                } else if (touchX > xLabelStopX) {
                    touchX = xLabelStopX
                }
            }
            MotionEvent.ACTION_UP -> {
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
        drawPopup(canvas)
    }

    /**
     * 画x轴
     */
    fun drawXAxis(canvas: Canvas) {
        canvas.drawLine(xAxisStartX, xAxisStartY, xAxisStopX, xAxisStopY, xAxisPaint)
    }

    /**
     * 画辅助线（虚线）
     */
    open fun drawGuide(canvas: Canvas) {
        for (i in 1 until guideLabelCount) {
            val startX = getGuideStartX()
            val startY = getGuideY(i)
            val stopX = getGuideStopX()
            val stopY = startY
            canvas.drawLine(startX, startY, stopX, stopY, guidePaint)
        }
    }

    /**
     * 画x轴分割线
     */
    open fun drawXDivider(canvas: Canvas) {
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
    open fun drawXLabel(canvas: Canvas) {
        for (i in 0 until xLabelCount) {
            val x = getXLabelX(i)
            val y = getXLabelY()
            canvas.drawText(getXLabelText(i), x, y, labelPaint)
        }
    }

    /**
     * 画辅助线标签
     */
    open fun drawGuideLabel(canvas: Canvas) {
        for (i in 0 until guideLabelCount) {
            val x = getGuideLabelX()
            val y = getGuideLabelY(i)
            val value = getGuideLabelText(i)
            canvas.drawText(value, x, y, labelPaint)
        }
    }


    /**
     * 画popup
     */
    fun drawPopup(canvas: Canvas) {
        //当未触摸时不显示
        if (touchX == -1f) {
            return
        }

        //当前选择的时间
        val selectTime = xLabelBeginTime + ((xLabelEndTime - xLabelBeginTime) / (xLabelStopX - xLabelStartX) * (touchX - xLabelStartX)).toLong()
        val text: String = getPopupText(selectTime)
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
    }

    /**
     * 获取辅助线startX
     */
    open fun getGuideStartX(): Float {
        return xAxisStartX
    }

    /**
     * 获取辅助线stopX
     */
    open fun getGuideStopX(): Float {
        return xAxisStopX
    }

    /**
     * 获取辅助线纵坐标
     */
    open fun getGuideY(index: Int): Float {
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
        return xAxisStopY + axisMarginBottom / 2
    }

    /**
     * 获取x轴标签文字
     */
    open fun getXLabelText(index: Int): String {
        return timeFormat(getXAxisLabelTime(index))
    }

    /**
     * 获取x轴标签时间
     */
    open fun getXAxisLabelTime(index: Int): Int {
        return (xLabelEndTime - xLabelBeginTime) / (xLabelCount - 1) * index
    }


    /**
     * 获取y轴标签横坐标
     */
    open fun getGuideLabelX(): Float {
        return axisMarginLeft / 2f
    }

    /**
     * 获取y轴标签纵坐标
     */
    open fun getGuideLabelY(index: Int): Float {
        return getGuideY(index) - labelTextOffset
    }

    /**
     * 获取y轴标签文字
     */
    open fun getGuideLabelText(index: Int): String {
        //两个label相差的值
        val yLabelDiffer = (guideLabelMaximum - guideLabelMinimum) / (guideLabelCount - 1)
        return (guideLabelMinimum + yLabelDiffer * index).toString()
    }

    /**
     * 获取条形top
     */
    open fun getBarY(value: Float): Float {
        val differ = guideLabelMaximum - guideLabelMinimum
        return xAxisStopY - ((value - guideLabelMinimum) / differ) * yLabelHeight
    }

    /**
     * 获取条形left
     */
    open fun getBarLeft(index: Int): Float {
        return xLabelStartX + index * xBarDistance
    }

    /**
     * 获取条形Right
     */
    open fun getBarRight(index: Int): Float {
        return xLabelStartX + (index + 1) * xBarDistance
    }



    /**
     * 获取条形的开始时间
     */
    fun getBarBeginTime(index: Int): Int {
        return index * ((xLabelEndTime - xLabelBeginTime) / barCount)
    }

    /**
     * 获取条形的结束时间
     */
    fun getBarEndTime(index: Int): Int {
        return (index + 1) * ((xLabelEndTime - xLabelBeginTime) / barCount)
    }

    /**
     * 根据时间戳获取数据
     */
    fun getIndexByTime(time: Long): Int? {
        for (i in 0 until barCount){
            val beginTime = getBarBeginTime(i)
            val endTime = getBarEndTime(i)
            if (time in beginTime..endTime) {
                return i
            }
        }
        return null
    }


    /**
     * 时间转换
     */
    protected fun timeFormat(second: Int): String {
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
     * 初始化辅助线标签
     */
    fun initGuideLabel() {
        val max = getBarMaxValue()
        val min = getBarMinValue()
        if (isGuideAutoLabel && max!=null && min!=null) {
            guideLabelMaximum = (max + (max * guideLabelSpaceTop)).toInt()
            guideLabelMinimum = (min - (min * guideLabelSpaceBottom)).toInt()
        }
    }



    private fun dp2px(dpValue: Float): Float {
        val density = resources.displayMetrics.density
        return (dpValue * density + 0.5f)
    }

    fun loadData(data:List<T>){
        dataList.clear()
        dataList.addAll(data)

        initBarData()
        initGuideLabel()
        initAnimBarData()
        postInvalidate()
    }

    /**
     * 动画数据
     */
    abstract fun initAnimBarData()

    abstract fun initBarData()

    abstract fun drawBar(canvas: Canvas)

    abstract fun getPopupText(selectTime: Long):String

    //获取bar数据最大值
    abstract fun getBarMaxValue():Float?

    //获取bar数据最小值
    abstract fun getBarMinValue():Float?
}