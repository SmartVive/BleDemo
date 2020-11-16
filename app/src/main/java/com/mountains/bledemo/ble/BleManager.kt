package com.mountains.bledemo.ble

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.FragmentActivity
import com.mountains.bledemo.util.ToastUtil
import com.orhanobut.logger.Logger
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock


class BleManager private constructor() {
    private var context: Context? = null
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var adapter: BluetoothAdapter
    //数据读写不能并发，所以使用线程池和ReentrantLock来保证串行
    private lateinit var threadPoolExecutor:ThreadPoolExecutor
    private var scanResultListener: ScanResultListener? = null
    private var connectDeviceListener: ConnectDeviceListener? = null
    private var connectDevice: BluetoothDevice? = null
    //蓝牙GATT
    private var bluetoothGatt : BluetoothGatt? = null

    //搜索时间
    private var scanDelayed = 15*1000L
    //连接超时时间
    private var connectTimeOut = 15*1000L
    //重试次数
    private var maxRetryCount = 3
    //现在重试次数
    private var currentRetryCount = 0


    companion object {
        private var instance: BleManager? = null
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        private var bleCallbackMap  = hashMapOf<String,BleCallback>()

        const val PERMISSION_FRAGMENT_TAG = "PERMISSION_FRAGMENT_TAG"
        const val STOP_SCAN_MSG = 100
        const val CONNECT_SUCCESS_MSG = 200
        const val CONNECT_FAIL_MSG = 300
        const val DISCONNECT_MSG = 400
        const val CONNECT_TIME_OUT = 500

        fun getInstance(): BleManager {
            if (instance == null) {
                instance = BleManager()
            }
            return instance!!
        }
    }


    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                STOP_SCAN_MSG -> {
                    scanResultListener?.onScanComplete()
                    stopScan()
                }
                CONNECT_SUCCESS_MSG -> {
                    connectDeviceListener?.connectSuccess()
                    currentRetryCount = 0
                    removeMessages(CONNECT_TIME_OUT)
                }
                CONNECT_FAIL_MSG -> {
                    connectDeviceListener?.connectFail()
                    currentRetryCount = 0
                    removeMessages(CONNECT_TIME_OUT)
                }
                DISCONNECT_MSG -> connectDeviceListener?.disconnect()
                CONNECT_TIME_OUT -> connectDeviceListener?.connectFail()
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                scanResultListener?.onScanResult(callbackType, result)
            }

        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            scanResultListener?.onScanFailed(errorCode)
            scanResultListener = null
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Logger.i("onConnectionStateChange,status:$status,newState:$newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //已连接
                Logger.d("已连接")
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                close()
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //已断开
                    Logger.d("已断开")
                    handler.sendEmptyMessage(DISCONNECT_MSG)
                } else {
                    //连接失败
                    if (currentRetryCount++ < maxRetryCount){
                        Logger.d("连接失败，正在重连")
                        connectDevice(connectDevice, connectDeviceListener)
                    }else{
                        Logger.d("连接失败")
                        handler.sendEmptyMessage(CONNECT_FAIL_MSG)
                    }

                }

            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //发现服务成功
                Logger.d("发现服务成功")
                bluetoothGatt = gatt
                handler.sendEmptyMessage(CONNECT_SUCCESS_MSG)
            } else {
                //发现服务失败,重试
                if (currentRetryCount++ < maxRetryCount){
                    Logger.d("发现服务失败,正在重试")
                    connectDevice(connectDevice, connectDeviceListener)
                }else{
                    handler.sendEmptyMessage(CONNECT_FAIL_MSG)
                }
            }
        }


        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Logger.i("onCharacteristicRead,status:$status")

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

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Logger.i("onCharacteristicWrite,status:$status")

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

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)

        }

    }


    fun init(context: Context) {
        this.context = context
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bluetoothManager.adapter

        //数据操作线程池
        threadPoolExecutor = ThreadPoolExecutor(1, 1, 0, TimeUnit.NANOSECONDS, LinkedBlockingQueue())
    }

    private fun isSupportBle():Boolean{
        if(context!= null && context!!.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) && adapter!= null){
            return true
        }
        return false
    }

    fun isBlueToothEnable(): Boolean {
        return adapter.isEnabled
    }

    /**
     * 开启蓝牙
     */
    fun enableBlueTooth(activity: FragmentActivity,listener:BlueToothEnableListener) {
        if (!isSupportBle()){
            listener.onEnableFail()
            return
        }

        if (!isBlueToothEnable()) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            SuperFragment.getSuperFragment(activity).startActivityForResult(intent, object : SuperFragment.ActivityResultListener {
                override fun onActivityResult(resultCode: Int, data: Intent?) {
                    if (resultCode == Activity.RESULT_OK) {
                        listener.onEnableSuccess()
                    }else{
                        listener.onEnableFail()
                    }
                }

            })

        }else{
            listener.onEnableSuccess()
        }
    }

    /**
     * 扫描设备
     */
    fun startScan(activity: FragmentActivity, listener: ScanResultListener) {
        if (scanResultListener != null){
            Logger.w("已经正在搜索，请勿重复调用")
            return
        }

        scanResultListener = listener
        SuperFragment.getSuperFragment(activity).requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            object : SuperFragment.PermissionListener {
                override fun onRequestSuccess() {
                    val scanSettings = ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .build()
                    adapter.bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)

                    //限定扫描时间，到达就停止扫描
                    handler.removeMessages(STOP_SCAN_MSG)
                    handler.sendEmptyMessageDelayed(STOP_SCAN_MSG, scanDelayed)
                }

                override fun onRequestFail() {
                    ToastUtil.show("未开启权限无法搜索蓝牙")
                }

            })
    }

    /**
     * 停止扫描
     */
    fun stopScan() {
        adapter.bluetoothLeScanner?.stopScan(scanCallback)
        scanResultListener = null
    }

    /**
     * 连接设备
     */
    fun connectDevice(device: BluetoothDevice?, listener: ConnectDeviceListener?) {
        connectDevice = device
        connectDeviceListener = listener

        if(connectDevice == null){
            Logger.e("device can't be null!!")
            handler.sendEmptyMessage(CONNECT_FAIL_MSG)
            return
        }

        //连接前关闭搜索
        stopScan()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectDevice?.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            connectDevice?.connectGatt(context, false, bluetoothGattCallback)
        }
        handler.removeMessages(CONNECT_TIME_OUT)
        handler.sendEmptyMessageDelayed(CONNECT_TIME_OUT,connectTimeOut)
    }

    /**
     * 主动断开连接
     */
    fun disconnect(){
        bluetoothGatt?.disconnect()
    }

    /**
     * 关闭GATT
     */
    @Synchronized
    fun close() {
        bluetoothGatt?.close()
        //bluetoothGatt = null
    }

    /**
     * 读取数据
     */
    fun readCharacteristic(serviceUUID: String, characteristicUUID:String,callBack:BleCallback){
        val service:BluetoothGattService? = bluetoothGatt?.getService(UUID.fromString(serviceUUID))
        val characteristic = service?.getCharacteristic(UUID.fromString(characteristicUUID))
        if (bluetoothGatt == null){
            //gatt为空
            callBack.onFail()
        }else if(service == null){
            //未找到服务
            callBack.onFail()
        }else if (characteristic == null){
            //未找到特征
            callBack.onFail()
        }else{
            threadPoolExecutor.execute(ReadCharacteristicRunnable(bluetoothGatt!!, characteristic,callBack))
        }
    }

    /**
     * 写入数据
     */
    fun writeCharacteristic(serviceUUID: String, characteristicUUID:String,data:ByteArray,callBack:BleCallback){
        val service:BluetoothGattService? = bluetoothGatt?.getService(UUID.fromString(serviceUUID))
        val characteristic = service?.getCharacteristic(UUID.fromString(characteristicUUID))
        if (bluetoothGatt == null){
            //gatt为空
            callBack.onFail()
        }else if(service == null){
            //未找到服务
            callBack.onFail()
        }else if (characteristic == null){
            //未找到特征
            callBack.onFail()
        }else{
            threadPoolExecutor.execute(WriteCharacteristicRunnable(bluetoothGatt!!, characteristic, data,callBack))
        }
    }

    abstract class BaseCharacteristicRunnable(val uuid:String, val bleCallback: BleCallback) : Runnable{
        override fun run() {
            try {
                lock.lock()
                bleCallbackMap.put(uuid,bleCallback)

                val isSuccess = bleHandle()

                if (!isSuccess) {
                    bleCallback.onFail()
                    return
                }

                //等待设备返回数据
                val isTimeout = !condition.await(3, TimeUnit.SECONDS)

                if(isTimeout){//超时
                    bleCallback.onFail()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                bleCallbackMap.remove(uuid)
                lock.unlock()
            }
        }

        abstract fun bleHandle():Boolean

    }

    class WriteCharacteristicRunnable(
        val bluetoothGatt: BluetoothGatt,
        val characteristic: BluetoothGattCharacteristic,
        val data: ByteArray, bleCallback: BleCallback
    ) : BaseCharacteristicRunnable(characteristic.uuid.toString(), bleCallback) {
        override fun bleHandle(): Boolean {
            characteristic.setValue(data)
            return bluetoothGatt.writeCharacteristic(characteristic)
        }
    }

    class ReadCharacteristicRunnable(
        val bluetoothGatt: BluetoothGatt,
        val characteristic: BluetoothGattCharacteristic,
        bleCallback: BleCallback
    ) : BaseCharacteristicRunnable(characteristic.uuid.toString(), bleCallback) {
        override fun bleHandle(): Boolean {
            return bluetoothGatt.readCharacteristic(characteristic)
        }
    }


    interface BlueToothEnableListener{
        fun onEnableSuccess()
        fun onEnableFail()
        fun notSupportBle()
    }

    interface ScanResultListener {
        fun onScanResult(callbackType: Int, result: ScanResult)

        fun onScanFailed(errorCode: Int)

        fun onScanComplete()
    }

    interface ConnectDeviceListener {
        fun connectSuccess()

        fun connectFail()

        fun disconnect()
    }

    interface BleCallback{
        fun onSuccess()

        fun onFail()
    }


}