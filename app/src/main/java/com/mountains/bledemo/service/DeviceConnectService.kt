package com.mountains.bledemo.service

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.*
import com.mountains.bledemo.ble.BleDevice
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.BleManager
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.ble.callback.ConnectCallback
import com.mountains.bledemo.helper.*
import com.mountains.bledemo.util.HexUtil
import com.mountains.bledemo.util.ToastUtil
import com.orhanobut.logger.Logger

class DeviceConnectService : Service() {
    companion object{
        const val DEVICE = "device"
        //已连接的设备
        var connectedDevice:BleDevice? = null

        var ENABLE_NOTIFY_MSG = 100
    }
    val sportDataDecodeHelper = SportDataDecodeHelper()
    val deviceInfoDataDecodeHelper = DeviceInfoDataDecodeHelper()
    val healthDataDecodeHelper = HealthDataDecodeHelper()
    val sleepDataDecodeHelper = SleepDataDecodeHelper()
    val syncDataHelper = SyncDataHelper()
    //开启通知重试次数
    var enableNotifyRetryCount:Int = 5
    var currentEnableNotifyRetryCount:Int = 0



    private val handler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                ENABLE_NOTIFY_MSG->{
                    if (currentEnableNotifyRetryCount++ < enableNotifyRetryCount){
                        enableNotify()
                    }
                }
            }
        }
    }

    inner class MyBinder : Binder() {
        fun getService():DeviceConnectService{
            return this@DeviceConnectService
        }

    }

    override fun onBind(p0: Intent?): IBinder? {
        return MyBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val bluetoothDevice = it.getParcelableExtra<BluetoothDevice>(DEVICE)
            bluetoothDevice?.let {
                if (connectedDevice!=null && connectedDevice!!.isConnected() && connectedDevice!!.device.address == bluetoothDevice.address){
                    Logger.e("设备已连接，请勿重复连接")
                }else{
                    connectDevice(bluetoothDevice)
                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 连接设备
     */
    fun connectDevice(device: BluetoothDevice,connectCallback: ConnectCallback? = null){
        Logger.d("正在连接设备${device.name}")
        BleManager.getInstance().connectDevice(device, object : ConnectCallback {
            override fun connectSuccess(bleDevice: BleDevice) {
                connectCallback?.connectSuccess(bleDevice)
                ToastUtil.show("连接成功:${bleDevice.device.name}")
                connectedDevice = bleDevice
                enableNotify()
            }

            override fun connectFail(exception: BleException) {
                connectCallback?.connectFail(exception)
                ToastUtil.show("连接失败：${exception.message}")
            }

            override fun disconnect() {
                connectCallback?.disconnect()
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
                syncTime()
            }

            override fun onFail(exception: BleException) {
                Logger.d("开启通知失败：${exception.message}")
                //开启失败后延时开启
                if (connectedDevice!=null && connectedDevice!!.isConnected()){
                    handler.sendEmptyMessageDelayed(ENABLE_NOTIFY_MSG,2000)
                }
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
                Logger.d(byteArray)
                Logger.d(HexUtil.bytes2HexString(byteArray))
                if(byteArray == null){
                    return
                }
                syncDataHelper.decode(byteArray)
                sportDataDecodeHelper.decode(byteArray)
                deviceInfoDataDecodeHelper.decode(byteArray)
                healthDataDecodeHelper.decode(byteArray)
                sleepDataDecodeHelper.decode(byteArray)

            }

            override fun onFail(exception: BleException) {

            }

        })
    }


    //同步设备时间
    private fun syncTime(){
        connectedDevice?.writeCharacteristic(BaseUUID.SERVICE,BaseUUID.WRITE,CommHelper.setDeviceTime(),object :CommCallback{
            override fun onSuccess(byteArray: ByteArray?) {
                Logger.i("同步时间成功")
            }

            override fun onFail(exception: BleException) {
                Logger.i("同步时间失败：${exception.message}")
            }

        })
    }
}