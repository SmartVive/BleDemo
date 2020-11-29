package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.view.HeartRateDetectionView
import com.orhanobut.logger.Logger

class HeartRateDetectionPresenter : BasePresenter<HeartRateDetectionView>() {

    fun startHeartRateDetection(){
        DeviceManager.writeCharacteristic(CommHelper.heartRateDetection(1),object :CommCallback{
            override fun onSuccess(byteArray: ByteArray?) {
                view?.onStartDetection()
            }

            override fun onFail(exception: BleException) {
                Logger.e("开始心率检测失败：${exception.message}")
                view?.showToast("开始心率检测失败:${exception.message}")
                view?.onStopDetection()
            }

        })
    }

    fun stopHeartRateDetection(){
        DeviceManager.writeCharacteristic(CommHelper.heartRateDetection(0),object : CommCallback{
            override fun onSuccess(byteArray: ByteArray?) {
                view?.onStopDetection()
            }

            override fun onFail(exception: BleException) {
                Logger.e("停止心率检测失败：${exception.message}")
                view?.showToast("停止心率检测失败:${exception.message}")
            }

        })
    }
}