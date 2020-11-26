package com.mountains.bledemo.event

class DataUpdateEvent(val type:Int) {
    companion object{
        const val HEART_RATE_UPDATE_TYPE = 1
    }
}