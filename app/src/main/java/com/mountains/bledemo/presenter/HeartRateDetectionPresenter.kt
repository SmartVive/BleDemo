package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.HeartRateBean
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.event.DataUpdateEvent
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.view.HeartRateDetectionView
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import org.litepal.extension.find

class HeartRateDetectionPresenter : BasePresenter<HeartRateDetectionView>() {
    private val heartRateList = mutableListOf<Int>()

    fun startHeartRateDetection(){
        heartRateList.clear()
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
                //view?.showToast("停止心率检测失败:${exception.message}")
            }

        })
    }

    /**
     * 添加心率结果，计算平均值
     */
    fun addHeartRateDetectionResult(heartRate:Int){
        heartRateList.add(heartRate)
    }

    /**
     * 心率检测完成并保存数据
     */
    fun heartRateDetectionFinish(){
        stopHeartRateDetection()
        val avgHeartRate = heartRateList.average().toInt()
        Logger.d("心率检测完成：平均心率:$avgHeartRate")

        saveHeartRate(avgHeartRate)
        view?.onDetectionFinish(avgHeartRate)
    }

    /**
     * 保存数据
     */
    private fun saveHeartRate(value:Int){
        val currentCalendar = CalendarUtil.getCurrentCalendar()
        val dateTime = currentCalendar.timeInMillis
        val timeIndex = CalendarUtil.convertTimeToIndex(currentCalendar, 1)
        val oldData = LitePal.where("datetime = ?", "$dateTime").find<HeartRateBean>()
        val newData = HeartRateBean(dateTime, timeIndex, value)
        if (oldData.isNotEmpty()){
            //已存在时更新
            newData.update(oldData.first().id)
        }else{
            //不存在保存
            newData.save()
        }

        EventBus.getDefault().post(DataUpdateEvent(DataUpdateEvent.HEART_RATE_UPDATE_TYPE))
    }
}