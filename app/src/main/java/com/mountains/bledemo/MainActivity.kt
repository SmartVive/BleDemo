package com.mountains.bledemo

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.ble.BleManager
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.locks.ReentrantLock


class MainActivity : BaseActivity<MainPresenter>(), MainView {
    val bleManager by lazy { BleManager.getInstance() }
    override fun createPresenter(): MainPresenter {
        return MainPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(Color.TRANSPARENT)

        initView()
    }

    private  fun initView(){
        btnScan.setOnClickListener {
            bleManager.enableBlueTooth(this,object :BleManager.BlueToothEnableListener{
                override fun onEnableSuccess() {
                    scanDevice()
                }

                override fun onEnableFail() {
                    showToast("打开蓝牙失败")
                }

                override fun notSupportBle() {
                    showToast("不支持当前设备")
                }
            })

        }

        btnRead.setOnClickListener {
            bleManager.readCharacteristic("00001800-0000-1000-8000-00805f9b34fb","00002a00-0000-1000-8000-00805f9b34fb",object :BleManager.BleCallback{
                override fun onSuccess() {
                    Logger.d("readCharacteristicSuccess")
                }

                override fun onFail() {
                    Logger.d("readCharacteristicFail")
                }

            })
            bleManager.readCharacteristic("00001800-0000-1000-8000-00805f9b34fb","00002a00-0000-1000-8000-00805f9b34fb",object :BleManager.BleCallback{
                override fun onSuccess() {
                    Logger.d("readCharacteristicSuccess")
                }

                override fun onFail() {
                    Logger.d("readCharacteristicFail")
                }

            })

        }
    }

    private fun scanDevice(){
        bleManager.startScan(this,object :BleManager.ScanResultListener{
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                Logger.d(result.device?.name)
                if (result.device?.name == "GIONEE F106"){
                    connect(result.device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Logger.e("onScanFailed:$errorCode")
            }

            override fun onScanComplete() {
                Logger.d("onScanComplete")
            }

        })
    }


    private fun connect(device:BluetoothDevice){
        bleManager.connectDevice(device,object :BleManager.ConnectDeviceListener{
            override fun connectSuccess() {
                showToast("连接成功")
            }

            override fun connectFail() {
                showToast("连接失败")
            }

            override fun disconnect() {
                showToast("断开连接")
            }
        })
    }

}
