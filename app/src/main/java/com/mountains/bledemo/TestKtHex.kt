package com.mountains.bledemo

fun main(){
    val num1:Byte = 64
    val num2:Byte = -59
    val i:Int = ((num1.toInt() and 255) * 256 + (num2.toInt() and 255)) and 65535
    println(i)
    println(i shr 14)
    println(i and 16383)
}