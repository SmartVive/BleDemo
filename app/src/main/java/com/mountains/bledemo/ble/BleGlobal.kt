package com.mountains.bledemo.ble

import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.ble.callback.ConnectCallback
import com.mountains.bledemo.ble.callback.NotifyCallback
import java.util.*
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
    private val commCallbackMap = hashMapOf<String, CommCallback>()

    //连接状态回调,key为设备mac
    private val connectCallbackMap = hashMapOf<String, ConnectCallback>()

    //通知回调，key为设备mac
    private val notifyCallbackMap = hashMapOf<String, LinkedList<NotifyCallback>>()

    //已连接的设备
    private val bleDeviceMap = hashMapOf<String,BleDevice>()

    /**
     * 根据mac获取bleDevice
     */
    fun getBleDevice(mac: String):BleDevice?{
        return bleDeviceMap.get(mac)
    }

    /**
     * 添加bleDevice
     */
    fun putBleDevice(mac: String,bleDevice: BleDevice){
        bleDeviceMap.put(mac,bleDevice)
    }

    /**
     * 删除bleDevice
     */
    fun removeBleDevice(mac: String){
        bleDeviceMap.remove(mac)
    }

    /**
     * 根据uuid获取通信Callback
     */
    fun getCommCallBack(uuid:String):CommCallback?{
        return commCallbackMap.get(uuid)
    }


    /**
     * 添加通信Callback
     */
    fun putCommCallback(uuid: String,callback: CommCallback){
        commCallbackMap.put(uuid,callback)
    }

    /**
     * 删除通信Callback
     */
    fun removeCommCallback(uuid: String){
        commCallbackMap.remove(uuid)
    }

    /**
     * 根据设备mac获取通知回调
     */
    fun getNotifyCallback(mac: String): LinkedList<NotifyCallback>? {
        return notifyCallbackMap.get(mac)
    }

    /**
     * 添加通知回调
     */
    fun putNotifyCallback(mac: String, callback: NotifyCallback) {
        var linkedList:LinkedList<NotifyCallback>? = getNotifyCallback(mac)

        if (linkedList == null) {
            linkedList = LinkedList<NotifyCallback>()
        }
        linkedList.add(callback)
        notifyCallbackMap.put(mac,linkedList)
    }


    /**
     * 删除通知回调
     */
    fun removeNotifyCallback(mac: String, callback: NotifyCallback){
        val notifyCallbackList = getNotifyCallback(mac)
        notifyCallbackList?.remove(callback)
    }
}