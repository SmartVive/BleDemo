package com.mountains.bledemo.helper

import com.mountains.bledemo.ble.BleDevice
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.service.DeviceConnectService

object DeviceManager {
    private var bleDevice:BleDevice? = null

    fun setDevice(bleDevice: BleDevice){
        this.bleDevice = bleDevice
    }

    fun writeCharacteristic(data: ByteArray,callback: CommCallback? = null){
        bleDevice?.writeCharacteristic(BaseUUID.SERVICE,BaseUUID.WRITE,data,callback)
    }
}