package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.mountains.bledemo.util.DisplayUtil
import com.orhanobut.logger.Logger
import java.util.*

class HistogramView : View {
    //y轴分割线个数
    var yAxisCount = 9
    //x轴分割线个数
    var xAxisCount = 7
    //x、y轴画笔
    lateinit var axisPaint: Paint
    //x、y轴宽度
    var axisWidth = 2f
    //x、y轴颜色
    var axisColor = Color.LTGRAY
    //x轴边距
    var xAxisMargin = 80f
    //y轴边距
    var yAxisMargin = 80f

    var xAxisLength = 48

    //y轴默认最大最小值
    var yMaxValue = 100f
    var yMinValue = 0f

    //单位秒
    var xAxisStartTime:Float = 0f
    var xAxisEndTime:Float = 86400f

    var datas: MutableList<IHistogramData> = mutableListOf()

    var textOffset = 0f

    lateinit var valuePaint:Paint

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

    }


    var xAxisLeft:Float = 0f
    var xAxisRight:Float = 0f
    var xAxisTop:Float = 0f

    var yAxisLeft:Float = 0f
    var yAxisTop:Float = 0f
    var yAxisBottom:Float = 0f

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
        xAxisLeft = xAxisMargin
        xAxisRight = measuredWidth - xAxisMargin
        xAxisTop = measuredHeight - xAxisMargin

        yAxisLeft = xAxisMargin
        yAxisTop = xAxisMargin
        yAxisBottom = measuredHeight - xAxisMargin

        xAxisWidth = xAxisRight - xAxisLeft

        yAxisHeight = yAxisBottom - yAxisTop

        //x轴分割线两点距离
        xDivideDistance = xAxisWidth / (xAxisCount - 1)
        //y轴分割线两点距离
        yDivideDistance = yAxisHeight / (yAxisCount-1)

        //x轴两个变量距离
        xValueDistance = xAxisWidth/(xAxisEndTime-xAxisStartTime)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //画x轴
        canvas.drawLine(xAxisLeft, xAxisTop, xAxisRight, xAxisTop, axisPaint)

        //画y轴
        canvas.drawLine(yAxisLeft, yAxisTop, yAxisLeft, yAxisBottom, axisPaint)

        //画x轴分割线
        for (i in 0 until xAxisCount) {
            val startX = getXAxisDivideX(i)
            val startY = xAxisTop - 10
            val endX = startX
            val endY = xAxisTop
            canvas.drawLine(startX, startY, endX, endY, axisPaint)
        }

        //画x轴变量
        for (i in 0 until xAxisCount) {
            val x = getXAxisValueX(i)
            val y = xAxisTop + 50
            //val min = xAxisLength / (xAxisCount-1) * i
            val second = (xAxisEndTime-xAxisStartTime) / (xAxisCount-1) * i
            canvas.drawText(second2Hour(second.toInt()), x, y, axisPaint)
        }


        //画y轴分割线
        for (i in 0 until yAxisCount) {
            val startX = yAxisLeft
            val startY = yAxisBottom - i * yDivideDistance
            val endX = startX + 10
            val endY = startY
            canvas.drawLine(startX, startY, endX, endY, axisPaint)
        }


        //遍历y轴数据，计算y轴最大值最小值
        var max = Float.MIN_VALUE
        var min = Float.MAX_VALUE
        for (data in datas) {
            max = Math.max(max, data.getHistogramValue().toFloat())
            min = Math.min(min, data.getHistogramValue().toFloat())
        }
        if(max == Float.MIN_VALUE && min == Float.MAX_VALUE){
            max = yMaxValue
            min = yMinValue
        }else{
            max *= 1.1f
            min *= 0.9f
        }


         //y值数据间隔
         val yDataDistance = (max  - min) / (yAxisCount-1)
         //画y轴数值
         for(i in 0 until yAxisCount){
             val x = yAxisLeft - 40
             val y = yAxisBottom - i * yDivideDistance - textOffset
             val value = (yDataDistance * i + min).toInt().toString()
             canvas.drawText(value,x,y,axisPaint)
         }


        //画数值
        for (i in 0 until  xAxisLength){
            val startTime = i* ((xAxisEndTime-xAxisStartTime)/xAxisLength)
            val endTime = (i+1)* ((xAxisEndTime-xAxisStartTime)/xAxisLength)

            var sumValue = 0f
            var count = 0
            for(data in datas){
                if (startTime<=data.getHistogramTime() && endTime>data.getHistogramTime()){
                    sumValue+=data.getHistogramValue()
                    count++
                }
            }

            //多个数值用一条线显示时，显示平均值
            val value = sumValue/count
            val y = yAxisBottom - yDivideDistance *((value - min)/yDataDistance)
            var left = xAxisLeft + startTime * xValueDistance
            var right = xAxisLeft + endTime * xValueDistance

            left += (right-left)*0.3f
            right -= (right-left)*0.3f
            //Logger.e("$left,$right")
            canvas.drawRect(left,y,right,xAxisTop,valuePaint)
        }

    }


    fun second2Hour(second: Int): String {
        var hourStr = (second/60 / 60).toString()
        var minStr = (second/60 % 60).toString()
        if(hourStr.length == 1){
            hourStr = "0$hourStr"
        }
        if(minStr.length == 1){
            minStr = "0$minStr"
        }
        return "$hourStr:$minStr"
    }

    /**获取x轴第num个变量横坐标
     * 取两个分割线中间横坐标
     */
    private fun getXAxisValueX(num: Int): Float {
        return xAxisLeft + num*xDivideDistance +  0.5f * xValueDistance
    }


    /**
     * 获取第num个分割线横坐标
     */
    private fun getXAxisDivideX(num:Int):Float{
        return num * xDivideDistance  + xAxisLeft
    }

    fun loadData(data : List<IHistogramData>){
        datas.clear()
        datas.addAll(data)
        postInvalidate()
    }
}