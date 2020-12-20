package com.mountains.bledemo.ble

import android.bluetooth.*
import android.content.Context
import com.mountains.bledemo.ble.callback.ConnectCallback
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.ble.callback.NotifyCallback
import com.orhanobut.logger.Logger
import java.util.*

/**
 * 每个从设备对应一个BleDevice
 */
class BleDevice(val device: BluetoothDevice) {
    //上下文
    var context: Context? = null

    //连接状态
    var connectionState = 0

    //当前重试次数
    var currentRetryCount = 0

    //设备gatt
    var bluetoothGatt:BluetoothGatt? = null

    //连接状态回调
    var connectCallback:ConnectCallback? = null


    companion object {
        val READ_CHARACTERISTIC_TYPE = 0
        val WRITE_CHARACTERISTIC_TYPE = 1
        val READ_DESCRIPTOR_TYPE = 2
        val WRITE_DESCRIPTOR_TYPE = 3
        val ENABLE_NOTIFY_TYPE = 4
    }



    val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Logger.i("${this@BleDevice},onConnectionStateChange,status:$status,newState:$newState")

            connectionState = newState
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //已连接
                Logger.d("设备已连接:${gatt?.device?.name}")
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                close()
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //已断开,主动断开
                    Logger.d("设备已断开:${gatt?.device?.name}")
                    sendDisconnectMsg()
                } else {
                    //连接失败
                    if (canReconnect()){
                        Logger.d("连接失败，重试次数：${currentRetryCount},正在重连:${gatt?.device?.name}")
                        reconnection()
                    }else{
                        Logger.d("超过重连次数，连接失败:${gatt?.device?.name}")
                        sendConnectFailMsg(BleException(BleException.CONNECT_FAIL_CODE,"超过重连次数，连接失败"))
                    }

                }

            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //发现服务成功
                Logger.d("发现服务成功:${gatt?.device?.name}")
                bluetoothGatt = gatt
                sendConnectSuccessMsg()
            } else {
                //发现服务失败,重试
                if (canReconnect()){
                    Logger.d("发现服务失败，重试次数：${currentRetryCount},正在重试:${gatt?.device?.name}")
                    reconnection()
                }else{
                    Logger.d("超过重连次数，发现服务失败:${gatt?.device?.name}")
                    sendConnectFailMsg(BleException(BleException.CONNECT_FAIL_CODE,"发现服务失败"))
                }
            }
        }


        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Logger.i("onCharacteristicRead,status:$status")
            commResult(status,characteristic.uuid.toString(),characteristic.value)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Logger.i("onCharacteristicWrite,status:$status")

            commResult(status,characteristic.uuid.toString(),characteristic.value)
        }

        override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
            Logger.i("onDescriptorRead,status:$status")

            commResult(status,descriptor.uuid.toString(),descriptor.value)

        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Logger.i("onDescriptorWrite,status:$status")

            commResult(status,descriptor.uuid.toString(),descriptor.value)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            Logger.i("onCharacteristicChanged")
            //SportDataDecodeHelper().decode(characteristic?.value)

            sendNotifyMsg(characteristic.uuid.toString(),characteristic.value)
        }
    }

    /**
     * 获取mac
     */
    fun getMac():String{
        return device.address
    }

    /**
     * 获取name
     */
    fun getName():String{
        return device.name ?: device.address
    }

    /**
     * 连接设备
     */
    fun connect(context: Context, callback: ConnectCallback) {
        this.context = context
        this.connectCallback = callback
        BleGlobal.bleCommThreadPoll.execute(ConnectRunnable(context,this))
    }

    /**
     * 能否重连
     * true : 未达到最大重连次数可以重连
     * false : 超过最大重连次数不能重连
     */
    private fun canReconnect():Boolean{
        return currentRetryCount++ < BleConfiguration.connectRetryCount
    }

    /**
     * 重连
     */
    private fun reconnection(){
        Logger.d("连接失败，正在重连:${device.name}")
        BleGlobal.lock.lock()
        BleGlobal.removeBleDevice(getMac())
        BleGlobal.condition.signal()
        BleGlobal.lock.unlock()
        if (context == null) {
            sendConnectFailMsg(BleException(BleException.CONNECT_FAIL_CODE, "context == null !!"))
            return
        }
        connectCallback?.let {
            connect(context!!,it)
        }
    }

    /**
     * 主动断开连接
     */
    fun disconnect(){
        bluetoothGatt?.disconnect()
    }

    /**
     * 关闭gatt
     */
    fun close(){
        Logger.i("BluetoothGatt.close()")
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    /**
     * 是否连接
     */
    fun isConnected():Boolean{
        return connectionState == BluetoothGatt.STATE_CONNECTED && bluetoothGatt!=null && bluetoothGatt!!.device != null
    }


    /**
     * 读取特征
     */
    fun readCharacteristic(serviceUUID: String, characteristicUUID:String, callback: CommCallback){
        commit(READ_CHARACTERISTIC_TYPE,serviceUUID,characteristicUUID,null,null,callback)
    }

    /**
     * 写入特征
     */
    fun writeCharacteristic(serviceUUID: String, characteristicUUID:String, data:ByteArray, callback: CommCallback?){
        commit(WRITE_CHARACTERISTIC_TYPE,serviceUUID,characteristicUUID,null,data,callback)
    }

    /**
     * 读取属性
     */
    fun readDescriptor(serviceUUID: String, characteristicUUID:String, descriptorUUID: String, callback: CommCallback){
        commit(READ_DESCRIPTOR_TYPE,serviceUUID,characteristicUUID,descriptorUUID,null,callback)
    }

    /**
     * 写入属性
     */
    fun writeDescriptor(serviceUUID: String, characteristicUUID:String, descriptorUUID: String, data:ByteArray, callback: CommCallback){
        commit(WRITE_DESCRIPTOR_TYPE,serviceUUID,characteristicUUID,descriptorUUID,data,callback)
    }

    /**
     * 开启通知
     */
    fun enableNotify(serviceUUID: String, characteristicUUID: String, descriptorUUID: String, isEnable:Boolean, callback:CommCallback? = null){
        val data:ByteArray
        if(isEnable){
            //开启
            data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }else{
            //关闭
            data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }
        commit(ENABLE_NOTIFY_TYPE,serviceUUID,characteristicUUID,descriptorUUID,data,callback)
    }


    /**
     * 添加通知回调
     */
    fun addNotifyCallBack(callback: NotifyCallback){
        BleGlobal.putNotifyCallback(device.address,callback)
    }

    /**
     * 删除通知回调
     */
    fun removeNotifyCallBack(callback: NotifyCallback){
        BleGlobal.removeNotifyCallback(device.address,callback)
    }

    private fun commit(type:Int, serviceUUID: String, characteristicUUID: String, descriptorUUID: String?, data: ByteArray?, callback: CommCallback?){

        if (!isConnected()){
            //设备未连接
            val bleException = BleException(BleException.DEVICE_NOT_CONNECTED, "device not connected !!")
            callback?.onFail(bleException)
            return
        }

        val service: BluetoothGattService? = bluetoothGatt?.getService(UUID.fromString(serviceUUID))
        val characteristic:BluetoothGattCharacteristic? = service?.getCharacteristic(UUID.fromString(characteristicUUID))
        var descriptor:BluetoothGattDescriptor? = null
        descriptorUUID?.let {
            descriptor= characteristic?.getDescriptor(UUID.fromString(descriptorUUID))
        }


        if (bluetoothGatt == null){
            //gatt为空
            val bleException = BleException(BleException.GATT_SERVICE_IS_NULL_CODE, "BluetoothGattService is null !!")
            callback?.onFail(bleException)
            return
        }else if(service == null){
            //未找到服务
            val bleException = BleException(BleException.NOT_FOUND_SERVICE_CODE, "not found service !!")
            callback?.onFail(bleException)
            return
        }else if (characteristic == null){
            //未找到特征
            val bleException = BleException(BleException.NOT_FOUND_CHARACTERISTIC_CODE,"not found characteristic !!")
            callback?.onFail(bleException)
            return
        }else if ((type == READ_DESCRIPTOR_TYPE || type == WRITE_DESCRIPTOR_TYPE || type == ENABLE_NOTIFY_TYPE) && descriptor == null){
            //未找到属性
            val bleException = BleException(BleException.NOT_FOUND_DESCRIPTOR_CODE,"not found descriptor !!")
            callback?.onFail(bleException)
            return
        }else if ((type == WRITE_CHARACTERISTIC_TYPE || type == WRITE_DESCRIPTOR_TYPE || type == ENABLE_NOTIFY_TYPE) && data == null){
            //写入数据为null
            val bleException = BleException(BleException.DATA_IS_NULL_CODE,"data is null !!")
            callback?.onFail(bleException)
            return
        }


        //提交给线程池处理，串行执行
        when(type){
            READ_CHARACTERISTIC_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(ReadCharacteristicRunnable(this,bluetoothGatt!!, characteristic,callback))
            }
            WRITE_CHARACTERISTIC_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(WriteCharacteristicRunnable(this,bluetoothGatt!!, characteristic, data!!,callback))
            }
            READ_DESCRIPTOR_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(ReadDescriptorRunnable(this,bluetoothGatt!!, descriptor!!,callback))
            }
            WRITE_DESCRIPTOR_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(WriteDescriptorRunnable(this,bluetoothGatt!!, descriptor!!,data!!,callback))
            }
            ENABLE_NOTIFY_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(EnableNotifyRunnable(this,bluetoothGatt!!, characteristic,descriptor!!,data!!,callback))
            }
        }
    }


    /**
     * 通信结果，回调数据到接口
     */
    private fun commResult(status:Int,uuid:String,data: ByteArray?){
        try {
            BleGlobal.lock.lock()
            BleGlobal.condition.signal()
            if(status != BluetoothGatt.GATT_SUCCESS){
                sendCommFailMsg(uuid,BleException(BleException.COMM_UNKNOWN_ERROR_CODE,"unKnown error:$status !!"))
            }else{
                Logger.d(data)
                sendCommSuccessMsg(uuid,data)
            }

        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            BleGlobal.lock.unlock()
        }
    }


    fun sendConnectFailMsg(bleException: BleException){
        BleGlobal.lock.lock()
        BleGlobal.removeBleDevice(getMac())
        BleGlobal.condition.signal()
        BleCallBackHandler.sendConnectFailMsg(this,bleException)
        BleGlobal.lock.unlock()
    }

    fun sendConnectSuccessMsg(){
        sendConnectSuccessMsg(this)
    }

    fun sendConnectSuccessMsg(bleDevice: BleDevice){
        BleGlobal.lock.lock()
        currentRetryCount = 0
        BleGlobal.putBleDevice(getMac(), bleDevice)
        BleGlobal.condition.signal()
        BleCallBackHandler.sendConnectSuccessMsg(bleDevice)
        BleGlobal.lock.unlock()
    }

    fun sendDisconnectMsg(){
        BleGlobal.lock.lock()
        BleGlobal.removeBleDevice(getMac())
        BleGlobal.condition.signal()
        BleCallBackHandler.sendDisconnectMsg(this)
        BleGlobal.lock.unlock()
    }

    fun sendCommSuccessMsg(uuid: String, data: ByteArray?){
        val callback = BleGlobal.getCommCallBack(uuid)
        BleGlobal.removeCommCallback(uuid)
        BleCallBackHandler.sendCommSuccessMsg(callback,data)
    }

    fun sendCommFailMsg(uuid: String, bleException: BleException){
        val callback = BleGlobal.getCommCallBack(uuid)
        BleGlobal.removeCommCallback(uuid)
        BleCallBackHandler.sendCommFailMsg(callback,bleException)
    }

    private fun sendNotifyMsg(uuid: String,data: ByteArray) {
        BleCallBackHandler.sendNotifyMsg(getMac(),uuid,data)
    }
}