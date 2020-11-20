package com.mountains.bledemo.bean

data class DeviceInfoBean(
    val deviceId:Int,
    val deviceVersion:Int,
    val deviceBatteryStatus:Int,
    val electricity:Int,
    val customerID:Int,
    val isSupportHeartRateHistory:Boolean,
    val isSupportBloodOxygenHistory:Boolean,
    val isSupportBloodPressureHistory:Boolean,
    val isSupportSportModeHistory:Boolean

)