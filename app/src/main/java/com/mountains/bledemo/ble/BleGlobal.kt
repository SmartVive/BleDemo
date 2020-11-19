package com.mountains.bledemo.ble

import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.ble.callback.ConnectCallback
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

object BleGlobal {
    //ble通信不能并发，所以使用线程池和ReentrantLock来保证串行
    val bleCommThreadPoll: ThreadPoolExecutor = ThreadPoolExecutor(1, 1, 0, TimeUnit.NANOSECONDS, LinkedBlockingQueue())
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    //通信回调，key为uuid
    val commCallbackMap  = hashMapOf<String, CommCallback>()
    //连接状态回调,key为设备mac
    val connectCallbackMap = hashMapOf<String,ConnectCallback>()



    /**
     * 根据设备获取连接回调接口
     */
    fun getConnectCallback(mac:String) : ConnectCallback?{
        return connectCallbackMap[mac]
    }

    /**
     * 根据设备获取连接回调接口
     */
    fun putConnectCallback(mac:String,callback: ConnectCallback){
        connectCallbackMap.put(mac,callback)
    }



}