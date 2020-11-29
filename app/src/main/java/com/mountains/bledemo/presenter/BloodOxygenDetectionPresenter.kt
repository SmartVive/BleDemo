package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.view.BloodOxygenDetectionView
import com.orhanobut.logger.Logger

class BloodOxygenDetectionPresenter : BasePresenter<BloodOxygenDetectionView>() {

    fun startBloodOxygenDetection(){
        DeviceManager.writeCharacteristic(CommHelper.bloodOxygenDetection(1),object :CommCallback{
            override fun onSuccess(byteArray: ByteArray?) {
                view?.onStartDetection()
            }

            override fun onFail(exception: BleException) {
                Logger.e("开始血氧检测失败：${exception.message}")
                view?.showToast("开始血氧检测失败:${exception.message}")
                view?.onStopDetection()
            }

        })
    }

    fun stopBloodOxygenDetection(){
        DeviceManager.writeCharacteristic(CommHelper.bloodOxygenDetection(0),object : CommCallback {
            override fun onSuccess(byteArray: ByteArray?) {
                view?.onStopDetection()
            }

            override fun onFail(exception: BleException) {
                Logger.e("停止血氧检测失败：${exception.message}")
                view?.showToast("停止血氧检测失败:${exception.message}")
            }

        })
    }
}