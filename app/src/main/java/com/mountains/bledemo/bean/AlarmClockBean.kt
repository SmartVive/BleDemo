package com.mountains.bledemo.bean

import org.litepal.crud.LitePalSupport

class AlarmClockBean : LitePalSupport(){
    var id:Long = 0
    var index = 0
    var hour:Int = 0
    var minute:Int = 0
    var repeat = mutableListOf<Int>()
    var isOpen  = false
    var mac = ""
}