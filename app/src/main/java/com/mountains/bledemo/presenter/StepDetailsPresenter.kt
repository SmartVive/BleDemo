package com.mountains.bledemo.presenter

import com.mountains.bledemo.base.BasePresenter
import com.mountains.bledemo.bean.SportBean
import com.mountains.bledemo.view.StepDetailsView
import org.litepal.LitePal
import org.litepal.extension.find
import org.litepal.extension.sum
import java.math.BigDecimal
import java.math.RoundingMode

class StepDetailsPresenter : BasePresenter<StepDetailsView>() {

    fun getStepsData(startTime:Long,endTime:Long){
        val stepsData = LitePal.where("datetime between ? and ? and value > ?", "$startTime", "$endTime","0").order("datetime desc")
            .find<SportBean.StepBean>()

        val distanceData = LitePal.where("datetime between ? and ? and value > ?", "$startTime", "$endTime","0").find<SportBean.DistanceBean>()

        val calorieData = LitePal.where("datetime between ? and ? and value > ?", "$startTime", "$endTime","0").find<SportBean.CalorieBean>()

        //val totalDistance = LitePal.where("datetime between ? and ? and value > ?", "$startTime", "$endTime","0").sum<SportBean.DistanceBean,Int>("value")

        val totalStep = stepsData.sumBy { it.value }
        val totalDistance = distanceData.sumBy { it.value }
        val totalCalorie = calorieData.sumBy { it.value }

        val totalStepString = if (totalStep == 0){
            "--"
        }else{
            "$totalStep"
        }

        val totalDistanceString = if (totalDistance == 0){
            "--"
        }else{
            val bigDecimal = BigDecimal(totalDistance.toDouble()/1000)
            bigDecimal.setScale(2, RoundingMode.HALF_UP).toString()
        }

        val totalCalorieString = if (totalCalorie == 0){
            "--"
        }else{
           "$totalCalorie"
        }

        view?.onStepsData(stepsData,totalStepString,totalDistanceString,totalCalorieString)
    }
}