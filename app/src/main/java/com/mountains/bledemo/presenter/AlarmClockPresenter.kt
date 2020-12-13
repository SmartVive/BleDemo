package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.AlarmClockBean
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.helper.DeviceStorage
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.AlarmClockView
import org.litepal.LitePal
import org.litepal.extension.find
import java.util.*

class AlarmClockPresenter : BasePresenter<AlarmClockView>() {

    fun getAlarmClockList(){
        val mac = DeviceStorage.getInstance().mac
        if (mac == null){
            view?.showToast("获取闹钟失败：获取设备mac失败")
            return
        }
        val alarmClockList = LitePal.where("mac = ?", mac).find<AlarmClockBean>(true)
        view?.onAlarmClockList(alarmClockList)
    }


    fun switchAlarmClock(alarmClock:AlarmClockBean,isOpen:Boolean){
        alarmClock.assignBaseObjId(alarmClock.id)
        alarmClock.isOpen = isOpen
        //alarmClock.date = getAlarmClockDate(alarmClock.hour,alarmClock.minute)
        val index = alarmClock.index
        val isOpenInt = if (isOpen){1}else{0}
        val hour = alarmClock.hour
        val minute = alarmClock.minute
        var repeat = 0
        for (i in alarmClock.repeat.indices) {
            if (alarmClock.repeat[i] == 1) {
                repeat = repeat or 1 shl i
            }
        }
        if (repeat == 0) {
            repeat = 128
        }


        DeviceManager.writeCharacteristic(CommHelper.setAlarmClock(index,isOpenInt,repeat,hour,minute,0),object : CommCallback{
            override fun onSuccess(byteArray: ByteArray?) {
                alarmClock.save()
                view?.onSwitchAlarmClockSuccess(alarmClock.isOpen,alarmClock.getDistanceNextAlarmTimeString())
            }

            override fun onFail(exception: BleException) {
                view?.showToast("切换失败：${exception.message}")
            }

        })
    }

    fun deleteAlarmClock(position:Int,alarmClock: AlarmClockBean){
        //先关闭后删除数据库
        alarmClock.assignBaseObjId(alarmClock.id)
        DeviceManager.writeCharacteristic(CommHelper.setAlarmClock(alarmClock.index,0,0,0,0,0),object : CommCallback{
            override fun onSuccess(byteArray: ByteArray?) {
                alarmClock.delete()
                view?.onDeleteAlarmClockSuccess(position)
            }

            override fun onFail(exception: BleException) {
                view?.showToast("删除失败：${exception.message}")
            }

        })
    }

    private fun getAlarmClockDate(hour:Int,minute:Int):Long{
        val alarmClockCalendar = CalendarUtil.getTodayBeginCalendar()
        alarmClockCalendar.set(Calendar.HOUR_OF_DAY,hour)
        alarmClockCalendar.set(Calendar.MINUTE,minute)
        val currentCalendar = CalendarUtil.getCurrentCalendar()
        val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentCalendar.get(Calendar.MINUTE)
        val alarmClockHour = alarmClockCalendar.get(Calendar.HOUR_OF_DAY)
        val alarmClockMinute = alarmClockCalendar.get(Calendar.MINUTE)
        if (currentHour*60+currentMinute >= alarmClockHour*60+alarmClockMinute){
            //超过今天的时间，设置为明天的闹钟
            alarmClockCalendar.add(Calendar.DAY_OF_MONTH,1)
        }
        return alarmClockCalendar.timeInMillis
    }
}