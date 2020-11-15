package com.mountains.bledemo

import android.app.Application
import android.content.Context
import com.mountains.bledemo.ble.BleManager
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class App : Application() {
    companion object{
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        BleManager.getInstance().init(this)
        Logger.addLogAdapter(AndroidLogAdapter())
    }

}