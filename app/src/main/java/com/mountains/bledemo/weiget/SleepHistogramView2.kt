package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.mountains.bledemo.util.DisplayUtil
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class SleepHistogramView2 : View {
    var sleepData = mutableListOf<ISleepHistogramData>()
    //条形画笔
    lateinit var barPaint: Paint

    //x轴画笔
    lateinit var axisPaint: Paint
    //x轴颜色
    var axisColor = Color.DKGRAY
    //x轴宽度
    var axisWidth = 2f
    var axisTextOffset = 0f

    //睡眠数据文字画笔
    lateinit var sleepDataSubTextPaint:Paint
    lateinit var sleepDataMainTextPaint:Paint
    //睡眠数据文字颜色
    var sleepDataSubTextColor = Color.GRAY
    var sleepDataMainTextColor = Color.BLACK
    //睡眠数据文字高度
    var sleepDataTextHeight = 250f

    lateinit var popupPaint:Paint
    //popup宽高度
    var popupWidth = 400f
    var popupHeight = 175f


    //xy轴边距
    var axisMarginLeft = 60f
    var axisMarginRight = 60f
    var axisMarginTop = 0f
    var axisMarginBottom = 60f



    //睡眠开始时间和结束时间
    var sleepBeginTime: Long = 0
    var sleepEndTime: Long = 0

    //x轴长度
    private var xAxisWidth = 0f
    //x轴位置
    private var xAxisLeft: Float = 0f
    private var xAxisRight: Float = 0f
    private var xAxisTop: Float = 0f

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

    constructor(context: Context?) : this(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
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
        barPaint.strokeWidth = 1f

        sleepDataSubTextPaint = Paint()
        sleepDataSubTextPaint.isAntiAlias = true
        sleepDataSubTextPaint.color = sleepDataSubTextColor
        //sleepDataSubTextPaint.textAlign = Paint.Align.CENTER
        sleepDataSubTextPaint.textSize = DisplayUtil.dp2px(context, 14f).toFloat()

        sleepDataMainTextPaint = Paint()
        sleepDataMainTextPaint.isAntiAlias = true
        sleepDataMainTextPaint.color = sleepDataMainTextColor
        //sleepDataMainTextPaint.textAlign = Paint.Align.CENTER
        sleepDataMainTextPaint.textSize = DisplayUtil.dp2px(context, 26f).toFloat()

        popupPaint = Paint()
        popupPaint.isAntiAlias = true
        popupPaint.color = Color.parseColor("#80000000")
        popupPaint.style = Paint.Style.FILL
        popupPaint.strokeWidth = 2f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xAxisLeft = axisMarginLeft
        xAxisRight = measuredWidth - axisMarginRight
        xAxisTop = measuredHeight - axisMarginBottom
        xAxisWidth = xAxisRight - xAxisLeft
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
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP->{
                touchX = -1f
                invalidate()
                return true
            }
        }
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

            val popupLeft: Float
            val popupRight: Float
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

            val popupTop = (sleepDataTextHeight - popupHeight) / 2f
            val popupBottom = popupHeight + popupTop
            canvas.drawRoundRect(popupLeft, popupTop, popupRight, popupBottom, 10f, 10f, popupPaint)
            canvas.drawLine(touchX,popupBottom,touchX,xAxisTop,popupPaint)

            val selectTime =  (sleepEndTime - sleepBeginTime) / (xAxisRight - xAxisLeft) * (touchX-xAxisLeft) / 1000
            for(data in sleepData){
                if(data.getSleepBeginTime()-sleepBeginTime <= selectTime && data.getSleepEndTime()-sleepBeginTime >= selectTime){
                    Logger.e("${selectTime},${data.getSleepBeginTime()-sleepBeginTime}")
                    break
                }
            }
        } else {
            drawSleepDataText(canvas)
        }
    }

    private fun drawSleepDataText(canvas: Canvas){
        val date = SimpleDateFormat("MM.dd", Locale.getDefault()).format(sleepEndTime)
        val text = "$date-夜间睡眠时长"
        val sleepDuration = sleepEndTime - sleepBeginTime
        val min = sleepDuration / 1000 / 60 % 60
        val hour = sleepDuration / 1000 / 60 / 60
        val sleepDurationText = "${hour}小时${min}分钟"


        val subTextRect = Rect()
        val mainTextRect = Rect()

        sleepDataSubTextPaint.getTextBounds(text,0,text.length,subTextRect)
        sleepDataMainTextPaint.getTextBounds(text,0,sleepDurationText.length,mainTextRect)

        val subTextHeight = subTextRect.bottom-subTextRect.top
        val mainTextHeight = mainTextRect.bottom - mainTextRect.top

        val textMargin = (sleepDataTextHeight - subTextHeight - mainTextHeight)/3


        val dateTextX = xAxisLeft
        val dateTextY = textMargin + subTextHeight
        val sleepDurationX = dateTextX
        val sleepDurationY = dateTextY  + textMargin + mainTextHeight

        canvas.drawText(text,dateTextX,dateTextY,sleepDataSubTextPaint)
        canvas.drawText(sleepDurationText,sleepDurationX,sleepDurationY,sleepDataMainTextPaint)


        //canvas.drawRect(0f,0f,100f,200f,sleepDataMainTextPaint)
    }

    /**
     * 画x轴
     */
    fun drawXAxis(canvas: Canvas) {
        canvas.drawLine(xAxisLeft, xAxisTop, xAxisRight, xAxisTop, axisPaint)
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
                barPaint.color = Color.parseColor("#33bcb7")
            }else if (type == TYPE_LIGHT){
                barPaint.color = Color.parseColor("#4f55e1")
            }else{
                barPaint.color = Color.parseColor("#e8b339")
            }
            canvas.drawRect(left,top,right,xAxisTop,barPaint)
        }
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
        return xAxisTop + axisMarginBottom/2 - axisTextOffset
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
                return xAxisTop - (xAxisTop - axisMarginTop - sleepDataTextHeight)*0.5f
            }
            TYPE_LIGHT->{
                return xAxisTop - (xAxisTop - axisMarginTop - sleepDataTextHeight)*0.75f
            }
            TYPE_DEEP->{
                return axisMarginTop + sleepDataTextHeight
            }
        }
        return  axisMarginTop + sleepDataTextHeight
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

    fun loadData(data:List<ISleepHistogramData>){
        sleepData.clear()
        sleepData.addAll(data)
        initDataTime()
        postInvalidate()
    }
}