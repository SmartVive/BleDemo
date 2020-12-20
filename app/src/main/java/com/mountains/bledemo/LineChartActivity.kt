package com.mountains.bledemo

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.acitivy_line_chart.*
import java.io.File


class LineChartActivity : AppCompatActivity() {

    companion object{
        const val CHOSE_PHOTO_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivy_line_chart)

        initView()
    }

    private fun initView(){
        btnChoseImage.setOnClickListener {
            //打开相册
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, CHOSE_PHOTO_REQUEST_CODE) // 打开相册
        }
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
                    wallpaperView.setImage(it)
                }
            }
        }
    }
}