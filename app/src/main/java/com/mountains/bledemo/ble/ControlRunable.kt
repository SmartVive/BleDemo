package com.mountains.bledemo.ble

import android.bluetooth.*
import android.content.Context
import android.os.Build
import com.mountains.bledemo.ble.callback.CommCallback
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
                getBleDevice().sendCommFailMsg(uuid,BleException(BleException.COMM_UNKNOWN_ERROR_CODE, "unKnown error !!"))
                return
            }

            //等待设备返回数据
            val isTimeout = !BleGlobal.condition.await(BleConfiguration.commTimeout, TimeUnit.MILLISECONDS)

            if(isTimeout){
                //超时
                getBleDevice().sendCommFailMsg(uuid,BleException(BleException.COMM_TIMEOUT_CODE, "comm timeout !!"))
            }
        }catch (e:Exception){
            e.printStackTrace()
            getBleDevice().sendCommFailMsg(uuid,BleException(BleException.COMM_UNKNOWN_ERROR_CODE, "${e.message}"))
        }finally {
            //BleGlobal.commCallbackMap.remove(uuid)
            BleGlobal.lock.unlock()
        }
    }

    abstract fun bleHandle():Boolean

    abstract fun getKey():String

    abstract fun getCallBack(): CommCallback

    abstract fun getBleDevice():BleDevice
}

class ReadCharacteristicRunnable(
    private val bleDevice: BleDevice,
    private val bluetoothGatt: BluetoothGatt,
    private val characteristic: BluetoothGattCharacteristic,
    private val commCallback: CommCallback
) : BaseDataControlRunnable() {

    override fun getBleDevice(): BleDevice {
        return bleDevice
    }

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
    private val bleDevice: BleDevice,
    private val bluetoothGatt: BluetoothGatt,
    private val characteristic: BluetoothGattCharacteristic,
    private val data: ByteArray,
    private val commCallback: CommCallback
) : BaseDataControlRunnable() {

    override fun getBleDevice(): BleDevice {
        return bleDevice
    }

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
    private val bleDevice: BleDevice,
    private val bluetoothGatt: BluetoothGatt,
    private val descriptor: BluetoothGattDescriptor,
    private val data: ByteArray,
    private val commCallback: CommCallback
): BaseDataControlRunnable(){

    override fun getBleDevice(): BleDevice {
        return bleDevice
    }

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
    private val bleDevice: BleDevice,
    private val bluetoothGatt: BluetoothGatt,
    private val descriptor: BluetoothGattDescriptor,
    private val commCallback: CommCallback
): BaseDataControlRunnable(){

    override fun getBleDevice(): BleDevice {
        return bleDevice
    }

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
    private val bleDevice: BleDevice,
    private val bluetoothGatt: BluetoothGatt,
    private val characteristic: BluetoothGattCharacteristic,
    private val descriptor: BluetoothGattDescriptor,
    private val data: ByteArray,
    private val commCallback: CommCallback
): BaseDataControlRunnable(){

    override fun getBleDevice(): BleDevice {
        return bleDevice
    }

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


class ConnectRunnable(val context: Context,val bleDevice : BleDevice):Runnable{
    override fun run() {
        try {
            BleGlobal.lock.lock()
            val mac = bleDevice.getMac()
            val existBleDevice = BleGlobal.getBleDevice(mac)
            if(existBleDevice != null && existBleDevice.isConnected()){
                //此设备已连接
                Logger.e("此设备已连接，请勿重复连接")
                bleDevice.sendConnectSuccessMsg(existBleDevice)
                return
            }
            BleGlobal.putBleDevice(mac, bleDevice)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bleDevice.device.connectGatt(context, false, bleDevice.bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                bleDevice.device.connectGatt(context, false, bleDevice.bluetoothGattCallback)
            }
            val isTimeout = !BleGlobal.condition.await(BleConfiguration.connectTimeout, TimeUnit.MILLISECONDS)

            if (isTimeout){
                bleDevice.sendConnectFailMsg(BleException(BleException.CONNECT_TIMEOUT_CODE, "连接超时"))
            }
        }catch (e:Exception){
            e.printStackTrace()
            bleDevice.sendConnectFailMsg(BleException(BleException.CONNECT_FAIL_CODE, "${e.message}"))
        }finally {
            BleGlobal.lock.unlock()
        }

    }

}
