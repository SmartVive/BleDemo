package com.mountains.bledemo.view

import android.bluetooth.le.ScanResult
import com.mountains.bledemo.base.BaseView
import com.mountains.bledemo.ble.BleDevice

interface BindDeviceView : BaseView {
    fun onStartScan()

    fun onScanComplete()

    fun onEnableBleSuccess()

    fun onScanDeviceResult(result: ScanResult)
}