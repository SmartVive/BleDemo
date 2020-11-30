package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.view.BloodPressureDetectionView
import com.orhanobut.logger.Logger

class BloodPressureDetectionPresenter : BasePresenter<BloodPressureDetectionView>() {
    private var bloodPressureList = mutableListOf<Int>()

    fun startBloodPressureDetection(){
        bloodPressureList.clear()
        DeviceManager.writeCharacteristic(CommHelper.bloodPressureDetection(1),object : CommCallback {
            override fun onSuccess(byteArray: ByteArray?) {
                view?.onStartDetection()
            }

            override fun onFail(exception: BleException) {
                Logger.e("开始血压检测失败：${exception.message}")
                view?.showToast("开始血压检测失败:${exception.message}")
                view?.onStopDetection()
            }

        })
    }

    fun stopBloodPressureDetection(){
        DeviceManager.writeCharacteristic(CommHelper.bloodPressureDetection(0),object : CommCallback {
            override fun onSuccess(byteArray: ByteArray?) {
                view?.onStopDetection()
            }

            override fun onFail(exception: BleException) {
                Logger.e("停止血压检测失败：${exception.message}")
                view?.showToast("停止血压检测失败:${exception.message}")
            }

        })
    }

    fun addBloodPressureDetectionResult(bloodPressure:Int){
        bloodPressureList.add(bloodPressure)
    }

    /**
     * 检测完成,保存数据
     */
    fun detectionBloodPressureFinish(){
        stopBloodPressureDetection()
    }
}