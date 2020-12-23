package com.mountains.bledemo

import android.app.Application
import android.content.Context
import com.billy.android.loading.Gloading
import com.mountains.bledemo.adapter.GlobalAdapter
import com.mountains.bledemo.ble.BleManager
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.litepal.LitePal


class App : Application() {
    companion object{
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        Logger.addLogAdapter(AndroidLogAdapter())
        LitePal.initialize(this);
        val db = LitePal.getDatabase()
        BleManager.getInstance().init(this)
        Gloading.initDefault(GlobalAdapter())
    }

}