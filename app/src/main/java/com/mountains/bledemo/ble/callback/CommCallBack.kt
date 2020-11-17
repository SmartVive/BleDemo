package com.mountains.bledemo.ble.callback

import com.mountains.bledemo.ble.BleException

interface CommCallBack {
    //通信成功
    fun onSuccess(byteArray: ByteArray?)

    //通信失败
    fun onFail(exception: BleException)
}