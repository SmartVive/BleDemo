package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.BloodPressureBean
import com.mountains.bledemo.view.BloodPressureDetailsView
import org.litepal.LitePal
import org.litepal.extension.find

class BloodPressureDetailsPresenter : BasePresenter<BloodPressureDetailsView>() {

    fun getBloodPressureData(beginTime:Long,endTime:Long){
        val bloodPressureData = LitePal.where("datetime between ? and ?", "$beginTime", "$endTime").order("datetime desc")
            .find<BloodPressureBean>()

        if (bloodPressureData.isEmpty()){
            view?.onBloodPressureData(bloodPressureData,"--","--","--","--")
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