package com.mountains.bledemo.bean

import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

data class SportBean(
    val steps: Int,
    val mileage: Int,
    val calorie: Int
) {

    open class BaseSportBean(
        @Column(unique = true)
        val dateTime: String,
        val index: Int,
        val value: Int
    ): LitePalSupport()

    class StepBean(
        dateTime: String,
        index: Int,
        value: Int
    ) : BaseSportBean(dateTime, index, value)

    class DistanceBean(
        dateTime: String,
        index: Int,
        value: Int
    ) : BaseSportBean(dateTime, index, value)

    class CalorieBean(
        dateTime: String,
        index: Int,
        value: Int
    ) : BaseSportBean(dateTime, index, value)
}