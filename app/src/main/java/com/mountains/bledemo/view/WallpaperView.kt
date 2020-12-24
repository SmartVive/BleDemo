package com.mountains.bledemo.view

import android.graphics.Bitmap
import android.graphics.Point
import com.mountains.bledemo.base.BaseView

interface WallpaperView : BaseView {

    fun initSuccess()

    fun initFail()

    fun onUploadWallpaperProgress(current: Int, total: Int)

    fun onUploadStart()

    fun onUploadStop()

    fun onWallpaperInfo(
        wallpaperBitmap: Bitmap?,
        screenWidth: Int,
        screenHeight: Int,
        isSupportWallpaper: Boolean,
        isWallpaperEnable: Boolean,
        isTimeEnable: Boolean,
        isStepEnable: Boolean,
        timeFontSize: IntArray?,
        stepFontSize: IntArray?,
        fontColor : Int,
        timeLocation:Point?
    )
}