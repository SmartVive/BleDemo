package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.util.rangeTo
import com.mountains.bledemo.R
import com.mountains.bledemo.util.DisplayUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max


class SleepHistogramView2 : View {
    var sleepData = mutableListOf<ISleepHistogramData>()
    //条形画笔
    private lateinit var barPaint: Paint


    //x轴画笔
    private lateinit var axisPaint: Paint
    //x轴颜色
    @ColorInt
    var axisColor: Int
    //x轴宽度
    var axisWidth: Float
    private var axisTextOffset = 0f


    //睡眠数据文字画笔
    private lateinit var sleepDataSubTextPaint: Paint
    private lateinit var sleepDataMainTextPaint: Paint
    //睡眠数据文字颜色
    @ColorInt
    var sleepDataSubTextColor: Int
    @ColorInt
    var sleepDataMainTextColor: Int
    //睡眠数据文字高度
    var sleepDataTextHeight: Float


    //popup画笔
    lateinit var popupPaint: Paint
    //popup文字画笔
    private lateinit var popupSubTextPaint: Paint
    private lateinit var popupMainTextPaint: Paint
    //popup高度,宽度由文字宽度决定
    var popupHeight: Float
    @ColorRes
    var popupTextColor: Int
    var popupTextMargin: Float

    //标注画笔，标注颜色对应的睡眠类型
    private lateinit var indicatorPaint: Paint

    //图表边距
    var chartMarginLeft = 0f
    var chartMarginRight = 0f
    var chartMarginTop = 0f
    var chartMarginBottom = 0f



    //睡眠类型对应颜色
    @ColorInt
    var deepTypeColor: Int
    @ColorInt
    var lightTypeColor: Int
    @ColorInt
    var soberTypeColor: Int




    //睡眠开始时间和结束时间,根据数据获取
    private var sleepBeginTime: Long = 0
    private var sleepEndTime: Long = 0

    //x轴长度
    private var xAxisWidth = 0f
    //x轴位置
    private var xAxisLeft: Float = 0f
    private var xAxisRight: Float = 0f
    private var xAxisBottom: Float = 0f

    //popup位置
    private var popupTop = 0f
    private var popupBottom = 0f


    private val sleepDataSubTextRect = Rect()
    private val sleepDataMainTextRect = Rect()
    private val popupSubTextRect = Rect()
    private val popupMainTextRect = Rect()
    private val indicatorTextRect = Rect()

    //触摸位置，显示popup用
    var touchX = -1f


