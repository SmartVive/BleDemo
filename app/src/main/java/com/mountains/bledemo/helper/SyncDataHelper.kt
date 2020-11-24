package com.mountains.bledemo.helper

import com.mountains.bledemo.service.DeviceConnectService
import com.mountains.bledemo.util.HexUtil

/**
 * 同步设备数据
 */
class SyncDataHelper {


    fun decode(bArr: ByteArray){
        //同步时间完成后同步实时步数
        if(HexUtil.bytes2HexString(bArr).startsWith("05020101")){
            DeviceManager.writeCharacteristic(CommHelper.getRealTimeSportData())
        }

        //同步实时步数完成后同步步数大数据
        if(HexUtil.bytes2HexString(bArr).startsWith("05070100")){
            DeviceManager.writeCharacteristic(CommHelper.getHistorySportData())
        }

        //同步步数大数据完成后同步睡眠大数据
        if(HexUtil.bytes2HexString(bArr).startsWith("0507ff01",true)){
            DeviceManager.writeCharacteristic(CommHelper.getHistorySleepData())
        }

        //同步睡眠大数据完成后同步 心率大数据
        if(HexUtil.bytes2HexString(bArr).startsWith("0507FE01",true)){
            DeviceManager.writeCharacteristic(CommHelper.getHistoryHeartRateData())
        }
    }
}