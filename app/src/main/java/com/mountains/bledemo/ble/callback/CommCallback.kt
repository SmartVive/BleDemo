package com.mountains.bledemo.ble.callback

import android.os.Parcelable
import com.mountains.bledemo.ble.BleException

interface CommCallback {
    //通信成功
    fun onSuccess(byteArray: ByteArray?)

    //通信失败
    fun onFail(exception: BleException)
}