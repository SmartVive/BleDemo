package com.mountains.bledemo.ui.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.presenter.WallpaperPresenter
import com.mountains.bledemo.view.WallpaperView

class WallpaperActivity : BaseActivity<WallpaperPresenter>(),WallpaperView {
    override fun createPresenter(): WallpaperPresenter {
        return WallpaperPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper)
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.setHighSpeedTransportStatus(false)
    }

    private fun initView(){
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.wallpaper)
        presenter.setWallpaper(bitmap)
    }

}