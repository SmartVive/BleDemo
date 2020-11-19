package com.mountains.bledemo.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.mountains.bledemo.ble.callback.CommCallBack
import java.util.concurrent.Callable
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
                val bleException = BleException(BleException.COMM_UNKNOWN_ERROR_CODE, "unKnown error !!")
                commCallback.onFail(bleException)
                return
            }

            //等待设备返回数据
            val isTimeout = !BleGlobal.condition.await(BleConfiguration.commTimeout, TimeUnit.MILLISECONDS)

            if(isTimeout){
                //超时
                val bleException = BleException(BleException.COMM_TIMEOUT_CODE, "comm timeout !!")
                commCallback.onFail(bleException)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            BleGlobal.commCallbackMap.remove(uuid)
            BleGlobal.lock.unlock()
        }
    }

    abstract fun bleHandle():Boolean

    abstract fun getKey():String

    abstract fun getCallBack(): CommCallBack
}

class ReadCharacteristicRunnable(
    val bluetoothGatt: BluetoothGatt,
    val characteristic: BluetoothGattCharacteristic,
    val commCallback: CommCallBack
) : BaseControlRunnable() {

    override fun getKey(): String {
        return characteristic.uuid.toString()
    }

    override fun getCallBack(): CommCallBack {
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
    val commCallback: CommCallBack
) : BaseControlRunnable() {

    override fun getKey(): String {
        return characteristic.uuid.toString()
    }

    override fun getCallBack(): CommCallBack {
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
    val commCallback: CommCallBack
): BaseControlRunnable(){
    override fun getKey(): String {
        return descriptor.uuid.toString()
    }

    override fun getCallBack(): CommCallBack {
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
    val commCallback: CommCallBack
): BaseControlRunnable(){
    override fun getKey(): String {
        return descriptor.uuid.toString()
    }

    override fun getCallBack(): CommCallBack {
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
    val commCallback: CommCallBack
): BaseControlRunnable(){
    override fun getKey(): String {
        return descriptor.uuid.toString()
    }

    override fun getCallBack(): CommCallBack {
        return commCallback
    }

    override fun bleHandle(): Boolean {
        descriptor.setValue(data)
        return bluetoothGatt.writeDescriptor(descriptor) && bluetoothGatt.setCharacteristicNotification(characteristic,true)
    }
}

