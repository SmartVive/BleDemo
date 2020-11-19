package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.mountains.bledemo.R
import com.mountains.bledemo.util.DisplayUtil
import com.orhanobut.logger.Logger


class StepsView : View {
    private var currentSteps = 0
    private var maxSteps = 5000

    //现在步数画笔
    lateinit var numberPaint: Paint

    //圆环画笔
    lateinit var ringPaint: Paint

    //文字画笔
    lateinit var textPaint: Paint

    //圆环宽度
    var ringWidth = 0f

    //文字偏移
    var numberOffset: Float = 0f
    var textOffset: Float = 0f

    //we文字大小
    var numberTextSize = 0f
    var textSize = 0f

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        context ?: return
        val attributeSet = context.obtainStyledAttributes(attrs, R.styleable.StepsView)
        ringWidth = attributeSet.getDimension(R.styleable.StepsView_ringWidth, DisplayUtil.dp2px(context, 16f).toFloat())
        numberTextSize = attributeSet.getDimension(R.styleable.StepsView_numberTextSize, ringWidth*3f)
        textSize = attributeSet.getDimension(R.styleable.StepsView_textSize, ringWidth*1.5f)
        attributeSet.recycle()

        init()
    }


    fun init() {
        ringPaint = Paint()
        ringPaint.isAntiAlias = true
        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = ringWidth

        numberPaint = Paint()
        numberPaint.isAntiAlias = true
        numberPaint.setColor(Color.WHITE)
        numberPaint.textSize = numberTextSize
        numberPaint.setTextAlign(Paint.Align.CENTER)
        val numberFontMetrics = Paint.FontMetrics()
        numberPaint.getFontMetrics(numberFontMetrics)
        numberOffset = (numberFontMetrics.descent + numberFontMetrics.ascent) / 2

        textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.setColor(Color.WHITE)
        textPaint.textSize = textSize
        textPaint.setTextAlign(Paint.Align.CENTER)
        val textFontMetrics = Paint.FontMetrics()
        textPaint.getFontMetrics(textFontMetrics)
        textOffset = (textFontMetrics.descent + textFontMetrics.ascent) / 2

    }

    fun setCurrentSteps(current: Int) {
        currentSteps = current
        postInvalidate()
    }

    fun getCurrentSteps(): Int {
        return currentSteps
    }

    fun setMaxSteps(max: Int) {
        maxSteps = max
        postInvalidate()
    }

    fun getMaxSteps(): Int {
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
        if (progress > 360) {
            progress = 360f
        }
        canvas.drawArc(left, top, right, bottom, -90f, progress, false, ringPaint)

        //画步数文字
        canvas.drawText(currentSteps.toString(), measuredWidth / 2f, measuredHeight / 2f - numberOffset, numberPaint)

        val textHeight = ((measuredHeight / 2f - numberOffset - top) / 2) - textOffset
        canvas.drawText("步数", measuredWidth / 2f, textHeight, textPaint)
    }
}