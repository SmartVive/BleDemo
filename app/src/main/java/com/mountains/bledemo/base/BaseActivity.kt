package com.mountains.bledemo.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mountains.bledemo.util.ToastUtil

abstract class BaseActivity<P : BasePresenter<*>> : AppCompatActivity(),BaseView {
    lateinit var presenter:P

    abstract  fun createPresenter(): P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = createPresenter()
        presenter.attachView(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun showError() {

    }

    override fun showToast(message: String) {
        ToastUtil.show(message)
    }
}