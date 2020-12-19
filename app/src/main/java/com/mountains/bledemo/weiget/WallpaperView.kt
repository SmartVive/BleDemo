package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.orhanobut.logger.Logger

class WallpaperView : View {
    var bitmap : Bitmap? = null
    private val bitmapPaint = Paint()
    private val timePaint = Paint()
    private val bitmapSrc = Rect()
    private val bitmapDst = Rect()
    private val timeBounds = Rect()
    private val clipPath = Path()


    val timeText = "12:00"
    private var timeRectF = RectF()
    private var timeCanMove = false
    private var downX = 0f
    private var downY = 0f

    constructor(context: Context?) : this(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    private fun init(){
        bitmapPaint.isAntiAlias = true
        timePaint.isAntiAlias = true
        timePaint.color = Color.WHITE
        timePaint.textSize = dp2px(40f)
        timePaint.isFakeBoldText = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setClipPath(w/2f,h/2f+h*0.1f)
        timePaint.getTextBounds(timeText,0,timeText.length,timeBounds)
        val timeLeft =  w/2f - timeBounds.width()
        val timeTop =  h/2f 
        val timeRight =  timeLeft + timeBounds.width()
        val timeBottom =  timeTop + timeBounds.height()
        timeRectF.set(timeLeft,timeTop,timeRight,timeBottom)
    }

    fun setImage(uri:Uri){
        bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        bitmap?.let {
            bitmapSrc.top = 0
            bitmapSrc.left = 0
            bitmapSrc.set(0,0,it.width,it.height)
            bitmapDst.set(0,0,measuredWidth,measuredHeight)
        }

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                downX = event.x
                downY = event.y
                if (timeRectF.contains(downX,downY)){
                    //触摸到了时间，时间变为可移动
                    Logger.d("触摸到了时间")
                    timeCanMove = true
                    return true
                }
            }
            MotionEvent.ACTION_MOVE->{
                if (timeCanMove){
                    val distanceX = event.x - downX
                    val distanceY = event.y - downY
                    timeRectF.left += distanceX
                    timeRectF.top += distanceY
                    timeRectF.right += distanceX
                    timeRectF.bottom += distanceY
                    invalidate()
                    downX = event.x
                    downY = event.y
                }
            }
            else->{
                timeCanMove = false
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap ?: return
        canvas.save()
        canvas.clipPath(clipPath)
        canvas.drawBitmap(bitmap!!,bitmapSrc,bitmapDst,bitmapPaint)

        canvas.drawText(timeText,timeRectF.centerX(),timeRectF.centerY(),timePaint)
    }

    private fun setClipPath(cx:Float,cy:Float){
        val radius = Math.sqrt((cx*cx+(measuredHeight-cy)*(measuredHeight-cy)).toDouble()).toFloat()
        clipPath.addCircle(cx,cy,radius,Path.Direction.CCW)
    }



    private fun dp2px(dpValue: Float): Float {
        val density = resources.displayMetrics.density
        return (dpValue * density + 0.5f)
    }
}