package com.mountains.bledemo.event

class DataUpdateEvent(val type:Int) {
    companion object{
        const val HEART_RATE_UPDATE_TYPE = 1
        const val BLOOD_OXYGEN_UPDATE_TYPE = 2
        const val BLOOD_PRESSURE_UPDATE_TYPE = 3
        const val SLEEP_UPDATE_TYPE = 4
    }
}