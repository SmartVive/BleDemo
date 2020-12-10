package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.AlarmClockBean
import com.mountains.bledemo.helper.DeviceStorage
import com.mountains.bledemo.view.AlarmClockView
import org.litepal.LitePal
import org.litepal.extension.find

class AlarmClockPresenter : BasePresenter<AlarmClockView>() {

    fun getAlarmClockList(){
        val mac = DeviceStorage.getInstance().mac
        if (mac == null){
            view?.showToast("获取闹钟失败：获取设备mac失败")
            return
        }
        val alarmClockList = LitePal.where("mac = ?", mac).order("index").find<AlarmClockBean>(true)
        view?.onAlarmClockList(alarmClockList)
    }
}