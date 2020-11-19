package com.mountains.bledemo.service

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.IBinder
import com.mountains.bledemo.ble.BleDevice
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.BleManager
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.ble.callback.ConnectCallback
import com.mountains.bledemo.helper.BaseUUID
import com.mountains.bledemo.helper.DeviceInfoDataDecodeHelper
import com.mountains.bledemo.helper.SportDataDecodeHelper
import com.mountains.bledemo.util.ToastUtil
import com.orhanobut.logger.Logger

class DeviceConnectService : Service() {
    companion object{
        const val DEVICE = "device"
        //已连接的设备
        var connectedDevice:BleDevice? = null
    }
    val sportDataDecodeHelper = SportDataDecodeHelper()
    val deviceInfoDataDecodeHelper = DeviceInfoDataDecodeHelper()

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val bluetoothDevice = it.getParcelableExtra<BluetoothDevice>(DEVICE)
            bluetoothDevice?.let {
                connectDevice(bluetoothDevice)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 连接设备
     */
    fun connectDevice(device: BluetoothDevice){
        BleManager.getInstance().connectDevice(device, object : ConnectCallback {
            override fun connectSuccess(bleDevice: BleDevice) {
                ToastUtil.show("连接成功:${bleDevice.device.name}")
                connectedDevice = bleDevice
                enableNotify()
            }

            override fun connectFail(exception: BleException) {
                ToastUtil.show("连接失败：${exception.message}")
            }

            override fun disconnect() {
                ToastUtil.show("断开连接")
            }

        })
    }

    /**
     * 开启通知
     */
    fun enableNotify(){
        connectedDevice?.enableNotify(BaseUUID.SERVICE, BaseUUID.NOTIFY, BaseUUID.DESC,true,object : CommCallback {
            override fun onSuccess(byteArray: ByteArray?) {
                Logger.d("开启通知成功")
                initNotifyCallBack()
            }

            override fun onFail(exception: BleException) {
                Logger.d("开启通知失败：${exception.message}")
            }

        })
    }

    /**
     * 通知回调
     */
    private fun initNotifyCallBack(){
        connectedDevice?.addNotifyCallBack(object : CommCallback{
            override fun onSuccess(byteArray: ByteArray?) {
                //解析数据
                sportDataDecodeHelper.decode(byteArray)
                deviceInfoDataDecodeHelper.decode(byteArray)
            }

            override fun onFail(exception: BleException) {

            }

        })
    }
}