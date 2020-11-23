package com.mountains.bledemo.ble

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.mountains.bledemo.ble.callback.CommCallback
import java.util.*

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
                    if (obj is BleDevice) {
                        obj.connectCallback?.connectSuccess(obj)
                    }
                }
                CONNECT_FAIL_MSG -> {
                    val exception = data.getParcelable<BleException>(BLE_EXCEPTION_KEY)
                    if (exception != null && obj is BleDevice) {
                        obj.connectCallback?.connectFail(exception)
                    }
                }
                DISCONNECT_MSG -> {
                    if (obj is BleDevice) {
                        obj.connectCallback?.disconnect()
                    }
                }
                COMM_SUCCESS_MSG -> {
                    if (obj is Array<*>) {
                        val callback = obj[0]
                        val byteArray = obj[1]
                        if (callback is CommCallback && byteArray is ByteArray) {
                            callback.onSuccess(byteArray)
                        }
                    }
                }
                COMM_FAIL_MSG -> {
                    val exception = data.getParcelable<BleException>(BLE_EXCEPTION_KEY)
                    if (exception != null) {
                        if (obj is Array<*>) {
                            val callback = obj[0]
                            if (callback is CommCallback) {
                                callback.onFail(exception)
                            }
                        }

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

    fun sendConnectFailMsg(bleDevice: BleDevice, bleException: BleException) {
        val message = handler.obtainMessage(CONNECT_FAIL_MSG, bleDevice)
        val bundle = Bundle()
        bundle.putParcelable(BLE_EXCEPTION_KEY, bleException)
        message.data = bundle
        handler.sendMessage(message)
    }

    fun sendConnectSuccessMsg(bleDevice: BleDevice) {
        val message = handler.obtainMessage(CONNECT_SUCCESS_MSG, bleDevice)
        handler.sendMessage(message)
    }

    fun sendDisconnectMsg(bleDevice: BleDevice) {
        val message = handler.obtainMessage(DISCONNECT_MSG, bleDevice)
        handler.sendMessage(message)
    }


    fun sendCommSuccessMsg(callback: CommCallback?, data: ByteArray?) {
        val message = handler.obtainMessage(COMM_SUCCESS_MSG, arrayOf(callback, data))
        val bundle = Bundle()
        message.data = bundle
        handler.sendMessage(message)
    }

    fun sendCommFailMsg(callback: CommCallback?, bleException: BleException) {
        val message = handler.obtainMessage(COMM_FAIL_MSG, arrayOf(callback))
        val bundle = Bundle()
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