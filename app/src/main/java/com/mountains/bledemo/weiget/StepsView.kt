package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.mountains.bledemo.util.DisplayUtil


class StepsView : View {
    private var currentSteps = 0
    private var maxSteps = 5000

    //现在步数画笔
    val numberPaint: Paint

    //圆环画笔
    val ringPaint: Paint

    //文字画笔
    val textPaint: Paint

    //圆环宽度
    val ringWidth = 50f

    val numberFontMetrics = Paint.FontMetrics()

    val textFontMetrics = Paint.FontMetrics()


    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        ringPaint = Paint()
        ringPaint.isAntiAlias = true
        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = ringWidth

        numberPaint = Paint()
        numberPaint.isAntiAlias = true
        numberPaint.setColor(Color.WHITE)
        numberPaint.textSize = DisplayUtil.dp2px(context, 48f).toFloat()
        numberPaint.setTextAlign(Paint.Align.CENTER)
        numberPaint.getFontMetrics(numberFontMetrics)

        textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.setColor(Color.WHITE)
        textPaint.textSize = DisplayUtil.dp2px(context, 24f).toFloat()
        textPaint.setTextAlign(Paint.Align.CENTER)
        textPaint.getFontMetrics(textFontMetrics)


        Thread(Runnable {
            while (true){
                setCurrentSteps(++currentSteps)
                Thread.sleep(20)
            }
        }).start()
    }

    fun setCurrentSteps(current:Int){
        currentSteps = current
        invalidate()
    }

    fun getCurrentSteps():Int{
        return currentSteps
    }

    fun setMaxSteps(max:Int){
        maxSteps = max
        postInvalidate()
    }

    fun getMaxSteps():Int{
        return maxSteps
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val left = ringWidth / 2f
        val top = ringWidth / 2f
        val right = measuredWidth - ringWidth / 2f
        val bottom = measuredHeight - ringWidth / 2f


        //画圆环背景
        ringPaint.color = Color.WHITE
        canvas.drawArc(left, top, right, bottom, -90f, 360f, false, ringPaint)


        //画圆环
        ringPaint.color = Color.parseColor("#69F0AE")
        var progress = ((currentSteps.toDouble() / maxSteps.toDouble()) * 360).toFloat()
        if(progress>360){
            progress = 360f
        }
        canvas.drawArc(left, top, right, bottom, -90f, progress, false, ringPaint)

        //画步数文字
        val numberOffset = (numberFontMetrics.descent + numberFontMetrics.ascent) / 2
        canvas.drawText(currentSteps.toString(), measuredWidth / 2f, measuredHeight / 2f - numberOffset, numberPaint)

        val textHeight = ((measuredHeight / 2f - numberOffset - top) / 2)  - ((textFontMetrics.descent + textFontMetrics.ascent)/2)
        canvas.drawText("步数", measuredWidth / 2f,textHeight,textPaint)
    }
}