package com.mountains.bledemo.ble

import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.mountains.bledemo.ble.callback.ConnectCallback
import com.mountains.bledemo.ble.callback.CommCallBack
import com.mountains.bledemo.helper.SportDataDecodeHelper
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

    //连接状态回调
    private var connectCallback: ConnectCallback? = null

    //设备gatt
    var bluetoothGatt:BluetoothGatt? = null


    companion object {
        //连接成功
        const val CONNECT_SUCCESS_MSG = 400

        //连接超时
        const val CONNECT_TIMEOUT_MSG = 500

        //连接失败
        const val CONNECT_FAIL_MSG = 600

        //断开连接
        const val DISCONNECT_MSG = 700


        val READ_CHARACTERISTIC_TYPE = 0
        val WRITE_CHARACTERISTIC_TYPE = 1
        val READ_DESCRIPTOR_TYPE = 2
        val WRITE_DESCRIPTOR_TYPE = 3
        val ENABLE_NOTIFY_TYPE = 4
    }

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                CONNECT_SUCCESS_MSG -> {
                    connectCallback?.connectSuccess(this@BleDevice)
                    currentRetryCount = 0
                    removeMessages(CONNECT_TIMEOUT_MSG)
                }
                CONNECT_FAIL_MSG -> {
                    val bleException = BleException(BleException.CONNECT_FAIL_CODE, "${msg.obj}")
                    connectCallback?.connectFail(bleException)
                    currentRetryCount = 0
                    removeMessages(CONNECT_TIMEOUT_MSG)
                }
                DISCONNECT_MSG -> {
                    connectCallback?.disconnect()
                    removeMessages(CONNECT_TIMEOUT_MSG)
                }
                CONNECT_TIMEOUT_MSG -> {
                    val bleException = BleException(BleException.CONNECT_TIMEOUT_CODE, "连接超时")
                    connectCallback?.connectFail(bleException)
                    removeMessages(CONNECT_TIMEOUT_MSG)
                }
            }

        }
    }

    val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Logger.i("onConnectionStateChange,status:$status,newState:$newState")

            connectionState = newState
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //已连接
                Logger.d("设备已连接:${gatt?.device?.name}")
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                close()
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //已断开
                    Logger.d("设备已断开:${gatt?.device?.name}")
                    handler.sendEmptyMessage(DISCONNECT_MSG)
                } else {
                    //连接失败
                    if (canReconnect()){
                        Logger.d("连接失败，正在重连:${gatt?.device?.name}")
                        reconnection()
                    }else{
                        Logger.d("连接失败:${gatt?.device?.name}")
                        val message = Message.obtain()
                        message.obj = "连接失败"
                        message.what = CONNECT_FAIL_MSG
                        handler.sendMessage(message)
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
                handler.sendEmptyMessage(CONNECT_SUCCESS_MSG)
            } else {
                //发现服务失败,重试
                if (canReconnect()){
                    Logger.d("发现服务失败,正在重试:${gatt?.device?.name}")
                    reconnection()
                }else{
                    Logger.d("发现服务失败:${gatt?.device?.name}")
                    val message = Message.obtain()
                    message.obj = "连接成功，但是发现服务失败"
                    message.what = CONNECT_FAIL_MSG
                    handler.sendMessage(message)
                }
            }
        }


        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Logger.i("onCharacteristicRead,status:$status")
            /*try {
                BleGlobal.lock.lock()
                BleGlobal.condition.signal()
                val bleCallback = BleGlobal.commCallbackMap.get(characteristic?.uuid.toString())
                if(status != BluetoothGatt.GATT_SUCCESS || characteristic == null){
                    bleCallback?.onFail(BleException(BleException.COMM_UNKNOWN_ERROR_CODE,"unKnown error !!"))
                }else{
                    Logger.d(String(characteristic.value))
                    bleCallback?.onSuccess(characteristic.value)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                BleGlobal.lock.unlock()
            }*/
            commResult(status,characteristic?.uuid.toString(),characteristic?.value)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Logger.i("onCharacteristicWrite,status:$status")

            commResult(status,characteristic?.uuid.toString(),characteristic?.value)
        }

        override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
            Logger.i("onDescriptorRead,status:$status")

            commResult(status,descriptor?.uuid.toString(),descriptor?.value)

        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Logger.i("onDescriptorWrite,status:$status")

            commResult(status,descriptor?.uuid.toString(),descriptor?.value)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            Logger.i("onCharacteristicChanged")
            SportDataDecodeHelper().decode(characteristic?.value)
        }
    }




    /**
     * 连接设备
     */
    fun connect(context: Context, callback: ConnectCallback) {
        this.context = context
        connectCallback = callback

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, bluetoothGattCallback)
        }
        handler.removeMessages(CONNECT_TIMEOUT_MSG)
        handler.sendEmptyMessageDelayed(CONNECT_TIMEOUT_MSG, BleConfiguration.connectTimeout)
    }

    /**
     * 能否重连
     * true : 未达到最大重连次数可以重连
     * false : 超过最大重连次数不能重连
     */
    private fun canReconnect():Boolean{
        return currentRetryCount++ < BleConfiguration.maxRetryCount
    }

    /**
     * 重连
     */
    private fun reconnection(){
        Logger.d("连接失败，正在重连:${device.name}")
        if (context == null) {
            connectCallback?.connectFail(BleException(BleException.CONNECT_FAIL_CODE, "context == null !!"))
            return
        }
        connect(context!!,connectCallback!!)
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
    fun readCharacteristic(serviceUUID: String, characteristicUUID:String,callBack: CommCallBack){
        commit(READ_CHARACTERISTIC_TYPE,serviceUUID,characteristicUUID,null,null,callBack)
    }

    /**
     * 写入特征
     */
    fun writeCharacteristic(serviceUUID: String, characteristicUUID:String,data:ByteArray,callBack: CommCallBack){
        commit(WRITE_CHARACTERISTIC_TYPE,serviceUUID,characteristicUUID,null,data,callBack)
    }

    /**
     * 读取属性
     */
    fun readDescriptor(serviceUUID: String,characteristicUUID:String,descriptorUUID: String,callBack: CommCallBack){
        commit(READ_DESCRIPTOR_TYPE,serviceUUID,characteristicUUID,descriptorUUID,null,callBack)
    }

    /**
     * 写入属性
     */
    fun writeDescriptor(serviceUUID: String,characteristicUUID:String,descriptorUUID: String,data:ByteArray,callBack: CommCallBack){
        commit(WRITE_DESCRIPTOR_TYPE,serviceUUID,characteristicUUID,descriptorUUID,data,callBack)
    }

    fun enableNotify(serviceUUID: String,characteristicUUID: String,descriptorUUID: String,isEnable:Boolean,callBack:CommCallBack){
        val data:ByteArray
        if(isEnable){
            //开启
            data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }else{
            //关闭
            data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }
        commit(ENABLE_NOTIFY_TYPE,serviceUUID,characteristicUUID,descriptorUUID,data,callBack)
    }

    private fun commit(type:Int,serviceUUID: String,characteristicUUID: String,descriptorUUID: String?,data: ByteArray?,callBack: CommCallBack){

        if (!isConnected()){
            //设备未连接
            val bleException = BleException(BleException.DEVICE_NOT_CONNECTED, "device not connected !!")
            callBack.onFail(bleException)
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
            callBack.onFail(bleException)
            return
        }else if(service == null){
            //未找到服务
            val bleException = BleException(BleException.NOT_FOUND_SERVICE_CODE, "not found BluetoothGattService !!")
            callBack.onFail(bleException)
            return
        }else if (characteristic == null){
            //未找到特征
            val bleException = BleException(BleException.NOT_FOUND_CHARACTERISTIC_CODE,"not found BluetoothGattCharacteristic !!")
            callBack.onFail(bleException)
            return
        }else if ((type == READ_DESCRIPTOR_TYPE || type == WRITE_DESCRIPTOR_TYPE || type == ENABLE_NOTIFY_TYPE) && descriptor == null){
            //未找到属性
            val bleException = BleException(BleException.NOT_FOUND_DESCRIPTOR_CODE,"not found BluetoothGattDescriptor !!")
            callBack.onFail(bleException)
            return
        }else if ((type == WRITE_CHARACTERISTIC_TYPE || type == WRITE_DESCRIPTOR_TYPE || type == ENABLE_NOTIFY_TYPE) && data == null){
            //写入数据为null
            val bleException = BleException(BleException.DATA_IS_NULL_CODE,"data is null !!")
            callBack.onFail(bleException)
            return
        }


        //提交给线程池处理，串行执行

        when(type){
            READ_CHARACTERISTIC_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(ReadCharacteristicRunnable(bluetoothGatt!!, characteristic,callBack))
            }
            WRITE_CHARACTERISTIC_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(WriteCharacteristicRunnable(bluetoothGatt!!, characteristic, data!!,callBack))
            }
            READ_DESCRIPTOR_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(ReadDescriptorRunnable(bluetoothGatt!!, descriptor!!,callBack))
            }
            WRITE_DESCRIPTOR_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(WriteDescriptorRunnable(bluetoothGatt!!, descriptor!!,data!!,callBack))
            }
            ENABLE_NOTIFY_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(EnableNotifyRunnable(bluetoothGatt!!, characteristic,descriptor!!,data!!,callBack))
            }
        }
    }


    fun commResult(status:Int,uuid:String?,data: ByteArray?){
        try {
            BleGlobal.lock.lock()
            BleGlobal.condition.signal()
            val bleCallback = BleGlobal.commCallbackMap.get(uuid)
            if(status != BluetoothGatt.GATT_SUCCESS){
                bleCallback?.onFail(BleException(BleException.COMM_UNKNOWN_ERROR_CODE,"unKnown error !!"))
            }else{
                Logger.d(data)
                bleCallback?.onSuccess(data)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            BleGlobal.lock.unlock()
        }
    }

}