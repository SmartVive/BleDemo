package com.mountains.bledemo.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.WallpaperInfoBean
import com.mountains.bledemo.presenter.WallpaperPresenter
import com.mountains.bledemo.view.WallpaperView
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.acitivy_line_chart.btnChoseImage
import kotlinx.android.synthetic.main.acitivy_line_chart.wallpaperView
import kotlinx.android.synthetic.main.activity_wallpaper.*
import java.io.File
import java.text.DecimalFormat

class WallpaperActivity : BaseActivity<WallpaperPresenter>(),WallpaperView {
    private var progressAlertDialog:AlertDialog? = null


    companion object{
        const val CHOSE_PHOTO_REQUEST_CODE = 100
    }


    override fun createPresenter(): WallpaperPresenter {
        return WallpaperPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper)
        initView()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopUploadWallpaper()
    }

    private fun initView(){
        btnChoseImage.setOnClickListener {
            //打开相册
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, CHOSE_PHOTO_REQUEST_CODE) // 打开相册
        }

        btnSave.setOnClickListener {
            /*val bitmap = wallpaperView.bitmap
            if (bitmap == null){
                showToast("请设置壁纸")
            }else{
                presenter.setWallpaper(bitmap)
            }*/
            val timeLocation = wallpaperView.getTimeLocation()
            val wallpaperInfoBean = WallpaperInfoBean().apply {
                enableWallpaper = true
                isTimeEnable = true
                timeFontSizeX = 20
                timeFontSizeY = 24
                fontColor = -16777216
                timeLocationX = timeLocation.x
                timeLocationY = timeLocation.y
            }
            presenter.setWallpaper(wallpaperInfoBean)
        }
    }

    private fun initData(){
        presenter.getWallpaperInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == CHOSE_PHOTO_REQUEST_CODE){
                data?.data?.let {
                    val destinationUri = Uri.fromFile(File(cacheDir,"${System.currentTimeMillis()}.jpg"))
                    val options = UCrop.Options()
                    options.setShowCropFrame(false)
                    options.setHideBottomControls(true)
                    options.setToolbarColor(Color.WHITE)
                    options.setToolbarWidgetColor(Color.BLACK)
                    options.setShowCropGrid(false)
                    options.setCircleDimmedLayer(true)
                    UCrop.of(it, destinationUri)
                        .withAspectRatio(1f,1f)
                        .withOptions(options)
                        .start(this)
                }
            }else if (requestCode == UCrop.REQUEST_CROP && data != null){
                val resultUri = UCrop.getOutput(data)
                resultUri?.let {
                    wallpaperView.setWallpaper(it)
                }
            }
        }
    }

    private fun getProgressAlertDialog():Dialog{
        if (progressAlertDialog == null){
            progressAlertDialog = AlertDialog.Builder(getContext())
                .setView(R.layout.dialog_wallpaper)
                .setCancelable(false)
                .setTitle("正在设置壁纸")
                .create()
        }
        return progressAlertDialog!!
    }


    override fun onUploadStart() {
        getProgressAlertDialog().show()
    }

    override fun onUploadStop() {
        getProgressAlertDialog().dismiss()
    }

    override fun onUploadWallpaperProgress(current: Int, total: Int) {
        val progressBar = getProgressAlertDialog().findViewById<ProgressBar>(R.id.progressBar)
        val tvProgress = getProgressAlertDialog().findViewById<TextView>(R.id.tvProgress)

        progressBar.max = total
        progressBar.progress = current
        val decimalFormat = DecimalFormat("0.00%")
        tvProgress.text = decimalFormat.format(current.toFloat() / total.toFloat())
    }
}