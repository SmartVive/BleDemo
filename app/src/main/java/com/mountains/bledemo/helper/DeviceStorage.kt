package com.mountains.bledemo.helper

import org.litepal.LitePal
import org.litepal.crud.LitePalSupport
import org.litepal.extension.findAll

class DeviceStorage private constructor() {
    private var id:Long? = null
    var name:String? = null
    var mac:String? = null
    //是否自动检测心率
    var isAutoHeartRateDetection:Boolean = false
    //是否抬腕亮屏
    var isLiftWristBrightScreen:Boolean = false

    fun save() {
        //先删除后保存
        LitePal.deleteAll(Device::class.java)
        val device = Device()
        device.name = name
        device.mac = mac
        device.isAutoHeartRateDetection = isAutoHeartRateDetection
        device.isLiftWristBrightScreen = isLiftWristBrightScreen
        device.save()
    }

    fun delete(){
        LitePal.deleteAll(Device::class.java)
        id = null
        name = null
        mac = null
        isAutoHeartRateDetection = false
        isLiftWristBrightScreen = false
    }

    companion object{
        private var instance:DeviceStorage? = null
        fun getInstance():DeviceStorage{
            if (instance == null){
                val deviceList = LitePal.findAll<Device>()
                instance = DeviceStorage()
                if (deviceList.isNotEmpty()){
                    val device = deviceList.last()
                    instance!!.id = device.id
                    instance!!.mac = device.mac
                    instance!!.name = device.name
                    instance!!.isAutoHeartRateDetection = device.isAutoHeartRateDetection
                    instance!!.isLiftWristBrightScreen = device.isLiftWristBrightScreen
                }
            }
            return instance!!
        }
    }

    internal class Device : LitePalSupport(){
        var id:Long? = null
        var name:String? = null
        var mac:String? = null
        //是否自动检测心率
        var isAutoHeartRateDetection:Boolean = false
        //是否抬腕亮屏
        var isLiftWristBrightScreen:Boolean = false
    }
}