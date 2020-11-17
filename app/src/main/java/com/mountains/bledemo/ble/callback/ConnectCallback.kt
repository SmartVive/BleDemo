package com.mountains.bledemo.ble.callback

import com.mountains.bledemo.ble.BleDevice
import com.mountains.bledemo.ble.BleException

interface ConnectCallback {

    //连接成功
    fun connectSuccess(bleDevice: BleDevice)

    //连接失败
    fun connectFail(exception: BleException)

    //断开连接
    fun disconnect()
}