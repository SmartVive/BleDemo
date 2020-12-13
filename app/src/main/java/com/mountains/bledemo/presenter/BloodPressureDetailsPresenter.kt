package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.BloodPressureBean
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.view.BloodPressureDetailsView
import org.litepal.LitePal
import org.litepal.extension.find

class BloodPressureDetailsPresenter : BasePresenter<BloodPressureDetailsView>() {

    fun getBloodPressureData(beginTime:Long,endTime:Long){
        val mac = DeviceManager.getDevice()?.getMac()
        if (mac == null){
            view?.onBloodPressureData(emptyList(),"--","--","--","--")
            return
        }

        val bloodPressureData = LitePal.where("mac = ? and datetime between ? and ?", mac,"$beginTime", "$endTime").order("datetime desc")
            .find<BloodPressureBean>()

        if (bloodPressureData.isEmpty()){
            view?.onBloodPressureData(emptyList(),"--","--","--","--")
        }else{
            val avgBloodDiastolic = bloodPressureData.sumBy { it.bloodDiastolic }/ bloodPressureData.size
            val avgBloodSystolic = bloodPressureData.sumBy { it.bloodSystolic }/ bloodPressureData.size
            val minBloodDiastolic = bloodPressureData.minBy { it.bloodDiastolic }?.bloodDiastolic
            val maxBloodSystolic = bloodPressureData.maxBy { it.bloodSystolic }?.bloodSystolic

            val avgBloodDiastolicString = "$avgBloodDiastolic mmHg"
            val avgBloodSystolicString = "$avgBloodSystolic mmHg"
            val minBloodDiastolicString = "$minBloodDiastolic mmHg"
            val maxBloodSystolicString = "$maxBloodSystolic mmHg"
            view?.onBloodPressureData(bloodPressureData,avgBloodDiastolicString,avgBloodSystolicString,minBloodDiastolicString,maxBloodSystolicString)
        }
    }
}