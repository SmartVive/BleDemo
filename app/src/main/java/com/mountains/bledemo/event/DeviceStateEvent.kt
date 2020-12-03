package com.mountains.bledemo.event

class DeviceStateEvent(val deviceName:String, val type:Int) {
    companion object{
        const val CONNECTED_TYPE = 1
        const val CONNECT_FAIL_TYPE = 2
        const val DISCONNECT_TYPE = 3
        const val CONNECTING_TYPE = 4
    }
}