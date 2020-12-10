package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.AlarmClockBean
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.helper.DeviceStorage
import com.mountains.bledemo.view.AlarmClockAddView
import org.litepal.LitePal
import org.litepal.extension.find

class AlarmClockAddPresenter : BasePresenter<AlarmClockAddView>() {

    fun addAlarmClock(hour:Int,minute:Int,repeatArray:MutableList<Boolean>){
        val mac = DeviceStorage.getInstance().mac
        if (mac == null){
            view?.showToast("添加失败：获取设备mac失败")
            return
        }
        val alarmClockList = LitePal.where("mac = ?", mac).order("index").find<AlarmClockBean>(true)
        if (alarmClockList.size >= 5){
            view?.showToast("最多添加五个闹钟")
        }else{
            var index = 0
            var repeat = 0

            for (i in alarmClockList.indices){
                if (index == alarmClockList[i].index){
                    index++
                }else{
                    break
                }
            }

            for (i in repeatArray.indices) {
                if (repeatArray[i]) {
                    repeat = repeat or 1 shl i
                }
            }
            if (repeat == 0) {
                repeat = 128
            }

            val alarmClockBean = AlarmClockBean()
            alarmClockBean.index = index
            alarmClockBean.hour = hour
            alarmClockBean.minute = minute
            alarmClockBean.mac = mac
            alarmClockBean.isOpen = true
            alarmClockBean.repeat = repeatArray.let {
                val list = mutableListOf<Int>()
                it.forEach {
                    if (it){
                        list.add(1)
                    }else{
                        list.add(0)
                    }
                }
                list
            }


            DeviceManager.writeCharacteristic(CommHelper.setAlarmClock(index,1,repeat,hour,minute,0),object : CommCallback{
                override fun onSuccess(byteArray: ByteArray?) {
                    alarmClockBean.save()
                    view?.onAddAlarmClockSuccess()
                }

                override fun onFail(exception: BleException) {
                    view?.showToast("添加失败：${exception.message}")
                }
            })
        }
    }


}