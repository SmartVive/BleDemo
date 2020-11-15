package com.mountains.bledemo.base

interface BaseView {

    fun showLoading()

    fun hideLoading()

    fun showError()

    fun showToast(message:String)
}