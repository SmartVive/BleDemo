package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.BloodOxygenBean
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.event.DataUpdateEvent
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.BloodOxygenDetectionView
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import org.litepal.extension.find

class BloodOxygenDetectionPresenter : BasePresenter<BloodOxygenDetectionView>() {
    private val bloodOxygenList = mutableListOf<Int>()

    fun startBloodOxygenDetection(){
        bloodOxygenList.clear()
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

    fun addBloodOxygenDetectionResult(bloodOxygen:Int){
        bloodOxygenList.add(bloodOxygen)
    }

    /**
     * 检测完成，并保存数据
     */
    fun bloodOxygenDetectionFinish(){
        stopBloodOxygenDetection()
        val avgBloodOxygen = bloodOxygenList.average().toInt()
        Logger.d("血氧检测完成，平均血氧：$avgBloodOxygen")
        view?.onBloodOxygenDetectionFinish(avgBloodOxygen)

        saveBloodOxygen(avgBloodOxygen)
    }

    private fun saveBloodOxygen(value:Int){
        val currentCalendar = CalendarUtil.getCurrentCalendar()
        val datetime = currentCalendar.timeInMillis
        val timeIndex = CalendarUtil.convertTimeToIndex(currentCalendar, 1)
        val newData = BloodOxygenBean(datetime,timeIndex,value)
        val oldData = LitePal.where("datetime = ?","$datetime").find<BloodOxygenBean>()
        if (oldData.isNotEmpty()){
            newData.update(oldData.first().id)
        }else{
            newData.save()
        }
        EventBus.getDefault().post(DataUpdateEvent(DataUpdateEvent.BLOOD_OXYGEN_UPDATE_TYPE))
    }
}