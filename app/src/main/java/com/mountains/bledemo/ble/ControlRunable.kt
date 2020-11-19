package com.mountains.bledemo.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Bundle
import android.os.Message
import com.mountains.bledemo.ble.callback.CommCallback
import java.util.concurrent.TimeUnit

abstract class BaseControlRunnable : Runnable{
    override fun run() {
        val uuid = getKey()
        val commCallback = getCallBack()
        try {
            BleGlobal.lock.lock()
            BleGlobal.commCallbackMap.put(uuid,commCallback)

            val isSuccess = bleHandle()

            if (!isSuccess) {
                //val bleException = BleException(BleException.COMM_UNKNOWN_ERROR_CODE, "unKnown error !!")
                //commCallback.onFail(bleException)
                CallBackHandler.sendCommFailMsg(uuid,BleException(BleException.COMM_UNKNOWN_ERROR_CODE, "unKnown error !!"))
                return
            }

            //等待设备返回数据
            val isTimeout = !BleGlobal.condition.await(BleConfiguration.commTimeout, TimeUnit.MILLISECONDS)

            if(isTimeout){
                //超时
                //val bleException = BleException(BleException.COMM_TIMEOUT_CODE, "comm timeout !!")
                //commCallback.onFail(bleException)
                CallBackHandler.sendCommFailMsg(uuid,BleException(BleException.COMM_TIMEOUT_CODE, "comm timeout !!"))
            }
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            //BleGlobal.commCallbackMap.remove(uuid)
            BleGlobal.lock.unlock()
        }
    }

    abstract fun bleHandle():Boolean

    abstract fun getKey():String

    abstract fun getCallBack(): CommCallback
}

class ReadCharacteristicRunnable(
    val bluetoothGatt: BluetoothGatt,
    val characteristic: BluetoothGattCharacteristic,
    val commCallback: CommCallback
) : BaseControlRunnable() {

    override fun getKey(): String {
        return characteristic.uuid.toString()
    }

    override fun getCallBack(): CommCallback {
        return commCallback
    }

    override fun bleHandle(): Boolean {
        return bluetoothGatt.readCharacteristic(characteristic)
    }
}

class WriteCharacteristicRunnable(
    val bluetoothGatt: BluetoothGatt,
    val characteristic: BluetoothGattCharacteristic,
    val data: ByteArray,
    val commCallback: CommCallback
) : BaseControlRunnable() {

    override fun getKey(): String {
        return characteristic.uuid.toString()
    }

    override fun getCallBack(): CommCallback {
        return commCallback
    }

    override fun bleHandle(): Boolean {
        characteristic.setValue(data)
        return bluetoothGatt.writeCharacteristic(characteristic)
    }
}

class WriteDescriptorRunnable(
    val bluetoothGatt: BluetoothGatt,
    val descriptor: BluetoothGattDescriptor,
    val data: ByteArray,
    val commCallback: CommCallback
): BaseControlRunnable(){
    override fun getKey(): String {
        return descriptor.uuid.toString()
    }

    override fun getCallBack(): CommCallback {
        return commCallback
    }

    override fun bleHandle(): Boolean {
        descriptor.setValue(data)
        return bluetoothGatt.writeDescriptor(descriptor)
    }
}


class ReadDescriptorRunnable(
    val bluetoothGatt: BluetoothGatt,
    val descriptor: BluetoothGattDescriptor,
    val commCallback: CommCallback
): BaseControlRunnable(){
    override fun getKey(): String {
        return descriptor.uuid.toString()
    }

    override fun getCallBack(): CommCallback {
        return commCallback
    }

    override fun bleHandle(): Boolean {
        return bluetoothGatt.readDescriptor(descriptor)
    }
}

class EnableNotifyRunnable(
    val bluetoothGatt: BluetoothGatt,
    val characteristic: BluetoothGattCharacteristic,
    val descriptor: BluetoothGattDescriptor,
    val data: ByteArray,
    val commCallback: CommCallback
): BaseControlRunnable(){
    override fun getKey(): String {
        return descriptor.uuid.toString()
    }

    override fun getCallBack(): CommCallback {
        return commCallback
    }

    override fun bleHandle(): Boolean {
        descriptor.setValue(data)
        return bluetoothGatt.writeDescriptor(descriptor) && bluetoothGatt.setCharacteristicNotification(characteristic,true)
    }
}

