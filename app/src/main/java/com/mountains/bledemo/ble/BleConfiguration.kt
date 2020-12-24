package com.mountains.bledemo.ble

object BleConfiguration {

    //搜索时间
    var scanTimeout = 15*1000L
        private set

    //连接超时时间
    var connectTimeout:Long = 20*1000
        private set

    //连接重试次数
    var connectRetryCount:Int = 5
        private set

    //通信超时时间
    var commTimeout:Long = 10*1000
        private set


    fun setScanTimeout(timeout:Long):BleConfiguration{
        scanTimeout = timeout
        return this
    }

    fun setConnectTimeout(timeout:Long):BleConfiguration{
        connectTimeout = timeout
        return this
    }

    fun setConnectRetryCount(count:Int):BleConfiguration{
        connectRetryCount = count
        return this
    }

    fun setCommTimeout(timeout:Long):BleConfiguration{
        commTimeout = timeout
        return this
    }
}