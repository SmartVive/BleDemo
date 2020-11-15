package com.mountains.bledemo.base

open class BasePresenter<V:BaseView>{
    var view:V? = null

    fun attachView(view: BaseView){
        this.view = view as V
    }

    fun destroy(){
        view = null
    }
}