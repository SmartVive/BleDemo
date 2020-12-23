package com.mountains.bledemo.base

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.billy.android.loading.Gloading
import com.mountains.bledemo.util.ToastUtil


abstract class BaseActivity<P : BasePresenter<*>> : AppCompatActivity(),BaseView {
    private lateinit var holder:Gloading.Holder
    lateinit var presenter:P

    abstract  fun createPresenter(): P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        holder = Gloading.getDefault().wrap(this)
        initWindow()
        presenter = createPresenter()
        presenter.attachView(this)
    }

    fun initWindow(){
        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.setStatusBarColor(Color.WHITE)
    }

    fun getContext(): Context = this

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    override fun showLoading() {
        holder.showLoading()
    }

    override fun hideLoading() {
        holder.showLoadSuccess()
    }

    override fun showError() {
        holder.showLoadFailed()
    }

    override fun showToast(message: String) {
        ToastUtil.show(message)
    }
}