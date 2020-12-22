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
    private val stepPaint = Paint()
    private val bitmapSrc = Rect()
    private val bitmapDst = Rect()
    private val clipPath = Path()

    private var wallpaperWidth = 240
    private var wallpaperHeight = 240



    val timeText = "12:00"
    private var timeWidth = 100
    private var timeHeight = 24
    private var timeRectF = RectF()
    private val timeBounds = Rect()
    private var timeCanMove = false

    val stepText = "03450"
    private var isStepShow = true
    private var stepWidth = 50
    private var stepHeight = 12
    private var stepRectF = RectF()
    private val stepBounds = Rect()

    private var downX = 0f
    private var downY = 0f
    //边界数据
    private val region = Region()
    private val tempRegion = Region()

    constructor(context: Context?) : this(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    private fun init(){
        bitmapPaint.isAntiAlias = true
        timePaint.isAntiAlias = true
        timePaint.color = Color.BLACK
        timePaint.isFakeBoldText = true
        stepPaint.isAntiAlias = true
        stepPaint.color = Color.BLACK
        stepPaint.isFakeBoldText = true

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clipPath.addCircle(w/2f,h/2f+h*0.071f,w*0.6f,Path.Direction.CCW)
        region.setPath(clipPath,Region(0,0,w,h))

        //计算timeTextSize
        timePaint.textSize = 1f
        while (timePaint.measureText(timeText) < w / wallpaperWidth * timeWidth){
            timePaint.textSize++
        }

        stepPaint.textSize = 1f
        while (stepPaint.measureText(stepText) < w / wallpaperWidth * stepWidth){
            stepPaint.textSize++
        }

        timePaint.getTextBounds(timeText,0,timeText.length,timeBounds)
        val timeTextWidth = timePaint.measureText(timeText)
        val timeLeft =  w/2f - timeTextWidth/2
        val timeTop =  h/2f - timeBounds.height()/2
        val timeRight =  timeLeft + timeTextWidth
        val timeBottom =  timeTop + timeBounds.height()
        timeRectF.set(timeLeft,timeTop,timeRight,timeBottom)


        stepPaint.getTextBounds(stepText,0,stepText.length,stepBounds)
        val stepTextWidth = stepPaint.measureText(stepText)
        val stepRight =  timeRight
        val stepLeft =  stepRight - stepTextWidth
        val stepTop =  timeBottom + 10
        val stepBottom =  stepTop + stepBounds.height()
        stepRectF.set(stepLeft,stepTop,stepRight,stepBottom)

    }

    fun setWallpaper(uri:Uri){
        bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        bitmap?.let {
            bitmapSrc.top = 0
            bitmapSrc.left = 0
            bitmapSrc.set(0,0,it.width,it.height)
            bitmapDst.set(0,0,measuredWidth,measuredHeight)
        }

        invalidate()
    }

    fun getTimeLocation():Point{
        val x = (wallpaperWidth.toFloat() / width.toFloat() * timeRectF.left).toInt()
        val y = (wallpaperHeight.toFloat() / height.toFloat() * timeRectF.top).toInt()
        return Point(x,y)
    }

    fun setWallpaperSize(width: Int,height: Int){
        wallpaperWidth = width
        wallpaperHeight = height
    }

    fun setTimeSize(width:Int,height:Int){
        timeWidth = width
        timeHeight = height
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

                    val left = timeRectF.left + distanceX
                    val top =  timeRectF.top + distanceY
                    val right =  timeRectF.right + distanceX
                    //val bottom = timeRectF.bottom + distanceY

                    val bottom = if (isStepShow){
                        stepRectF.bottom + distanceY
                    }else{
                        timeRectF.bottom + distanceY
                    }

                    tempRegion.set(region)
                    //当时间超过显示区域则不重绘
                    //REVERSE_DIFFERENCE：时间区域和显示区域的并集减去显示区域，当为空说明时间区域在显示区域里面
                    tempRegion.op(left.toInt(),top.toInt(),right.toInt(),bottom.toInt(),Region.Op.REVERSE_DIFFERENCE)
                    if (tempRegion.isEmpty){
                        timeRectF.left += distanceX
                        timeRectF.top += distanceY
                        timeRectF.right += distanceX
                        timeRectF.bottom += distanceY

                        stepRectF.left += distanceX
                        stepRectF.top += distanceY
                        stepRectF.right += distanceX
                        stepRectF.bottom += distanceY

                        invalidate()
                    }
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

        canvas.drawText(timeText,timeRectF.left,timeRectF.bottom,timePaint)

        canvas.drawText(stepText,stepRectF.left,stepRectF.bottom,stepPaint)
    }

    private fun setClipPath(cx:Float,cy:Float,radius:Float){
        clipPath.addCircle(cx,cy,radius,Path.Direction.CCW)
    }



    private fun dp2px(dpValue: Float): Float {
        val density = resources.displayMetrics.density
        return (dpValue * density + 0.5f)
    }
}