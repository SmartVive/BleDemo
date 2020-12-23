package com.mountains.bledemo.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.billy.android.loading.Gloading
import com.mountains.bledemo.util.ToastUtil


abstract class BaseFragment<P:BasePresenter<*>>:Fragment(),BaseView {
    lateinit var presenter : P
    private lateinit var holder:Gloading.Holder

    abstract fun createPresenter():P

    @LayoutRes
    abstract fun setContentView():Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = createPresenter()
        presenter.attachView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflate = inflater.inflate(setContentView(), container, false)
        holder = Gloading.getDefault().wrap(inflate)
        return holder.wrapper
    }

    override fun showToast(message: String) {
        ToastUtil.show(message)
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun showError() {

    }

}