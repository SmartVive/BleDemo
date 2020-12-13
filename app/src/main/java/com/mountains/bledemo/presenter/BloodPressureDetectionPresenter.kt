package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.BloodPressureBean
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.event.DataUpdateEvent
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.BloodPressureDetectionView
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import org.litepal.extension.find

class BloodPressureDetectionPresenter : BasePresenter<BloodPressureDetectionView>() {
    private var bloodDiastolicList = mutableListOf<Int>()
    private var bloodSystolicList = mutableListOf<Int>()

    fun startBloodPressureDetection(){
        bloodDiastolicList.clear()
        bloodSystolicList.clear()
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
                //view?.showToast("停止血压检测失败:${exception.message}")
            }

        })
    }

    fun addBloodPressureDetectionResult(bloodDiastolic:Int,bloodSystolic:Int){
        bloodDiastolicList.add(bloodDiastolic)
        bloodSystolicList.add(bloodSystolic)
    }

    /**
     * 检测完成,并保存数据
     */
    fun detectionBloodPressureFinish(){
        stopBloodPressureDetection()

        val avgBloodDiastolic = bloodDiastolicList.average().toInt()
        val avgBloodSystolic = bloodSystolicList.average().toInt()
        Logger.d("血压检测完成，平均血压：$avgBloodDiastolic / $avgBloodSystolic mmHg")
        view?.onDetectionSuccess(avgBloodDiastolic,avgBloodSystolic)
        saveBloodPressure(avgBloodDiastolic,avgBloodSystolic)
    }

    /**
     * 保存数据
     */
    private fun saveBloodPressure(bloodDiastolic:Int,bloodSystolic:Int){
        val mac = DeviceManager.getDevice()?.getMac() ?: return
        val currentCalendar = CalendarUtil.getCurrentCalendar()
        val timeIndex = CalendarUtil.convertTimeToIndex(currentCalendar, 1)
        val newData = BloodPressureBean(mac,currentCalendar.timeInMillis, timeIndex, bloodDiastolic, bloodSystolic)
        val oldData = LitePal.where("datetime = ?", "${currentCalendar.timeInMillis}").find<BloodPressureBean>()
        if (oldData.isNotEmpty()){
            //有历史数据，更新
            newData.update(oldData.first().id)
        }else{
            //无历史数据，添加
            newData.save()
        }

        EventBus.getDefault().post(DataUpdateEvent(DataUpdateEvent.BLOOD_PRESSURE_UPDATE_TYPE))
    }
}