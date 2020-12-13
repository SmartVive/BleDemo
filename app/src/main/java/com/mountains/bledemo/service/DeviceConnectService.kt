package com.mountains.bledemo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.*
import com.mountains.bledemo.R
import com.mountains.bledemo.base.Const
import com.mountains.bledemo.ble.BleDevice
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.BleManager
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.ble.callback.ConnectCallback
import com.mountains.bledemo.event.DeviceStateEvent
import com.mountains.bledemo.event.DisconnectAllDeviceEvent
import com.mountains.bledemo.helper.*
import com.mountains.bledemo.util.HexUtil
import com.mountains.bledemo.util.SharedUtil
import com.mountains.bledemo.util.ToastUtil
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class DeviceConnectService : Service() {
    companion object{
        const val CHANNEL_ID_STRING = "channelId"
        const val ENABLE_NOTIFY_MSG = 100
    }

    //已连接的设备
    var connectedDeviceList:MutableList<BleDevice> = mutableListOf()
    val sportDataDecodeHelper = SportDataDecodeHelper()
    val deviceInfoDataDecodeHelper = DeviceInfoDecodeHelper()
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
                    val obj = msg.obj
                    if (obj is Array<*> && obj[0] is BleDevice && obj[1] is ConnectCallback){
                        val bleDevice = obj[0] as BleDevice
                        val connectCallback = obj[1] as ConnectCallback
                        if (currentEnableNotifyRetryCount++ < enableNotifyRetryCount){
                            enableNotify(bleDevice,connectCallback)
                        }else{
                            connectFailCallback(bleDevice.getMac(),bleDevice.getName(), BleException(BleException.CONNECT_FAIL_CODE,"开启通知失败"),connectCallback)
                        }
                    }

                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        EventBus.getDefault().register(this)
        //val bindDeviceMac = SharedUtil.read(Const.BIND_DEVICE_MAC, "")
        val bindDeviceMac = DeviceStorage.getInstance().mac
        if (!bindDeviceMac.isNullOrBlank()){
            connectDevice(bindDeviceMac,null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event:DisconnectAllDeviceEvent){
        disConnectAllDevice()
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
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID_STRING,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
            val notification: Notification = Notification.Builder(applicationContext, CHANNEL_ID_STRING).build()
            startForeground(1, notification)
        }
    }

    /**
     * 连接设备
     */
    fun connectDevice(device: BluetoothDevice,connectCallback: ConnectCallback? = null){
        Logger.d("正在连接设备${device.name}")
        val name = if (device.name != null){
            device.name
        }else{
            device.address
        }
        EventBus.getDefault().post(DeviceStateEvent(device.address,name,DeviceStateEvent.CONNECTING_TYPE))

        for (connectDevice in connectedDeviceList) {
            if (connectDevice.isConnected() && connectDevice.getMac() == device.address) {
                Logger.e("设备已连接，请勿重复连接")
                connectSuccessCallback(connectDevice.getMac(),connectDevice.getName(),connectDevice,connectCallback)
                return
            }
        }

        BleManager.getInstance().connectDevice(device, object : ConnectCallback {
            override fun connectSuccess(bleDevice: BleDevice) {
                //connectCallback?.connectSuccess(bleDevice)
                Logger.d("连接成功:${bleDevice.getMac()}")
                enableNotify(bleDevice,connectCallback)
            }

            override fun connectFail(exception: BleException) {
                connectFailCallback(device.address,device.name,exception,connectCallback)
                DeviceManager.setDevice(null)
                ToastUtil.show("连接失败：${exception.message}")
            }

            override fun disconnect() {
                disconnectCallback(device.address,device.name,connectCallback)
                DeviceManager.setDevice(null)
                ToastUtil.show("断开连接")
            }

        })
    }

    /**
     * 连接设备
     */
    fun connectDevice(deviceMac: String,connectCallback: ConnectCallback? = null){
        val remoteDevice = BleManager.getInstance().getRemoteDevice(deviceMac)
        connectDevice(remoteDevice,connectCallback)
    }

    /**
     * 开启通知
     */
    fun enableNotify(device: BleDevice,connectCallback: ConnectCallback?){
        initNotifyCallBack(device)
        device.enableNotify(BaseUUID.SERVICE, BaseUUID.NOTIFY, BaseUUID.DESC,true,object : CommCallback {
            override fun onSuccess(byteArray: ByteArray?) {
                Logger.d("开启通知成功")
                ToastUtil.show("连接成功:${device.getMac()}")
                connectedDeviceList.add(device)
                connectSuccessCallback(device.getMac(),device.getName(),device,connectCallback)
                syncTime(device)
            }

            override fun onFail(exception: BleException) {
                Logger.d("开启通知失败：${exception.message}")
                //开启失败后延时开启
                if (device.isConnected()){
                    val obj = arrayOf(device, connectCallback)
                    val message = handler.obtainMessage(ENABLE_NOTIFY_MSG, obj)
                    handler.sendMessageDelayed(message,2000)
                }
            }

        })
    }

    /**
     * 通知回调
     */
    private fun initNotifyCallBack(device: BleDevice){
        val mac = device.getMac()
        device.addNotifyCallBack(object : CommCallback{
            override fun onSuccess(byteArray: ByteArray?) {
                //解析数据
                Logger.d(byteArray)
                Logger.d(HexUtil.bytes2HexString(byteArray))
                if(byteArray == null){
                    return
                }
                syncDataHelper.decode(byteArray)
                sportDataDecodeHelper.decode(byteArray,mac)
                deviceInfoDataDecodeHelper.decode(byteArray,mac)
                healthDataDecodeHelper.decode(byteArray,mac)
                sleepDataDecodeHelper.decode(byteArray,mac)

            }

            override fun onFail(exception: BleException) {

            }

        })
    }


    //同步设备时间
    private fun syncTime(device: BleDevice){
        device.writeCharacteristic(BaseUUID.SERVICE,BaseUUID.WRITE,CommHelper.setDeviceTime(),object :CommCallback{
            override fun onSuccess(byteArray: ByteArray?) {
                Logger.i("同步时间成功")
            }

            override fun onFail(exception: BleException) {
                Logger.i("同步时间失败：${exception.message}")
            }

        })
    }

    /**
     * 连接成功
     */
    private fun connectSuccessCallback(deviceMac: String,deviceName: String?, device: BleDevice,connectCallback:ConnectCallback?){
        DeviceManager.setDevice(device)
        EventBus.getDefault().post(DeviceStateEvent(deviceMac,deviceName,DeviceStateEvent.CONNECTED_TYPE))
        connectCallback?.connectSuccess(device)
    }

    /**
     * 连接失败
     */
    private fun connectFailCallback(deviceMac: String,deviceName: String?, bleException: BleException, connectCallback:ConnectCallback?){
        DeviceManager.setDevice(null)
        EventBus.getDefault().post(DeviceStateEvent(deviceMac,deviceName,DeviceStateEvent.CONNECT_FAIL_TYPE))
        connectCallback?.connectFail(bleException)
    }

    /**
     * 断开连接
     */
    private fun disconnectCallback(deviceMac: String,deviceName: String?, connectCallback:ConnectCallback?){
        DeviceManager.setDevice(null)
        EventBus.getDefault().post(DeviceStateEvent(deviceMac,deviceName,DeviceStateEvent.DISCONNECT_TYPE))
        connectCallback?.disconnect()
    }


    /**
     * 断开所有设备
     */
    fun disConnectAllDevice(){
        connectedDeviceList.forEach {
            it.disconnect()
        }
    }
}