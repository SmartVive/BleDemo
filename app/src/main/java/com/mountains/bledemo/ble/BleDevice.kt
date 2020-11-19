package com.mountains.bledemo.ble

import android.bluetooth.*
import android.content.Context
import android.os.*
import androidx.core.os.bundleOf
import com.mountains.bledemo.ble.callback.ConnectCallback
import com.mountains.bledemo.ble.callback.CommCallback
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

    //通知回调
    var notifyCallBackList = LinkedList<CommCallback>()
    private set

    companion object {
        //连接成功
        const val CONNECT_SUCCESS_MSG = 400

        //连接超时
        const val CONNECT_TIMEOUT_MSG = 500

        //连接失败
        const val CONNECT_FAIL_MSG = 600

        //断开连接
        const val DISCONNECT_MSG = 700

        //通信成功
        const val COMM_SUCCESS_MSG = 800

        //通信失败
        const val COMM_FAIL_MSG = 900


        val READ_CHARACTERISTIC_TYPE = 0
        val WRITE_CHARACTERISTIC_TYPE = 1
        val READ_DESCRIPTOR_TYPE = 2
        val WRITE_DESCRIPTOR_TYPE = 3
        val ENABLE_NOTIFY_TYPE = 4

        const val MAC_KEY = "address"
        const val BLE_EXCEPTION_KEY = "bleException"
        const val UUID_KEY = "address"

        val handler = object : Handler(Looper.getMainLooper()) {
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
                            removeMessages(CONNECT_TIMEOUT_MSG)
                        }

                    }
                    CONNECT_FAIL_MSG -> {
                        val mac = data.getString(MAC_KEY)
                        val exception = data.getParcelable<BleException>(BLE_EXCEPTION_KEY)
                        if (mac != null && exception != null){
                            val connectCallback = BleGlobal.getConnectCallback(mac)
                            connectCallback?.connectFail(exception)
                            removeMessages(CONNECT_TIMEOUT_MSG)
                        }
                    }
                    DISCONNECT_MSG -> {
                        val mac = data.getString(MAC_KEY)
                        mac?.let {
                            val connectCallback = BleGlobal.getConnectCallback(mac)
                            connectCallback?.disconnect()
                            removeMessages(CONNECT_TIMEOUT_MSG)
                        }
                    }
                    CONNECT_TIMEOUT_MSG -> {
                        val mac = data.getString(MAC_KEY)
                        mac?.let {
                            val connectCallback = BleGlobal.getConnectCallback(mac)
                            val bleException = BleException(BleException.CONNECT_TIMEOUT_CODE, "连接超时")
                            connectCallback?.connectFail(bleException)
                            removeMessages(CONNECT_TIMEOUT_MSG)
                        }

                    }
                    COMM_SUCCESS_MSG->{
                        val uuid = data.getString(UUID_KEY)
                        val data = msg.obj as ByteArray
                        uuid?.let {
                            val callback = BleGlobal.commCallbackMap.get(uuid)
                            callback?.onSuccess(data)
                        }
                    }
                    COMM_FAIL_MSG->{
                        val uuid = data.getString(UUID_KEY)
                        val exception = data.getParcelable<BleException>(BLE_EXCEPTION_KEY)
                        if (uuid != null && exception != null){
                            val callback = BleGlobal.commCallbackMap.get(uuid)
                            callback?.onFail(exception)
                        }

                    }
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
                    sendDisconnectMsg()
                } else {
                    //连接失败
                    if (canReconnect()){
                        Logger.d("连接失败，正在重连:${gatt?.device?.name}")
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
                    Logger.d("发现服务失败,正在重试:${gatt?.device?.name}")
                    reconnection()
                }else{
                    Logger.d("发现服务失败:${gatt?.device?.name}")
                    sendConnectFailMsg(BleException(BleException.CONNECT_FAIL_CODE,"发现服务失败"))
                }
            }
        }


        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Logger.i("onCharacteristicRead,status:$status")
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
            //SportDataDecodeHelper().decode(characteristic?.value)
            for (callBack in notifyCallBackList){
                if(characteristic != null){
                    callBack.onSuccess(characteristic.value)
                }else{
                    callBack.onFail(BleException(BleException.COMM_UNKNOWN_ERROR_CODE,"characteristic is null!!"))
                }

            }
        }
    }




    /**
     * 连接设备
     */
    fun connect(context: Context, callback: ConnectCallback) {
        this.context = context
        BleGlobal.putConnectCallback(device.address,callback)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, bluetoothGattCallback)
        }
        sendConnectTimeoutMsg()
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
            sendConnectFailMsg(BleException(BleException.CONNECT_FAIL_CODE, "context == null !!"))
            return
        }
        BleGlobal.getConnectCallback(device.address)?.let {
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
    fun writeCharacteristic(serviceUUID: String, characteristicUUID:String, data:ByteArray, callback: CommCallback){
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
    fun enableNotify(serviceUUID: String, characteristicUUID: String, descriptorUUID: String, isEnable:Boolean, callback:CommCallback){
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
    fun addNotifyCallBack(callback: CommCallback){
        notifyCallBackList.add(callback)
    }

    /**
     * 删除通知回调
     */
    fun removeNotifyCallBack(callback: CommCallback){
        notifyCallBackList.remove(callback)
    }

    private fun commit(type:Int, serviceUUID: String, characteristicUUID: String, descriptorUUID: String?, data: ByteArray?, callback: CommCallback){

        if (!isConnected()){
            //设备未连接
            val bleException = BleException(BleException.DEVICE_NOT_CONNECTED, "device not connected !!")
            callback.onFail(bleException)
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
            callback.onFail(bleException)
            return
        }else if(service == null){
            //未找到服务
            val bleException = BleException(BleException.NOT_FOUND_SERVICE_CODE, "not found BluetoothGattService !!")
            callback.onFail(bleException)
            return
        }else if (characteristic == null){
            //未找到特征
            val bleException = BleException(BleException.NOT_FOUND_CHARACTERISTIC_CODE,"not found BluetoothGattCharacteristic !!")
            callback.onFail(bleException)
            return
        }else if ((type == READ_DESCRIPTOR_TYPE || type == WRITE_DESCRIPTOR_TYPE || type == ENABLE_NOTIFY_TYPE) && descriptor == null){
            //未找到属性
            val bleException = BleException(BleException.NOT_FOUND_DESCRIPTOR_CODE,"not found BluetoothGattDescriptor !!")
            callback.onFail(bleException)
            return
        }else if ((type == WRITE_CHARACTERISTIC_TYPE || type == WRITE_DESCRIPTOR_TYPE || type == ENABLE_NOTIFY_TYPE) && data == null){
            //写入数据为null
            val bleException = BleException(BleException.DATA_IS_NULL_CODE,"data is null !!")
            callback.onFail(bleException)
            return
        }


        //提交给线程池处理，串行执行
        when(type){
            READ_CHARACTERISTIC_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(ReadCharacteristicRunnable(bluetoothGatt!!, characteristic,callback))
            }
            WRITE_CHARACTERISTIC_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(WriteCharacteristicRunnable(bluetoothGatt!!, characteristic, data!!,callback))
            }
            READ_DESCRIPTOR_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(ReadDescriptorRunnable(bluetoothGatt!!, descriptor!!,callback))
            }
            WRITE_DESCRIPTOR_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(WriteDescriptorRunnable(bluetoothGatt!!, descriptor!!,data!!,callback))
            }
            ENABLE_NOTIFY_TYPE->{
                BleGlobal.bleCommThreadPoll.execute(EnableNotifyRunnable(bluetoothGatt!!, characteristic,descriptor!!,data!!,callback))
            }
        }
    }


    /**
     * 通信结果，回调数据到接口
     */
    private fun commResult(status:Int,uuid:String?,data: ByteArray?){
        try {
            BleGlobal.lock.lock()
            BleGlobal.condition.signal()
            if(status != BluetoothGatt.GATT_SUCCESS){
                CallBackHandler.sendCommFailMsg(uuid,BleException(BleException.COMM_UNKNOWN_ERROR_CODE,"unKnown error:$status !!"))
            }else{
                Logger.d(data)
                CallBackHandler.sendCommSuccessMsg(uuid,data)
            }

        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            BleGlobal.lock.unlock()
        }
    }


    private fun sendConnectFailMsg(bleException: BleException){
        currentRetryCount = 0
        val message = getConnectMsg()
        message.what = CONNECT_FAIL_MSG
        message.data.putParcelable(BLE_EXCEPTION_KEY,bleException)
        handler.sendMessage(message)
    }

    private fun sendConnectSuccessMsg(){
        currentRetryCount = 0
        val message = getConnectMsg()
        message.what = CONNECT_SUCCESS_MSG
        handler.sendMessage(message)
    }

    private fun sendDisconnectMsg(){
        currentRetryCount = 0
        val message = getConnectMsg()
        message.what = DISCONNECT_MSG
        handler.sendMessage(message)
    }

    private fun sendConnectTimeoutMsg(){
        val message = getConnectMsg()
        message.what = CONNECT_TIMEOUT_MSG
        handler.removeMessages(CONNECT_TIMEOUT_MSG)
        handler.sendMessageDelayed(message,BleConfiguration.connectTimeout)
    }

    private fun getConnectMsg():Message{
        val message = Message.obtain()
        val bundle = Bundle()
        bundle.putString(MAC_KEY,device.address)
        message.data = bundle
        message.obj = this
        return message
    }

    private fun sendCommSuccessMsg(uuid: String?, data: ByteArray?){
        val message = Message.obtain()
        val bundle = Bundle()
        bundle.putString(UUID_KEY,uuid)
        message.what = COMM_SUCCESS_MSG
        message.obj = data
        message.data = bundle
        handler.sendMessage(message)
    }

    private fun sendCommFailMsg(uuid: String?, bleException: BleException){
        val message = Message.obtain()
        val bundle = Bundle()
        bundle.putString(UUID_KEY,uuid)
        bundle.putParcelable(BLE_EXCEPTION_KEY,bleException)
        message.what = COMM_FAIL_MSG
        message.data = bundle
        handler.sendMessage(message)
    }
}