package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.mountains.bledemo.util.DisplayUtil
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.util.*

class HistogramView : View {
    //y轴个数
    var yAxisCount = 8
    //x轴个数
    var xAxisCount = 6
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



        for (i in 0 until 72){
            val random = Random()
            val value = (random.nextDouble() * 100 + 30).toInt()
            val histogramEntity = HistogramEntity(100, i * 20)
            datas.add(histogramEntity)
        }
        datas
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //画x轴
        val xAxisStartX = xAxisMargin
        val xAxisStartY = measuredHeight - xAxisMargin
        val xAxisEndX = measuredWidth - xAxisMargin
        val xAxisEndY = xAxisStartY
        canvas.drawLine(xAxisStartX, xAxisStartY, xAxisEndX, xAxisEndY, axisPaint)

        //画y轴
        val yAxisStartX = yAxisMargin
        val yAxisStartY = yAxisMargin
        val yAxisEndX = yAxisMargin
        val yAxisEndY = measuredHeight - yAxisMargin
        canvas.drawLine(yAxisStartX, yAxisStartY, yAxisEndX, yAxisEndY, axisPaint)

        //画x轴单位分割线
        //x轴长度
        val xAxisWidth = xAxisEndX - xAxisStartX
        //x轴两点距离
        val xDistance = xAxisWidth / xAxisCount
        for (i in 0 until xAxisCount) {
            val startX = (i + 1) * xDistance + xAxisStartX
            val startY = xAxisStartY - 10
            val endX = startX
            val endY = xAxisStartY
            canvas.drawLine(startX, startY, endX, endY, axisPaint)
        }


        //画y轴单位分割线
        //y轴长度
        val yAxisHeight = yAxisEndY - yAxisStartY
        //y轴两点距离
        val yDistance = yAxisHeight / (yAxisCount + 1)
        for (i in 0 until yAxisCount) {
            val startX = yAxisStartX
            val startY = yAxisEndY - (i + 1) * yDistance
            val endX = startX + 10
            val endY = startY
            canvas.drawLine(startX, startY, endX, endY, axisPaint)
        }

        //画x轴数值
        for (i in 0 .. xAxisCount) {
            val x = (i) * xDistance + xAxisStartX
            val y = xAxisStartY + 50
            val min = xAxisLength / xAxisCount * i
            canvas.drawText(min2Hour(min), x, y, axisPaint)
        }

        //遍历y轴数据，计算y轴最大值最小值
        var max = Float.MIN_VALUE
        var min = Float.MAX_VALUE
        for (data in datas) {
            max = Math.max(max, data.value.toFloat())
            min = Math.min(min, data.value.toFloat())
        }
        Logger.e("$max,$min")

         max *= 1.1f
         min /= 1.1f
         //y值数据间隔
         val yDataDistance = (max  - min) / yAxisCount
         //画y轴数值
         for(i in 0 until yAxisCount){
             val x = yAxisStartX - 40
             val y = yAxisEndY - (i+1) * yDistance - textOffset
             val value = (yDataDistance * i + min).toInt().toString()
             canvas.drawText(value,x,y,axisPaint)
         }

        //画数值
        for(data in datas){
            val time = data.time.toFloat()
            val value = data.value

            val x = (time / xAxisLength) * xAxisWidth + xAxisStartX

            val y = yAxisEndY -(yAxisHeight/yAxisCount+1)*(value / (max - min))
            canvas.drawLine(x,xAxisStartY,x,y,valuePaint)
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

}