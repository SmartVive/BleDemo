package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.helper.DeviceStorage
import com.mountains.bledemo.view.DeviceView

class DevicePresenter : BasePresenter<DeviceView>() {

    val isLiftWristBrightScreen = DeviceStorage.getInstance().isLiftWristBrightScreen
    val isAutoHeartRateDetection = DeviceStorage.getInstance().isAutoHeartRateDetection


    fun setAutoHeartRateDetection(isOpen:Boolean){
        DeviceManager.writeCharacteristic(CommHelper.setDeviceOtherInfo(isLiftWristBrightScreen,false,isOpen))
        DeviceStorage.getInstance().isAutoHeartRateDetection = isOpen
        DeviceStorage.getInstance().save()
    }

    fun setLiftWristBrightScreen(isOpen:Boolean){
        DeviceManager.writeCharacteristic(CommHelper.setDeviceOtherInfo(isOpen,false,isAutoHeartRateDetection))
        DeviceStorage.getInstance().isLiftWristBrightScreen = isOpen
        DeviceStorage.getInstance().save()
    }
}