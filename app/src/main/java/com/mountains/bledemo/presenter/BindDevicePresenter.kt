package com.mountains.bledemo.presenter

import android.bluetooth.le.ScanResult
import androidx.fragment.app.FragmentActivity
import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.BleManager
import com.mountains.bledemo.view.BindDeviceView
import com.orhanobut.logger.Logger

class BindDevicePresenter : BasePresenter<BindDeviceView>() {


    fun startScan(activity: FragmentActivity) {
        view?.onStartScan()
        BleManager.getInstance().startScan(activity, object : BleManager.ScanResultListener {
            override fun onScanResult(result: ScanResult) {
                Logger.d(result.device?.name)
                filterDevice(result)
            }

            override fun onScanFailed(bleException: BleException) {
                Logger.e("onScanFailed:${bleException.message}")
                view?.showToast("扫描失败：${bleException.message}")
                view?.onScanComplete()
            }

            override fun onScanComplete() {
                Logger.d("onScanComplete")
                view?.onScanComplete()
            }

        })
    }

    fun stopScan(){
        BleManager.getInstance().stopScan()
    }

    fun enableBlueTooth(activity: FragmentActivity) {
        BleManager.getInstance().enableBlueTooth(activity, object : BleManager.BlueToothEnableListener {
            override fun onEnableSuccess() {
                view?.onEnableBleSuccess()
            }

            override fun onEnableFail(bleException: BleException) {
                view?.showToast("开启蓝牙失败：${bleException.message}")
            }
        })
    }


    fun filterDevice(result: ScanResult) {
        if (result.device.name != "X10pro"){
            return
        }
        view?.onScanDeviceResult(result)
    }


}