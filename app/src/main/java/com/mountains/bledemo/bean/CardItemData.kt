package com.mountains.bledemo.bean

import androidx.annotation.DrawableRes

class CardItemData(
    var itemType:Int,
    @DrawableRes
    var iconSrc:Int,
    var value:String?,
    var time:String?,
    var name:String
){
    companion object{
        //心率
        const val HEART_TYPE = 1
        //血压
        const val BLOOD_PRESSURE_TYPE = 2
        //血氧
        const val BLOOD_OXYGEN_TYPE = 3
        //睡眠
        const val SLEEP_TYPE = 4
        //设备
        const val DEVICE_TYPE = 5
    }
}