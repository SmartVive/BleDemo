package com.mountains.bledemo.ble.callback

interface NotifyCallback {

    fun onNotify(uuid:String,byteArray: ByteArray?)

}