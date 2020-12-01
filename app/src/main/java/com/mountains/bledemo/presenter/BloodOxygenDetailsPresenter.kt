package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.BloodOxygenBean
import com.mountains.bledemo.view.BloodOxygenDetailsView
import org.litepal.LitePal
import org.litepal.extension.find

class BloodOxygenDetailsPresenter : BasePresenter<BloodOxygenDetailsView>() {

    fun getBloodOxygenData(startTime:Long,endTime:Long){
        val bloodOxygenData = LitePal.where("datetime between ? and ?", "$startTime", "$endTime").order("datetime desc")
            .find<BloodOxygenBean>()


        if (bloodOxygenData.isEmpty()){
            view?.onBloodOxygenData(bloodOxygenData,"--","--","--")
            return
        }

        val maxBloodOxygen = bloodOxygenData.maxBy { it.value }?.value
        val minBloodOxygen = bloodOxygenData.minBy { it.value }?.value
        val avgBloodOxygen = bloodOxygenData.sumBy { it.value } / bloodOxygenData.size

        val maxBloodOxygenString = "$maxBloodOxygen %"
        val minBloodOxygenString = "$minBloodOxygen %"
        val avgBloodOxygenString = "$avgBloodOxygen %"

        view?.onBloodOxygenData(bloodOxygenData,avgBloodOxygenString,maxBloodOxygenString,minBloodOxygenString)
    }
}