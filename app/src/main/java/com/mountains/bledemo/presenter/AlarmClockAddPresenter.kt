package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.AlarmClockBean
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.helper.DeviceStorage
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.AlarmClockAddView
import org.litepal.LitePal
import org.litepal.extension.find
import java.util.*

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
            for (i in alarmClockList.indices){
                if (index == alarmClockList[i].index){
                    index++
                }else{
                    break
                }
            }

            val repeat = getRepeat(repeatArray)
            val alarmClockBean = createAlarmClockBean(mac, index, hour, minute, repeatArray)



            writeToDevice(index, repeat, hour, minute, alarmClockBean)
        }
    }

    fun updateAlarmClock(id:Long,index:Int,hour:Int,minute:Int,repeatArray:MutableList<Boolean>){
        val mac = DeviceStorage.getInstance().mac
        if (mac == null){
            view?.showToast("添加失败：获取设备mac失败")
            return
        }

        val repeat = getRepeat(repeatArray)
        val alarmClockBean = createAlarmClockBean(mac,index, hour, minute,  repeatArray)
        alarmClockBean.assignBaseObjId(id)


        writeToDevice(index, repeat, hour, minute, alarmClockBean)
    }

    private fun writeToDevice(index: Int,repeat:Int,hour: Int,minute: Int,alarmClockBean: AlarmClockBean){
        DeviceManager.writeCharacteristic(CommHelper.setAlarmClock(index, 1, repeat, hour, minute, 0),
            object : CommCallback {
                override fun onSuccess(byteArray: ByteArray?) {
                    alarmClockBean.save()
                    val distanceNextAlarmTimeString = alarmClockBean.getDistanceNextAlarmTimeString()
                    view?.onAddAlarmClockSuccess(distanceNextAlarmTimeString)
                }

                override fun onFail(exception: BleException) {
                    view?.showToast("添加失败：${exception.message}")
                }
            })
    }


    private fun createAlarmClockBean(mac:String,index: Int,hour: Int,minute: Int,repeatArray: MutableList<Boolean>):AlarmClockBean{
        val alarmClockBean = AlarmClockBean()
        alarmClockBean.index = index
        alarmClockBean.hour = hour
        alarmClockBean.minute = minute
        alarmClockBean.mac = mac
        alarmClockBean.isOpen = true
        //alarmClockBean.date = getAlarmClockDate(hour, minute)
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
        return alarmClockBean
    }

    private fun getRepeat(repeatArray:MutableList<Boolean>):Int{
        var repeat = 0

        for (i in repeatArray.indices) {
            if (repeatArray[i]) {
                repeat = repeat or 1 shl i
            }
        }
        if (repeat == 0) {
            repeat = 128
        }
        return repeat
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