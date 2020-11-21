package com.mountains.bledemo.ble

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message

object CallBackHandler {
    //连接成功
    const val CONNECT_SUCCESS_MSG = 100

    //连接失败
    const val CONNECT_FAIL_MSG = 200

    //断开连接
    const val DISCONNECT_MSG = 400

    //通信成功
    const val COMM_SUCCESS_MSG = 500

    //通信失败
    const val COMM_FAIL_MSG = 600

    const val MAC_KEY = "address"
    const val BLE_EXCEPTION_KEY = "bleException"
    const val UUID_KEY = "address"

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val data = msg.data
            when(msg.what){
                CONNECT_SUCCESS_MSG -> {
                    val mac = data.getString(MAC_KEY)
                    val bleDevice = msg.obj as BleDevice
                    mac?.let {
                        val connectCallback = BleGlobal.getConnectCallback(mac)
                        connectCallback?.connectSuccess(bleDevice)
                    }

                }
                CONNECT_FAIL_MSG -> {
                    val mac = data.getString(MAC_KEY)
                    val exception = data.getParcelable<BleException>(BLE_EXCEPTION_KEY)
                    if (mac != null && exception != null){
                        val connectCallback = BleGlobal.getConnectCallback(mac)
                        connectCallback?.connectFail(exception)
                    }
                }
                DISCONNECT_MSG -> {
                    val mac = data.getString(MAC_KEY)
                    mac?.let {
                        val connectCallback = BleGlobal.getConnectCallback(mac)
                        connectCallback?.disconnect()
                    }
                }
                COMM_SUCCESS_MSG->{
                    val uuid = data.getString(UUID_KEY)
                    val obj = msg.obj
                    uuid?.let {
                        val callback = BleGlobal.commCallbackMap.get(uuid)
                        if (obj is ByteArray){
                            callback?.onSuccess(obj)
                        }else{
                            callback?.onSuccess(null)
                        }
                        BleGlobal.commCallbackMap.remove(uuid)
                    }
                }
                COMM_FAIL_MSG->{
                    val uuid = data.getString(UUID_KEY)
                    val exception = data.getParcelable<BleException>(BLE_EXCEPTION_KEY)
                    if (uuid != null && exception != null){
                        val callback = BleGlobal.commCallbackMap.get(uuid)
                        callback?.onFail(exception)
                        BleGlobal.commCallbackMap.remove(uuid)
                    }

                }
            }

        }
    }

    fun sendConnectFailMsg(mac:String,bleException: BleException){
        val message = getConnectMsg(mac)
        message.what = CONNECT_FAIL_MSG
        message.data.putParcelable(BLE_EXCEPTION_KEY,bleException)
        handler.sendMessage(message)
    }

    fun sendConnectSuccessMsg(mac:String,bleDevice: BleDevice){
        val message = getConnectMsg(mac)
        message.what = CONNECT_SUCCESS_MSG
        message.obj = bleDevice
        handler.sendMessage(message)
    }

    fun sendDisconnectMsg(mac:String){
        val message = getConnectMsg(mac)
        message.what = DISCONNECT_MSG
        handler.sendMessage(message)
    }


    fun getConnectMsg(mac:String):Message{
        val message = Message.obtain()
        val bundle = Bundle()
        bundle.putString(MAC_KEY,mac)
        message.data = bundle
        return message
    }

    fun sendCommSuccessMsg(uuid: String?, data: ByteArray?){
        val message = Message.obtain()
        val bundle = Bundle()
        bundle.putString(UUID_KEY,uuid)
        message.what = COMM_SUCCESS_MSG
        message.obj = data
        message.data = bundle
        handler.sendMessage(message)
    }

    fun sendCommFailMsg(uuid: String?, bleException: BleException){
        val message = Message.obtain()
        val bundle = Bundle()
        bundle.putString(UUID_KEY,uuid)
        bundle.putParcelable(BLE_EXCEPTION_KEY,bleException)
        message.what = COMM_FAIL_MSG
        message.data = bundle
        handler.sendMessage(message)
    }
}