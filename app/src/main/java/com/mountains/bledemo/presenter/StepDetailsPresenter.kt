package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.SportBean
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.view.StepDetailsView
import org.litepal.LitePal
import org.litepal.extension.find
import org.litepal.extension.sum
import java.math.BigDecimal
import java.math.RoundingMode

class StepDetailsPresenter : BasePresenter<StepDetailsView>() {

    fun getStepsData(startTime: Long, endTime: Long) {
        val mac = DeviceManager.getDevice()?.getMac()
        if (mac == null){
            view?.onStepsData(emptyList(), "--", "--", "--")
            return
        }

        val stepsData = LitePal.where("mac = ? and datetime between ? and ? and value > ?", mac,"$startTime", "$endTime", "0")
            .order("datetime desc")
            .find<SportBean.StepBean>()

        val distanceData = LitePal.where("mac = ? and datetime between ? and ? and value > ?",mac, "$startTime", "$endTime", "0")
            .find<SportBean.DistanceBean>()

        val calorieData = LitePal.where("mac = ? and datetime between ? and ? and value > ?", mac,"$startTime", "$endTime", "0")
            .find<SportBean.CalorieBean>()


        val totalStep = stepsData.sumBy { it.value }
        val totalDistance = distanceData.sumBy { it.value }
        val totalCalorie = calorieData.sumBy { it.value }

        var totalStepString = "--"
        var totalDistanceString = "--"
        var totalCalorieString = "--"

        if (totalStep != 0) {
            totalStepString = "${totalStep}步"
        }

        if (totalDistance != 0) {
            val bigDecimal = BigDecimal(totalDistance.toDouble() / 1000)
            val distance = bigDecimal.setScale(2, RoundingMode.HALF_UP).toString()
            totalDistanceString = "${distance}km"
        }

        if (totalCalorie != 0) {
            totalCalorieString = "${totalCalorie}大卡"
        }

        view?.onStepsData(stepsData, totalStepString, totalDistanceString, totalCalorieString)
    }
}