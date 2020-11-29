package com.mountains.bledemo.ble

object BleConfiguration {

    //搜索时间
    var scanTimeout = 15*1000L

    //连接超时时间
    var connectTimeout:Long = 15*1000

    //连接重试次数
    var connectRetryCount:Int = 5


    //通信超时时间
    var commTimeout:Long = 10*1000
}