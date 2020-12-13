package com.mountains.bledemo.bean

import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

class TemperatureBean : LitePalSupport {
    val id:Long = 0
    var mac: String = ""
    var dateTime: Long = 0L
    var index: Int = 0
    var value: Int = 0

    constructor()

    constructor(mac:String,dateTime: Long,index: Int,value: Int){
        this.mac = mac
        this.dateTime = dateTime
        this.index = index
        this.value = value
    }
}