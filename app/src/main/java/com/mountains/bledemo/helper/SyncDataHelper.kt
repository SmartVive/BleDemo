package com.mountains.bledemo.helper

import com.mountains.bledemo.service.DeviceConnectService
import com.mountains.bledemo.util.HexUtil

/**
 * 同步设备数据
 */
class SyncDataHelper {
    var isSyncDeviceInfoData = true
    var isSyncRealTimeSportData = true
    var isSyncHistorySportData = true
    var isSyncHistorySleepData = true
    var isSyncHeartRateData = true

    fun startSync() {
        isSyncDeviceInfoData = true
        isSyncRealTimeSportData = true
        isSyncHistorySportData = true
        isSyncHistorySleepData = true
        isSyncHeartRateData = true
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

    }
}