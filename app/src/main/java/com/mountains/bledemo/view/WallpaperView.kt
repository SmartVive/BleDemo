package com.mountains.bledemo.view

import com.mountains.bledemo.base.BaseView

interface WallpaperView : BaseView {

    fun onUploadWallpaperProgress(current:Int, total:Int)

    fun onUploadStart()

    fun onUploadStop()
}