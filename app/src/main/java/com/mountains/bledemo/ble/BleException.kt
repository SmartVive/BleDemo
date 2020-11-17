package com.mountains.bledemo.ble

class BleException(val code:Int,val message:String) {
    companion object{
        /**
         * 连接错误
         */
        const val CONNECT_FAIL_CODE = 1
        const val CONNECT_TIMEOUT_CODE = 2

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