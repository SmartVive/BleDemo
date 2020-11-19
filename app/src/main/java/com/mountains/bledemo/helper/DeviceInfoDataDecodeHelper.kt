package com.mountains.bledemo.helper

import android.util.Log
import com.mountains.bledemo.util.HexUtil
import com.orhanobut.logger.Logger

class DeviceInfoDataDecodeHelper :IDataDecodeHelper {

    override fun decode(bArr: ByteArray?) {

        if (HexUtil.bytes2HexString(bArr).startsWith("050102")) {
            //电量
            val subBytes = HexUtil.subBytes(bArr, 7, 7)
            val bytesToInt = HexUtil.bytesToInt(subBytes)
            Logger.e("电量：${bytesToInt}")
        }

    }
}