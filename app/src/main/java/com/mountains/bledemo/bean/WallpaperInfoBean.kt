package com.mountains.bledemo.bean

import android.graphics.Bitmap
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

class WallpaperInfoBean:LitePalSupport() {
    var id:Long = 0
    var mac: String? = null

    @Column(ignore = true)
    var bitmap: Bitmap? = null
    var bitmapPath : String? = null

    //是否启用壁纸
    var enableWallpaper = true

    //是否显示时间
    var isTimeEnable = true

    //时间颜色
    var fontColor = 0

    //时间坐标
    var timeLocationX = 0
    var timeLocationY = 0

    //时间字体坐标
    var timeFontWidth = 0
    var timeFontHeight = 0


    //是否显示步数
    var isStepEnable = false

    //步数坐标
    var stepLocationX = 0
    var stepLocationY = 0

    //步数字体坐标
    var stepFontWidth = 0
    var stepFontHeight = 0

}