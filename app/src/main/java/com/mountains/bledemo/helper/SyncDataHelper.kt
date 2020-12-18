package com.mountains.bledemo.helper

import com.mountains.bledemo.bean.AlarmClockBean
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.service.DeviceConnectService
import com.mountains.bledemo.util.HexUtil
import com.orhanobut.logger.Logger
import org.litepal.LitePal
import org.litepal.extension.find

/**
 * 同步设备数据
 */
class SyncDataHelper {
    var isSyncDeviceInfoData = true
    var isSyncRealTimeSportData = true
    var isSyncHistorySportData = true
    var isSyncHistorySleepData = true
    var isSyncHeartRateData = true
    var isSyncDeviceSetting = true

    fun startSync() {
        isSyncDeviceInfoData = true
        isSyncRealTimeSportData = true
        isSyncHistorySportData = true
        isSyncHistorySleepData = true
        isSyncHeartRateData = true
        //isSyncDeviceSetting = true
        syncTime()
    }

    //同步设备时间
    private fun syncTime(){
        DeviceManager.writeCharacteristic(CommHelper.setDeviceTime(),object : CommCallback {
            override fun onSuccess(byteArray: ByteArray?) {
                Logger.i("同步时间成功")
            }

            override fun onFail(exception: BleException) {
                Logger.i("同步时间失败：${exception.message}")
            }

        })
    }


    fun decode(bArr: ByteArray) {
        //同步时间完成后同步设备信息
        if (isSyncDeviceInfoData && HexUtil.bytes2HexString(bArr).startsWith("05020101")) {
            isSyncDeviceInfoData = false
            DeviceManager.writeCharacteristic(CommHelper.getDeviceInfo())
        }


        //同步时间完成后同步实时步数
        if (isSyncRealTimeSportData && HexUtil.bytes2HexString(bArr).startsWith("050102",true)) {
            isSyncRealTimeSportData = false
            DeviceManager.writeCharacteristic(CommHelper.getRealTimeSportData())
        }


        //同步实时步数完成后同步步数大数据
        if (isSyncHistorySportData && HexUtil.bytes2HexString(bArr).startsWith("05070100")) {
            isSyncHistorySportData = false
            DeviceManager.writeCharacteristic(CommHelper.getHistorySportData())
        }


        //同步步数大数据完成后同步睡眠大数据
        if (isSyncHistorySleepData && HexUtil.bytes2HexString(bArr).startsWith("0507ff01", true)) {
            isSyncHistorySleepData = false
            DeviceManager.writeCharacteristic(CommHelper.getHistorySleepData())
        }


        //同步睡眠大数据完成后同步 心率大数据
        if (isSyncHeartRateData && HexUtil.bytes2HexString(bArr).startsWith("0507FE01", true)) {
            isSyncHeartRateData = false
            DeviceManager.writeCharacteristic(CommHelper.getHistoryHeartRateData())
        }


        //同步心率大数据完成后同步设备设置
        if (isSyncDeviceSetting && HexUtil.bytes2HexString(bArr).startsWith("0507FD",true)){
            isSyncDeviceSetting = false

            //同步抬腕亮屏和自动检测心率
            val liftWristBrightScreen = DeviceStorage.getInstance().isLiftWristBrightScreen
            val autoHeartRateDetection = DeviceStorage.getInstance().isAutoHeartRateDetection
            DeviceManager.writeCharacteristic(CommHelper.setDeviceOtherInfo(liftWristBrightScreen,false,autoHeartRateDetection))

            //同步闹钟
            val mac = DeviceManager.getDevice()?.getMac() ?: return
            val alarmCLockList = LitePal.where("mac = ?", mac).find<AlarmClockBean>()
            alarmCLockList.forEach {
                it.isOpen = it.isOpen
                val index = it.index
                val isOpenInt = if (it.isOpen) { 1 } else { 0 }
                var repeat = 0
                val hour = it.hour
                val minute = it.minute

                for (i in it.repeat.indices) {
                    if (it.repeat[i] == 1) {
                        repeat = repeat or 1 shl i
                    }
                }
                if (repeat == 0) {
                    repeat = 128
                }
                DeviceManager.writeCharacteristic(CommHelper.setAlarmClock(index,isOpenInt,repeat,hour,minute,0))
            }
        }
    }
}