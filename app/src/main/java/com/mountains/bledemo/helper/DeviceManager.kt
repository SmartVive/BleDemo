package com.mountains.bledemo.helper

import com.mountains.bledemo.ble.BleDevice
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.service.DeviceConnectService

object DeviceManager {
    private var bleDevice:BleDevice? = null

    fun setDevice(bleDevice: BleDevice?){
        this.bleDevice = bleDevice
    }

    fun getDevice():BleDevice?{
        return bleDevice
    }

    fun writeCharacteristic(data: ByteArray,callback: CommCallback? = null){
        if (bleDevice != null && bleDevice!!.isConnected()){
            bleDevice?.writeCharacteristic(BaseUUID.SERVICE,BaseUUID.WRITE,data,callback)
        }else{
            callback?.onFail(BleException(BleException.DEVICE_NOT_CONNECTED,"设备未连接！"))
        }
    }

    fun writeWallpaperCharacteristic(data: ByteArray,callback: CommCallback? = null){
        if (bleDevice != null && bleDevice!!.isConnected()){
            bleDevice?.writeCharacteristic(BaseUUID.SERVICE,BaseUUID.WRITE_WALLPAPER,data,callback)
        }else{
            callback?.onFail(BleException(BleException.DEVICE_NOT_CONNECTED,"设备未连接！"))
        }
    }
}