    val simpleDateFormat by lazy { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    companion object{
        //浅睡眠
        const val TYPE_LIGHT = 0
        //深睡眠
        const val TYPE_DEEP = 1
        //清醒
        const val TYPE_SOBER = 2
    }

    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SleepHistogramView2)
        chartMarginLeft = typedArray.getDimension(R.styleable.SleepHistogramView2_SleepHistogramView_chartMarginLeft,DisplayUtil.dp2px(context,30f).toFloat())
        chartMarginRight = typedArray.getDimension(R.styleable.SleepHistogramView2_SleepHistogramView_chartMarginRight,DisplayUtil.dp2px(context,30f).toFloat())
        chartMarginTop = typedArray.getDimension(R.styleable.SleepHistogramView2_SleepHistogramView_chartMarginTop,DisplayUtil.dp2px(context,0f).toFloat())
        chartMarginBottom = typedArray.getDimension(R.styleable.SleepHistogramView2_chartMarginBottom,DisplayUtil.dp2px(context,30f).toFloat())
        axisColor = typedArray.getColor(R.styleable.SleepHistogramView2_SleepHistogramView_axisColor,Color.DKGRAY)
        axisWidth = typedArray.getDimension(R.styleable.SleepHistogramView2_SleepHistogramView_axisWidth,2f)
        deepTypeColor = typedArray.getColor(R.styleable.SleepHistogramView2_SleepHistogramView_deepTypeColor, Color.parseColor("#33bcb7"))
        lightTypeColor = typedArray.getColor(R.styleable.SleepHistogramView2_SleepHistogramView_lightTypeColor, Color.parseColor("#4f55e1"))
        soberTypeColor = typedArray.getColor(R.styleable.SleepHistogramView2_SleepHistogramView_soberTypeColor, Color.parseColor("#f6d39d"))
        sleepDataSubTextColor = typedArray.getColor(R.styleable.SleepHistogramView2_SleepHistogramView_sleepDataSubTextColor, Color.GRAY)
        sleepDataMainTextColor = typedArray.getColor(R.styleable.SleepHistogramView2_SleepHistogramView_sleepDataMainTextColor, Color.BLACK)
        sleepDataTextHeight = typedArray.getDimension(R.styleable.SleepHistogramView2_SleepHistogramView_sleepDataTextHeight,DisplayUtil.dp2px(context,80f).toFloat())
        popupHeight = typedArray.getDimension(R.styleable.SleepHistogramView2_SleepHistogramView_popupHeight,DisplayUtil.dp2px(context,60f).toFloat())
        popupTextColor = typedArray.getColor(R.styleable.SleepHistogramView2_SleepHistogramView_popupTextColor,  Color.parseColor("#f6d39d"))
        popupTextMargin = typedArray.getDimension(R.styleable.SleepHistogramView2_SleepHistogramView_popupTextMargin,DisplayUtil.dp2px(context,8f).toFloat())
        typedArray.recycle()
        init()
    }

    private fun init(){
        axisPaint = Paint()
        axisPaint.isAntiAlias = true
        axisPaint.setColor(axisColor)
        axisPaint.strokeWidth = axisWidth
        axisPaint.textSize = DisplayUtil.dp2px(context, 12f).toFloat()
        axisPaint.textAlign = Paint.Align.CENTER
        val fontMetrics = Paint.FontMetrics()
        axisPaint.getFontMetrics(fontMetrics)
        axisTextOffset = (fontMetrics.bottom + fontMetrics.top) / 2


        barPaint = Paint()
        barPaint.isAntiAlias = true
        barPaint.setColor(Color.RED)

        sleepDataSubTextPaint = Paint()
        sleepDataSubTextPaint.isAntiAlias = true
        sleepDataSubTextPaint.color = sleepDataSubTextColor
        //sleepDataSubTextPaint.textAlign = Paint.Align.CENTER
        sleepDataSubTextPaint.textSize = DisplayUtil.dp2px(context, 14f).toFloat()

        sleepDataMainTextPaint = Paint()
        sleepDataMainTextPaint.isAntiAlias = true
        sleepDataMainTextPaint.color = sleepDataMainTextColor
        //sleepDataMainTextPaint.textAlign = Paint.Align.CENTER
        sleepDataMainTextPaint.textSize = DisplayUtil.dp2px(context, 28f).toFloat()

        popupPaint = Paint()
        popupPaint.isAntiAlias = true
        popupPaint.color = Color.parseColor("#80000000")
        popupPaint.style = Paint.Style.FILL
        popupPaint.strokeWidth = 2f

        popupSubTextPaint = Paint()
        popupSubTextPaint.isAntiAlias = true
        popupSubTextPaint.color = popupTextColor
        popupSubTextPaint.textSize = DisplayUtil.dp2px(context, 14f).toFloat()

        popupMainTextPaint = Paint()
        popupMainTextPaint.isAntiAlias = true
        popupMainTextPaint.color = popupTextColor
        popupMainTextPaint.textSize = DisplayUtil.dp2px(context, 18f).toFloat()

        indicatorPaint = Paint()
        indicatorPaint.isAntiAlias = true
        indicatorPaint.textSize = DisplayUtil.dp2px(context, 12f).toFloat()
        indicatorPaint.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xAxisLeft = chartMarginLeft
        xAxisRight = measuredWidth - chartMarginRight
        xAxisBottom = measuredHeight - chartMarginBottom
        xAxisWidth = xAxisRight - xAxisLeft

        popupTop = (sleepDataTextHeight - popupHeight) / 2f
        popupBottom = popupHeight + popupTop
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        when(event.action){
            MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE->{
                touchX = event.x
                //边界处理
                if (touchX < xAxisLeft){
                    touchX = xAxisLeft
                }else if (touchX > xAxisRight){
                    touchX = xAxisRight
                }
            }
            MotionEvent.ACTION_UP->{
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
        //画x轴标签
        drawXAxisLabel(canvas)
        //画条形
        drawBar(canvas)


        if (touchX != -1f){
            drawSleepDataPopup(canvas)
        } else {
            drawSleepDurationText(canvas)
            //画标注
            drawIndicator(canvas)
        }
    }



    /**
     * 画x轴
     */
    fun drawXAxis(canvas: Canvas) {
        canvas.drawLine(xAxisLeft, xAxisBottom, xAxisRight, xAxisBottom, axisPaint)
    }


    /**
     * 画x轴标签
     */
    fun drawXAxisLabel(canvas: Canvas){
        for (i in 0 until 2) {
            val x = getXAxisLabelX(i)
            val y = getXAxisLabelY()
            canvas.drawText(getXAxisLabelText(i), x, y, axisPaint)
        }
    }

    fun drawBar(canvas: Canvas) {
        for (i in 0 until sleepData.size){
            val sleepBeginTime = sleepData[i].getSleepBeginTime()
            val sleepEndTime = sleepData[i].getSleepEndTime()
            val type = sleepData[i].getSleepType()
            val left = getBarX(sleepBeginTime)
            val right = getBarX(sleepEndTime)
            val top = getBarY(type)
            if(type == TYPE_DEEP){
                barPaint.color = deepTypeColor
            }else if (type == TYPE_LIGHT){
                barPaint.color = lightTypeColor
            }else{
                barPaint.color = soberTypeColor
            }
            canvas.drawRect(left,top,right,xAxisBottom,barPaint)
        }
    }

    /**
     * 画睡眠类型对应颜色的标注
     */
    fun drawIndicator(canvas: Canvas){
        val texts = arrayOf("深睡", "浅睡", "清醒")
        val colors = arrayOf(deepTypeColor,lightTypeColor,soberTypeColor)
        val textWidths = FloatArray(texts.size)
        val textHeights = FloatArray(texts.size)

        texts.forEachIndexed {index,s->
            indicatorPaint.getTextBounds(s,0,s.length,indicatorTextRect)
            val textWidth = indicatorTextRect.right - indicatorTextRect.left
            val textHeight =  indicatorTextRect.bottom - indicatorTextRect.top
            textWidths[index] = textWidth.toFloat()
            textHeights[index] = textHeight.toFloat()
        }
        val totalTextHeight = textHeights.sum()
        val textMarginHeight = (sleepDataTextHeight - totalTextHeight) / 4



        var textY = 0f
        texts.forEachIndexed { index, s ->
            indicatorPaint.color = colors[index]
            val textX = xAxisRight - textWidths[index]
            textY += textMarginHeight + textHeights[index]

            val radius = 10f
            val circleX = textX - radius - 5f
            val circleY = textY - radius
            canvas.drawText(s,textX,textY,indicatorPaint)
            canvas.drawCircle(circleX,circleY,radius,indicatorPaint)
        }
    }

    /**
     * 画睡眠时长数据
     */
    fun drawSleepDurationText(canvas: Canvas){
        val date = SimpleDateFormat("MM.dd", Locale.getDefault()).format(sleepEndTime)
        val text = "$date-夜间睡眠时长"
        val sleepDurationText = formatDurationText(sleepBeginTime,sleepEndTime)

        sleepDataSubTextPaint.getTextBounds(text,0,text.length,sleepDataSubTextRect)
        sleepDataMainTextPaint.getTextBounds(sleepDurationText,0,sleepDurationText.length,sleepDataMainTextRect)

        val subTextHeight = sleepDataSubTextRect.bottom-sleepDataSubTextRect.top
        val mainTextHeight = sleepDataMainTextRect.bottom - sleepDataMainTextRect.top

        val textMargin = (sleepDataTextHeight - subTextHeight - mainTextHeight)/3


        val dateTextX = xAxisLeft
        val dateTextY = textMargin + subTextHeight
        val sleepDurationX = dateTextX
        val sleepDurationY = dateTextY  + textMargin + mainTextHeight

        canvas.drawText(text,dateTextX,dateTextY,sleepDataSubTextPaint)
        canvas.drawText(sleepDurationText,sleepDurationX,sleepDurationY,sleepDataMainTextPaint)
    }

    /**
     * 画睡眠弹窗数据
     */
    fun drawSleepDataPopup(canvas: Canvas){
        val selectTime =  sleepBeginTime + ((sleepEndTime - sleepBeginTime) / (xAxisRight - xAxisLeft) * (touchX-xAxisLeft)).toLong()
        val sleepDataByTime = getSleepDataByTime(selectTime) ?: return

        //文字宽度，用来确定popup宽度
        val textWidth:Float
        //睡眠数据时间（03：00-04：00）
        val timeText:String
        //睡眠类型和时长（浅睡 1小时30分钟）
        val typeAndDurationText:String

        sleepDataByTime.let {
            val sleepDurationText = formatDurationText(it.getSleepBeginTime(),it.getSleepEndTime())
            val sleepTypeString = getSleepTypeString(it.getSleepType())
            val beginTime = simpleDateFormat.format(it.getSleepBeginTime())
            val endTime = simpleDateFormat.format(it.getSleepEndTime())

            //Logger.d("$sleepDurationText,$sleepDurationText,$beginTime-$endTime")

            timeText = "$beginTime - $endTime"
            typeAndDurationText = "$sleepTypeString $sleepDurationText"
            popupSubTextPaint.getTextBounds(timeText,0,timeText.length,popupSubTextRect)
            popupMainTextPaint.getTextBounds(typeAndDurationText,0,typeAndDurationText.length,popupMainTextRect)
            val subTextWidth = popupSubTextRect.right-popupSubTextRect.left
            val mainTextWidth = popupMainTextRect.right-popupMainTextRect.left
            textWidth = max(subTextWidth,mainTextWidth).toFloat()
        }

        val popupLeft: Float
        val popupRight: Float
        val popupWidth = textWidth + popupTextMargin * 2
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

        val subTextHeight = popupSubTextRect.bottom-popupSubTextRect.top
        val mainTextHeight = popupMainTextRect.bottom - popupMainTextRect.top
        val textMargin = (popupHeight - subTextHeight - mainTextHeight)/3
        val timeX = popupLeft + popupTextMargin
        val timeY = popupTop + textMargin + subTextHeight
        val typeAndDurationX = popupLeft + popupTextMargin
        val typeAndDurationY = timeY  + textMargin + mainTextHeight

        //popup
        canvas.drawRoundRect(popupLeft, popupTop, popupRight, popupBottom, 10f, 10f, popupPaint)
        //触摸位置提醒线条
        canvas.drawLine(touchX,popupBottom,touchX,xAxisBottom,popupPaint)
        //popup里的文字
        canvas.drawText(timeText,timeX,timeY,popupSubTextPaint)
        canvas.drawText(typeAndDurationText,typeAndDurationX,typeAndDurationY,popupMainTextPaint)
    }


    /**
     * 获取x轴第index个标签横坐标
     */
    private fun getXAxisLabelX(index: Int): Float {
        return xAxisLeft + xAxisWidth * index
    }

    /**
     * 获取x轴标签纵坐标
     */
    private fun getXAxisLabelY(): Float {
        return xAxisBottom + chartMarginBottom/2 - axisTextOffset
    }

    /**
     * 获取x轴第index个标签文字
     */
    private fun getXAxisLabelText(index: Int):String{
        if(index == 0){
            return simpleDateFormat.format(sleepBeginTime)
        }else{
            return simpleDateFormat.format(sleepEndTime)
        }
    }

    /**
     * 根据时间获取条形横坐标
     */
    fun getBarX(time: Long):Float {
        return xAxisLeft + ((time - sleepBeginTime).toFloat() / (sleepEndTime - sleepBeginTime).toFloat()) * xAxisWidth
    }

    /**
     * 根据类型获取条形纵坐标
     */
    fun getBarY(type: Int):Float {
        when(type){
            TYPE_SOBER->{
                return xAxisBottom - (xAxisBottom - chartMarginTop - sleepDataTextHeight)*0.5f
            }
            TYPE_LIGHT->{
                return xAxisBottom - (xAxisBottom - chartMarginTop - sleepDataTextHeight)*0.75f
            }
            TYPE_DEEP->{
                return chartMarginTop + sleepDataTextHeight
            }
        }
        return  chartMarginTop + sleepDataTextHeight
    }

    private fun initDataTime(){
        if (!sleepData.isEmpty()){
            sleepBeginTime = sleepData.first().getSleepBeginTime()
            sleepEndTime = sleepData.last().getSleepEndTime()
        }else{
            sleepBeginTime = 57600000
            sleepEndTime = 144000000-1
        }

    }

    /**
     * 时间差转换为时长
     */
    fun formatDurationText(beginTime:Long,endTime:Long):String{
        val duration = endTime - beginTime
        val min = duration / 1000 / 60 % 60
        val hour = duration / 1000 / 60 / 60
        if (hour == 0L){
            return "${min}分钟"
        }
        return "${hour}小时${min}分钟"
    }

    /**
     * 根据时间戳获取睡眠数据
     */
    fun getSleepDataByTime(time: Long): ISleepHistogramData? {
        for (data in sleepData) {
            if (time in data.getSleepBeginTime()..data.getSleepEndTime()) {
                return data
            }
        }
        return null
    }

    /**
     * 获取睡眠类型文字
     */
    fun getSleepTypeString(type: Int):String{
        return if(type == TYPE_DEEP){
            "深睡"
        }else if (type == TYPE_LIGHT){
            "浅睡"
        }else{
            "清醒"
        }
    }

    fun loadData(data:List<ISleepHistogramData>){
        sleepData.clear()
        sleepData.addAll(data)
        initDataTime()
        postInvalidate()
    }
}