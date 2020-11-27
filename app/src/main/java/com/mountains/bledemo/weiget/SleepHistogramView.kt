package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.mountains.bledemo.util.DisplayUtil
import java.text.SimpleDateFormat
import java.util.*

class SleepHistogramView : View {
    var sleepData = mutableListOf<ISleepHistogramData>()
    //条形画笔
    lateinit var barPaint: Paint
    //x、y轴画笔
    lateinit var axisPaint: Paint
    //x、y轴颜色
    var axisColor = Color.DKGRAY
    //x、y轴宽度
    var axisWidth = 2f
    var textOffset = 0f
    //x轴长度
    var xAxisWidth = 0f

    var xAxisLeft: Float = 0f
    var xAxisRight: Float = 0f
    var xAxisTop: Float = 0f

    //xy轴边
    var axisMarginLeft = 60f
    var axisMarginRight = 60f
    var axisMarginTop = 60f
    var axisMarginBottom = 60f

    //睡眠开始时间和结束时间
    var sleepBeginTime: Long = 0
    var sleepEndTime: Long = 0

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
        textOffset = (fontMetrics.descent + fontMetrics.ascent) / 2


        barPaint = Paint()
        barPaint.isAntiAlias = true
        barPaint.setColor(Color.RED)
        barPaint.strokeWidth = 1f


        axisMarginLeft = axisPaint.measureText("9999")
        axisMarginBottom = (fontMetrics.descent - fontMetrics.ascent) * 2
        axisMarginTop = axisMarginBottom
        axisMarginRight = axisMarginLeft

       /* for (i in 0..10){
            sleepData.add(SleepHistogramEntity(i%3,i*1440L,(i+1)*1440L))
        }*/
        sleepData.clear()
        sleepData.add(SleepHistogramEntity(1,1606410780000,1606412780000))
        sleepData.add(SleepHistogramEntity(0,1606412780000,1606413780000))
        sleepData.add(SleepHistogramEntity(2,1606413780000,1606423780000))
        sleepData.add(SleepHistogramEntity(1,1606423780000,1606430780000))
        sleepData.add(SleepHistogramEntity(2,1606430780000,1606433780000))
        sleepData.add(SleepHistogramEntity(1,1606433780000,1606435020000))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xAxisLeft = axisMarginLeft
        xAxisRight = measuredWidth - axisMarginRight
        xAxisTop = measuredHeight - axisMarginBottom
        xAxisWidth = xAxisRight - xAxisLeft
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画x轴
        drawXAxis(canvas)
        //画x轴标签
        drawXAxisLabel(canvas)
        //画条形
        drawBar(canvas)

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
                barPaint.color = Color.parseColor("e8b339")
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
        return xAxisTop + axisMarginBottom/2 - textOffset
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
                return xAxisTop - (xAxisTop - axisMarginTop)/2
            }
            TYPE_LIGHT->{
                return xAxisTop - (xAxisTop - axisMarginTop)/3*2
            }
            TYPE_DEEP->{
                return axisMarginTop
            }
        }
        return  axisMarginTop
    }

    private fun initDataTime(){
        sleepBeginTime = sleepData.first().getSleepBeginTime()
        sleepEndTime = sleepData.last().getSleepEndTime()
    }

    fun loadData(data:List<ISleepHistogramData>){
        sleepData.clear()
        sleepData.addAll(data)
        initDataTime()
        postInvalidate()
    }
}