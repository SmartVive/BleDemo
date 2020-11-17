package com.mountains.bledemo.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import java.util.concurrent.TimeUnit

abstract class BaseControlRunnable : Runnable{
    override fun run() {
        val uuid = getKey()
        val bleCallback = getCallBack()
        try {
            BleControl.lock.lock()
            BleControl.bleCallbackMap.put(uuid,bleCallback)

            val isSuccess = bleHandle()

            if (!isSuccess) {
                bleCallback.onFail()
                return
            }

            //等待设备返回数据
            val isTimeout = !BleControl.condition.await(3, TimeUnit.SECONDS)

            if(isTimeout){
                //超时
                bleCallback.onFail()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            BleControl.bleCallbackMap.remove(uuid)
            BleControl.lock.unlock()
        }
    }

    abstract fun bleHandle():Boolean

    abstract fun getKey():String

    abstract fun getCallBack(): BleControl.BleCallback
}

class ReadCharacteristicRunnable(
    val bluetoothGatt: BluetoothGatt,
    val characteristic: BluetoothGattCharacteristic,
    val bleCallback: BleControl.BleCallback
) : BaseControlRunnable() {

    override fun getKey(): String {
        return characteristic.uuid.toString()
    }

    override fun getCallBack(): BleControl.BleCallback {
        return bleCallback
    }

    override fun bleHandle(): Boolean {
        return bluetoothGatt.readCharacteristic(characteristic)
    }
}

class WriteCharacteristicRunnable(
    val bluetoothGatt: BluetoothGatt,
    val characteristic: BluetoothGattCharacteristic,
    val data: ByteArray,
    val bleCallback: BleControl.BleCallback
) : BaseControlRunnable() {

    override fun getKey(): String {
        return characteristic.uuid.toString()
    }

    override fun getCallBack(): BleControl.BleCallback {
        return bleCallback
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
    val bleCallback: BleControl.BleCallback
): BaseControlRunnable(){
    override fun getKey(): String {
        return descriptor.uuid.toString()
    }

    override fun getCallBack(): BleControl.BleCallback {
        return bleCallback
    }

    override fun bleHandle(): Boolean {
        descriptor.setValue(data)
        return bluetoothGatt.writeDescriptor(descriptor)
    }
}


class ReadDescriptorRunnable(
    val bluetoothGatt: BluetoothGatt,
    val descriptor: BluetoothGattDescriptor,
    val bleCallback: BleControl.BleCallback
): BaseControlRunnable(){
    override fun getKey(): String {
        return descriptor.uuid.toString()
    }

    override fun getCallBack(): BleControl.BleCallback {
        return bleCallback
    }

    override fun bleHandle(): Boolean {
        return bluetoothGatt.readDescriptor(descriptor)
    }
}
