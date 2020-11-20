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
    //y轴个数
    var yAxisCount = 9
    //x轴个数
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

    var xAxisLength = 1440

    var datas: MutableList<HistogramEntity> = mutableListOf()

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
        valuePaint.strokeWidth = 3f



        for (i in 0 .. 72){
            val random = Random()
            val value = (random.nextDouble() * 100 + 30).toInt()
            val histogramEntity = HistogramEntity(100, i * 20)
            datas.add(histogramEntity)
        }

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
        xDivideDistance = xAxisWidth / (xAxisLength)
        //y轴分割线两点距离
        yDivideDistance = yAxisHeight / (yAxisCount-1)

        Logger.e("$xDivideDistance")
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
            //val min = ((xAxisLength/xAxisCount) * i  + (xAxisLength/xAxisCount) * (i+1)) / 2
            val min = xAxisLength / (xAxisCount-1) * i
            canvas.drawText(min2Hour(min), x, y, axisPaint)
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
            max = Math.max(max, data.value.toFloat())
            min = Math.min(min, data.value.toFloat())
        }
        max *= 1.1f
        min *= 0.9f


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
        for(data in datas){
            val time = data.time.toFloat()
            val value = data.value
            //val x = (time / xAxisLength) * xAxisWidth + xAxisLeft
            val x = xAxisLeft + time * xDivideDistance + 0.5f * xDivideDistance
            val y = yAxisBottom - yDivideDistance *((value - min)/yDataDistance)
            Logger.e("${time * xDivideDistance + 0.5f * xDivideDistance}")
            canvas.drawLine(x,xAxisTop,x,y,valuePaint)
        }
    }


    fun min2Hour(min: Int): String {
        var hourStr = (min / 60).toString()
        var minStr = (min % 60).toString()
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
    private fun getXAxisValueX(num:Int):Float{
        return xAxisLeft + (num*(xAxisLength/(xAxisCount-1))*xDivideDistance)+0.5f*xDivideDistance
    }


    /**
     * 获取第num个分割线横坐标
     */
    private fun getXAxisDivideX(num:Int):Float{
        return (num) * (xAxisLength.toFloat()/(xAxisCount.toFloat()-1))*xDivideDistance + xAxisLeft
    }
}