package com.mountains.bledemo.helper

import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.service.DeviceConnectService

object DeviceManager {

    fun writeCharacteristic(data: ByteArray,callback: CommCallback? = null){
        DeviceConnectService.connectedDevice?.writeCharacteristic(BaseUUID.SERVICE,BaseUUID.WRITE,data,callback)
    }
}