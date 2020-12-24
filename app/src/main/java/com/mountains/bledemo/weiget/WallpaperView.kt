package com.mountains.bledemo.weiget

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toRect
import com.mountains.bledemo.R
import com.orhanobut.logger.Logger

class WallpaperView : View {
    var bitmap : Bitmap? = null
    private val bitmapPaint = Paint()
    private val timePaint = Paint()
    private val stepPaint = Paint()
    private val stepBitmapPaint = Paint()
    private val bitmapSrc = Rect()
    private val bitmapDst = Rect()
    private val clipPath = Path()

    private var wallpaperWidth = 240
    private var wallpaperHeight = 240
    var fontColor = Color.WHITE

    //时间
    val timeText = "12:00"
    var timeWidth = 20
        private set
    var timeHeight = 24
        private set
    private var timeRectF = RectF()
    private val timeBounds = Rect()
    private var timeCanMove = false


    //步数
    val stepText = "03450"
    private var isStepShow = true
    var stepWidth = 10
        private set
    var stepHeight = 12
        private set
    private var stepMarginTop = 10
    private var stepRectF = RectF()
    private val stepBounds = Rect()
    private val stepBitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.ic_wallpaper_step) }
    private val stepBitmapSrc = Rect()
    private val stepBitmapDst = RectF()


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
        stepBitmapPaint.isAntiAlias = true
        stepBitmapSrc.set(0,0,stepBitmap.width,stepBitmap.height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clipPath.addCircle(w/2f,h/2f+h*0.071f,w*0.6f,Path.Direction.CCW)
        region.setPath(clipPath,Region(0,0,w,h))
        calculateWidget(w, h)
    }

    private fun calculateWidget(w:Int,h: Int){
        //计算timeTextSize
        timePaint.textSize = 1f
        while (timePaint.measureText(timeText) < w / wallpaperWidth * timeWidth * timeText.length){
            timePaint.textSize++
        }

        stepPaint.textSize = 1f
        while (stepPaint.measureText(stepText) < w / wallpaperWidth * stepWidth * stepText.length){
            stepPaint.textSize++
        }

        timePaint.getTextBounds(timeText,0,timeText.length,timeBounds)
        val timeTextWidth = timePaint.measureText(timeText)
        //val timeLeft =  w/2f - timeTextWidth/2
        //val timeTop =  h/2f - timeBounds.height()/2
        val timeRight =  timeLeft + timeTextWidth
        val timeBottom =  timeTop + timeBounds.height()
        timeRectF.set(timeLeft,timeTop,timeRight,timeBottom)


        stepPaint.getTextBounds(stepText,0,stepText.length,stepBounds)
        val stepTextWidth = stepPaint.measureText(stepText)
        val stepRight =  timeRight
        val stepLeft =  stepRight - stepTextWidth
        val stepTop =  timeBottom + stepMarginTop
        val stepBottom =  stepTop + stepBounds.height()
        stepRectF.set(stepLeft,stepTop,stepRight,stepBottom)

        val stepBitmapSize = stepBounds.height()
        val stepBitmapLeft = stepLeft - stepBitmapSize - 4
        val stepBitmapTop = stepTop
        val stepBitmapRight = stepBitmapLeft + stepBitmapSize
        val stepBitmapBottom = stepBitmapTop + stepBitmapSize

        stepBitmapDst.set(stepBitmapLeft,stepBitmapTop,stepBitmapRight,stepBitmapBottom)
    }

    fun setWallpaper(bitmap: Bitmap){
        this.bitmap = bitmap
        bitmap.let {
            bitmapSrc.set(0,0,it.width,it.height)
            bitmapDst.set(0,0,measuredWidth,measuredHeight)
        }
        invalidate()
    }

    fun setWallpaper(uri:Uri){
        bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        bitmap?.let {
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

    fun getStepLocation():Point{
        val x = (wallpaperWidth.toFloat() / width.toFloat() * stepRectF.left).toInt()
        val y = (wallpaperHeight.toFloat() / height.toFloat() * stepRectF.top).toInt()
        return Point(x,y)
    }

    var timeLeft: Float = 0f
    var timeTop: Float = 0f
    fun setTimeLocation(point: Point?) {
        post {
            if (point != null){
                timeLeft = width.toFloat() / wallpaperWidth.toFloat() * point.x
                timeTop =  height.toFloat() / wallpaperHeight.toFloat() * point.y
                calculateWidget(width,height)
                if (!isContainsClip()){
                    resetWidgetLocation()
                }
            }else{
                resetWidgetLocation()
            }
            invalidate()
        }
    }

    private fun resetWidgetLocation(){
        timePaint.getTextBounds(timeText,0,timeText.length,timeBounds)
        val timeTextWidth = timePaint.measureText(timeText)
        timeLeft =  width/2f - timeTextWidth/2
        timeTop =  height/2f - timeBounds.height()/2
        calculateWidget(width,height)
    }

    fun setWallpaperSize(width: Int,height: Int){
        wallpaperWidth = width
        wallpaperHeight = height
    }

    fun setTimeSize(width:Int,height:Int){
        timeWidth = width
        timeHeight = height
    }


    fun setStepSize(width: Int,height: Int){
        stepWidth = width
        stepHeight = height
    }

    fun setStepShow(isShow:Boolean){
        post {
            isStepShow = isShow
            if (!isContainsClip()){
                resetWidgetLocation()
            }
            invalidate()
        }

    }


    fun setColor(color:Int){
        fontColor = color
        timePaint.color = color
        stepPaint.color = color
        stepBitmapPaint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                downX = event.x
                downY = event.y
                if (timeRectF.contains(downX,downY) || (isStepShow && stepRectF.contains(downX,downY))){
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

                    if (isContainsClip(distanceX,distanceY)){
                        timeRectF.offset(distanceX,distanceY)
                        stepRectF.offset(distanceX,distanceY)
                        stepBitmapDst.offset(distanceX,distanceY)

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

    //时间和步数是否在显示范围
    private fun isContainsClip(distanceX:Float = 0f,distanceY: Float = 0f):Boolean{
        val tempTimeRect = RectF(timeRectF)
        val tempStepRect = RectF(stepRectF)
        val tempStepBitmapRect = RectF(stepBitmapDst)
        tempTimeRect.offset(distanceX,distanceY)
        tempStepRect.offset(distanceX,distanceY)
        tempStepBitmapRect.offset(distanceX,distanceY)
        val textRegion = Region()
        textRegion.op(tempTimeRect.toRect(),Region.Op.UNION)
        if (isStepShow){
            textRegion.op(tempStepRect.toRect(),Region.Op.UNION)
            textRegion.op(tempStepBitmapRect.toRect(),Region.Op.UNION)
        }
        tempRegion.set(region)
        //REVERSE_DIFFERENCE：时间区域和显示区域的并集减去显示区域，当为空说明时间区域在显示区域里面
        tempRegion.op(textRegion,Region.Op.REVERSE_DIFFERENCE)
        return tempRegion.isEmpty
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap ?: return
        canvas.save()
        canvas.clipPath(clipPath)
        canvas.drawBitmap(bitmap!!,bitmapSrc,bitmapDst,bitmapPaint)

        canvas.drawText(timeText,timeRectF.left,timeRectF.bottom,timePaint)

        if (isStepShow){
            canvas.drawText(stepText,stepRectF.left,stepRectF.bottom,stepPaint)
            canvas.drawBitmap(stepBitmap,stepBitmapSrc,stepBitmapDst,stepBitmapPaint)
        }
    }

    private fun dp2px(dpValue: Float): Float {
        val density = resources.displayMetrics.density
        return (dpValue * density + 0.5f)
    }
}