package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.BloodOxygenBean
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.view.BloodOxygenDetailsView
import org.litepal.LitePal
import org.litepal.extension.find

class BloodOxygenDetailsPresenter : BasePresenter<BloodOxygenDetailsView>() {

    fun getBloodOxygenData(beginTime:Long, endTime:Long){
        val mac = DeviceManager.getDevice()?.getMac()
        if (mac == null){
            view?.onBloodOxygenData(emptyList(),"--","--","--")
            return
        }

        val bloodOxygenData = LitePal.where("mac = ? and datetime between ? and ?", mac,"$beginTime", "$endTime").order("datetime desc")
            .find<BloodOxygenBean>()


        if (bloodOxygenData.isEmpty()){
            view?.onBloodOxygenData(emptyList(),"--","--","--")
            return
        }

        val maxBloodOxygen = bloodOxygenData.maxBy { it.value }?.value
        val minBloodOxygen = bloodOxygenData.minBy { it.value }?.value
        val avgBloodOxygen = bloodOxygenData.sumBy { it.value } / bloodOxygenData.size

        val maxBloodOxygenString = "$maxBloodOxygen %"
        val minBloodOxygenString = "$minBloodOxygen %"
        val avgBloodOxygenString = "$avgBloodOxygen %"

        view?.onBloodOxygenData(bloodOxygenData,avgBloodOxygenString,minBloodOxygenString,maxBloodOxygenString)
    }
}