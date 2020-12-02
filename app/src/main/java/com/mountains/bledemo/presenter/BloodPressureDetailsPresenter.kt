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

        view?.onBloodPressureData(bloodPressureData)
    }
}