package com.mountains.bledemo.ble

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message

object BleCallBackHandler {
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

    //notify
    const val COMM_NOTIFY_MSG = 700

    const val MAC_KEY = "address"
    const val BLE_EXCEPTION_KEY = "bleException"
    const val UUID_KEY = "address"

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val data = msg.data
            val mac = data.getString(MAC_KEY)
            val uuid = data.getString(UUID_KEY)
            val obj = msg.obj
            when (msg.what) {
                CONNECT_SUCCESS_MSG -> {
                    mac?.let {
                        val connectCallback = BleGlobal.getConnectCallback(mac)
                        if (obj is BleDevice) {
                            connectCallback?.connectSuccess(obj)
                        }
                    }
                }
                CONNECT_FAIL_MSG -> {
                    val exception = data.getParcelable<BleException>(BLE_EXCEPTION_KEY)
                    if (mac != null && exception != null) {
                        val connectCallback = BleGlobal.getConnectCallback(mac)
                        BleGlobal.removeConnectCallback(mac)
                        connectCallback?.connectFail(exception)
                    }
                }
                DISCONNECT_MSG -> {
                    mac?.let {
                        val connectCallback = BleGlobal.getConnectCallback(mac)
                        BleGlobal.removeConnectCallback(mac)
                        connectCallback?.disconnect()
                    }
                }
                COMM_SUCCESS_MSG -> {
                    uuid?.let {
                        val callback = BleGlobal.getCommCallBack(uuid)
                        BleGlobal.removeCommCallback(uuid)
                        if (obj is ByteArray) {
                            callback?.onSuccess(obj)
                        }
                    }
                }
                COMM_FAIL_MSG -> {
                    val exception = data.getParcelable<BleException>(BLE_EXCEPTION_KEY)
                    if (uuid != null && exception != null) {
                        val callback = BleGlobal.getCommCallBack(uuid)
                        BleGlobal.removeCommCallback(uuid)
                        callback?.onFail(exception)
                    }

                }
                COMM_NOTIFY_MSG -> {
                    mac?.let {
                        val notifyCallbackList = BleGlobal.getNotifyCallback(mac) ?: return
                        if (obj is ByteArray) {
                            for (callBack in notifyCallbackList) {
                                callBack.onSuccess(obj)
                            }
                        }
                    }
                }

            }

        }
    }

    fun sendConnectFailMsg(mac: String, bleException: BleException) {
        val message = getConnectMsg(mac)
        message.what = CONNECT_FAIL_MSG
        message.data.putParcelable(BLE_EXCEPTION_KEY, bleException)
        handler.sendMessage(message)
    }

    fun sendConnectSuccessMsg(mac: String, bleDevice: BleDevice) {
        val message = getConnectMsg(mac)
        message.what = CONNECT_SUCCESS_MSG
        message.obj = bleDevice
        handler.sendMessage(message)
    }

    fun sendDisconnectMsg(mac: String) {
        val message = getConnectMsg(mac)
        message.what = DISCONNECT_MSG
        handler.sendMessage(message)
    }


    fun getConnectMsg(mac: String): Message {
        val message = handler.obtainMessage()
        val bundle = Bundle()
        bundle.putString(MAC_KEY, mac)
        message.data = bundle
        return message
    }

    fun sendCommSuccessMsg(uuid: String?, data: ByteArray?) {
        val message = handler.obtainMessage(COMM_SUCCESS_MSG, data)
        val bundle = Bundle()
        bundle.putString(UUID_KEY, uuid)
        message.data = bundle
        handler.sendMessage(message)
    }

    fun sendCommFailMsg(uuid: String?, bleException: BleException) {
        val message = handler.obtainMessage(COMM_FAIL_MSG)
        val bundle = Bundle()
        bundle.putString(UUID_KEY, uuid)
        bundle.putParcelable(BLE_EXCEPTION_KEY, bleException)
        message.data = bundle
        handler.sendMessage(message)
    }

    fun sendNotifyMsg(mac: String, data: ByteArray?) {
        val message = handler.obtainMessage(COMM_NOTIFY_MSG, data)
        val bundle = Bundle()
        bundle.putString(MAC_KEY, mac)
        message.data = bundle
        handler.sendMessage(message)
    }
}