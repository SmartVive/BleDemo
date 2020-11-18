package com.mountains.bledemo.helper

object CommHelper {

    //查找手机，（震动）
    fun findDevice(flag:Boolean):ByteArray{
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 5
        bArr[2] = 3
        if (flag) {
            bArr[3] = 1
            return bArr
        }
        bArr[3] = 0
        return bArr
    }


    //检查心率
    fun checkHeartRate(i: Int): ByteArray {
        val bArr = ByteArray(20)
        bArr[0] = 5
        bArr[1] = 5
        bArr[2] = 1
        bArr[3] = i.toByte()
        return bArr
    }

}