package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView

interface WallpaperView : BaseView {

    fun initSuccess()

    fun initFail()

    fun onUploadWallpaperProgress(current: Int, total: Int)

    fun onUploadStart()

    fun onUploadStop()

    fun onWallpaperInfo(
        screenWidth: Int,
        screenHeight: Int,
        isSupportWallpaper: Boolean,
        isWallpaperEnable: Boolean,
        isTimeEnable: Boolean,
        isStepEnable: Boolean,
        timeFontSize: IntArray?,
        stepFontSize: IntArray?
    )
}