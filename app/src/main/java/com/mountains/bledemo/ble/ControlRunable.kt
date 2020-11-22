package com.mountains.bledemo.ble

import android.bluetooth.*
import android.content.Context
import android.os.Build
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.ble.callback.ConnectCallback
import com.orhanobut.logger.Logger
import java.util.concurrent.TimeUnit

abstract class BaseDataControlRunnable : Runnable{
    override fun run() {
        val uuid = getKey()
        val commCallback = getCallBack()
        try {
            BleGlobal.lock.lock()
            BleGlobal.putCommCallback(uuid,commCallback)

            val isSuccess = bleHandle()

            if (!isSuccess) {
                BleCallBackHandler.sendCommFailMsg(uuid,BleException(BleException.COMM_UNKNOWN_ERROR_CODE, "unKnown error !!"))
                return
            }

            //等待设备返回数据
            val isTimeout = !BleGlobal.condition.await(BleConfiguration.commTimeout, TimeUnit.MILLISECONDS)

            if(isTimeout){
                //超时
                BleCallBackHandler.sendCommFailMsg(uuid,BleException(BleException.COMM_TIMEOUT_CODE, "comm timeout !!"))
            }
        }catch (e:Exception){
            e.printStackTrace()
            BleCallBackHandler.sendCommFailMsg(uuid,BleException(BleException.COMM_UNKNOWN_ERROR_CODE, "${e.message}"))
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
) : BaseDataControlRunnable() {

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
) : BaseDataControlRunnable() {

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
): BaseDataControlRunnable(){
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
): BaseDataControlRunnable(){
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
): BaseDataControlRunnable(){
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


class ConnectRunnable(val context: Context,val device: BluetoothDevice, val bluetoothGattCallback: BluetoothGattCallback,val connectCallback: ConnectCallback):Runnable{
    override fun run() {
        try {
            BleGlobal.lock.lock()
            val bleDevice = BleGlobal.getBleDevice(device.address)
            if(bleDevice != null){
                //此设备已连接
                Logger.e("此设备已连接，请勿重复连接")
                BleCallBackHandler.sendConnectSuccessMsg(device.address,bleDevice)
                return
            }
            BleGlobal.putConnectCallback(device.address,connectCallback)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, bluetoothGattCallback)
            }
            val isTimeout = !BleGlobal.condition.await(BleConfiguration.connectTimeout, TimeUnit.MILLISECONDS)

            if (isTimeout){
                BleCallBackHandler.sendConnectFailMsg(device.address,BleException(BleException.CONNECT_TIMEOUT_CODE, "连接超时"))
            }
        }catch (e:Exception){
            e.printStackTrace()
            BleCallBackHandler.sendConnectFailMsg(device.address,BleException(BleException.CONNECT_FAIL_CODE, "${e.message}"))
        }finally {
            BleGlobal.lock.unlock()
        }

    }

}

