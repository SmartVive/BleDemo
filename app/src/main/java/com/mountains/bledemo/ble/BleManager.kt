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
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.FragmentActivity
import com.mountains.bledemo.ble.callback.ConnectCallback
import com.mountains.bledemo.util.ToastUtil
import com.orhanobut.logger.Logger
import java.lang.Exception


class BleManager private constructor() {
    private lateinit var context: Context
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var adapter: BluetoothAdapter
    private var scanResultListener: ScanResultListener? = null

    companion object {
        private var instance: BleManager? = null

        const val PERMISSION_FRAGMENT_TAG = "PERMISSION_FRAGMENT_TAG"
        const val STOP_SCAN_MSG = 100

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
            scanResultListener?.onScanFailed(BleException(BleException.SCAN_UNKNOWN_ERROR_CODE,"ScanCallback:onScanFailed($errorCode)"))
            scanResultListener = null
        }
    }


    fun init(context: Context) {
        this.context = context
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bluetoothManager.adapter
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
            listener.onEnableFail(BleException(BleException.BLE_NOT_SUPPORT,"此设备不支持ble蓝牙"))
            return
        }

        if (!isBlueToothEnable()) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            SuperFragment.getSuperFragment(activity).startActivityForResult(intent, object : SuperFragment.ActivityResultListener {
                override fun onActivityResult(resultCode: Int, data: Intent?) {
                    if (resultCode == Activity.RESULT_OK) {
                        listener.onEnableSuccess()
                    }else{
                        listener.onEnableFail(BleException(BleException.ENABLE_PERMISSION_DENIED_CODE,"获取权限失败"))
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
                    handler.sendEmptyMessageDelayed(STOP_SCAN_MSG, BleConfiguration.scanDelayed)
                }

                override fun onRequestFail() {
                    //ToastUtil.show("未开启权限无法搜索蓝牙")
                    listener.onScanFailed(BleException(BleException.SCAN_PERMISSION_DENIED_CODE,"获取权限失败"))
                }

            })
    }

    /**
     * 停止扫描
     */
    fun stopScan() {
        adapter.bluetoothLeScanner?.stopScan(scanCallback)
        scanResultListener?.onScanComplete()
        scanResultListener = null
    }



    /**
     * 连接设备
     */
    fun connectDevice(device: BluetoothDevice, connectCallback: ConnectCallback){
        //连接前关闭搜索
        stopScan()
        val bleDevice = BleDevice(device)
        bleDevice.connect(context,connectCallback)
    }



    interface BlueToothEnableListener{
        fun onEnableSuccess()
        fun onEnableFail(bleException: BleException)
    }

    interface ScanResultListener {
        fun onScanResult(callbackType: Int, result: ScanResult)

        fun onScanFailed(bleException: BleException)

        fun onScanComplete()
    }


}