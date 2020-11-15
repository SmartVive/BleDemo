package com.mountains.bledemo.util

import android.widget.Toast
import com.mountains.bledemo.App

object ToastUtil {

    fun show(message:String){
        val toast =  Toast.makeText(App.context,null, Toast.LENGTH_LONG)
        toast.setText(message)
        toast.show()
    }
}