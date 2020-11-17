package com.mountains.bledemo.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.orhanobut.logger.Logger
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

object BleControl {
    //数据读写不能并发，所以使用线程池和ReentrantLock来保证串行
    private val threadPoolExecutor: ThreadPoolExecutor = ThreadPoolExecutor(1, 1, 0, TimeUnit.NANOSECONDS, LinkedBlockingQueue())
    val bleCallbackMap  = hashMapOf<String, BleCallback>()
    val lock = ReentrantLock()
    val condition = lock.newCondition()


    val READ_CHARACTERISTIC_TYPE = 0
    val WRITE_CHARACTERISTIC_TYPE = 1
    val READ_DESCRIPTOR_TYPE = 2
    val WRITE_DESCRIPTOR_TYPE = 3

    fun commit(
        commitType: Int,
        bluetoothGatt: BluetoothGatt?,
        serviceUUID: String,
        characteristicUUID: String,
        descriptorUUID: String?,
        data: ByteArray?,
        callBack: BleCallback
    ) {
        val service: BluetoothGattService? = bluetoothGatt?.getService(UUID.fromString(serviceUUID))
        val characteristic = service?.getCharacteristic(UUID.fromString(characteristicUUID))
        var descriptor:BluetoothGattDescriptor? = null
        descriptorUUID?.let {
            descriptor= characteristic?.getDescriptor(UUID.fromString(descriptorUUID))
        }
        if (bluetoothGatt == null){
            //gatt为空
            callBack.onFail()
            return
        }else if(service == null){
            //未找到服务
            callBack.onFail()
            return
        }else if (characteristic == null){
            //未找到特征
            callBack.onFail()
            return
        }else if ((commitType == READ_DESCRIPTOR_TYPE || commitType == WRITE_DESCRIPTOR_TYPE) && descriptor == null){
            //未找到属性
            callBack.onFail()
            return
        }else if ((commitType == WRITE_CHARACTERISTIC_TYPE || commitType == WRITE_DESCRIPTOR_TYPE) && data == null){
            //写入数据为null
            callBack.onFail()
            return
        }

        //提交给线程池处理，串行执行
        when(commitType){
            READ_CHARACTERISTIC_TYPE->{
                threadPoolExecutor.execute(ReadCharacteristicRunnable(bluetoothGatt, characteristic,callBack))
            }
            WRITE_CHARACTERISTIC_TYPE->{
                threadPoolExecutor.execute(WriteCharacteristicRunnable(bluetoothGatt, characteristic, data!!,callBack))
            }
            READ_DESCRIPTOR_TYPE->{
                threadPoolExecutor.execute(ReadDescriptorRunnable(bluetoothGatt, descriptor!!,callBack))
            }
            WRITE_DESCRIPTOR_TYPE->{
                threadPoolExecutor.execute(WriteDescriptorRunnable(bluetoothGatt, descriptor!!,data!!,callBack))
            }
        }
    }

    fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int){
        try {
            lock.lock()
            condition.signal()
            val bleCallback = bleCallbackMap.get(characteristic?.uuid.toString())
            if(status != BluetoothGatt.GATT_SUCCESS){
                bleCallback?.onFail()
            }else{
                Logger.d(String(characteristic!!.value))
                bleCallback?.onSuccess()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            lock.unlock()
        }
    }

    fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        try {
            lock.lock()
            condition.signal()
            val bleCallback = bleCallbackMap.get(characteristic?.uuid.toString())
            if(status != BluetoothGatt.GATT_SUCCESS){
                bleCallback?.onFail()
            }else{
                Logger.d(String(characteristic!!.value))
                bleCallback?.onSuccess()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            lock.unlock()
        }
    }

    fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {

    }

    fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {

    }

    fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {

    }


    interface BleCallback{
        fun onSuccess()

        fun onFail()
    }

}