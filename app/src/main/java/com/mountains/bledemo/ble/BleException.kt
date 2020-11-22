package com.mountains.bledemo.ble

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class BleException(val code:Int,val message:String):Parcelable {
    companion object{
        /**
         * 启动蓝牙
         */
        //不支持ble
        const val BLE_NOT_SUPPORT_CODE = 30
        //打开蓝牙失败
        const val BLE_ENABLE_FAIL_CODE = 31


        /**
         * 搜索
         */
        //没有权限
        const val SCAN_PERMISSION_DENIED_CODE = 20
        //未知错误
        const val SCAN_UNKNOWN_ERROR_CODE = 21


        /**
         * 连接错误
         */
        const val CONNECT_FAIL_CODE = 1
        const val CONNECT_TIMEOUT_CODE = 2
        const val CONNECT_ALREADY_CONNECTED_CODE = 3

        /**
         * 通信错误
         */
        //找不到 BluetoothGattService
        const val NOT_FOUND_SERVICE_CODE = 10
        //找不到 BluetoothGattCharacteristic
        const val NOT_FOUND_CHARACTERISTIC_CODE = 11
        //找不到 BluetoothGattDescriptor
        const val NOT_FOUND_DESCRIPTOR_CODE = 12
        //gatt为 null
        const val GATT_SERVICE_IS_NULL_CODE = 13
        //数据为 null
        const val DATA_IS_NULL_CODE = 14
        //设备未连接
        const val DEVICE_NOT_CONNECTED = 15
        //超时
        const val COMM_TIMEOUT_CODE = 16
        //未知
        const val COMM_UNKNOWN_ERROR_CODE = 16
    }

}