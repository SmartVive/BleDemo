package com.mountains.bledemo.helper

import com.mountains.bledemo.util.HexUtil
import com.orhanobut.logger.Logger

class SportDataDecodeHelper : IDataDecodeHelper{
    override fun decode(bArr: ByteArray?) {
        Logger.d(bArr)
        if (HexUtil.bytes2HexString(bArr).startsWith("050701")) {
            Logger.i("实时运动数据:解析开始")
            val subBytesToInt: Int = HexUtil.bytesToInt(HexUtil.subBytes(bArr, 3, 6) )
            val subBytesToInt2: Int = HexUtil.bytesToInt(HexUtil.subBytes(bArr, 7, 10))
            val subBytesToInt3: Int = HexUtil.bytesToInt(HexUtil.subBytes(bArr, 11, 14))
            val z = subBytesToInt == 0 && (subBytesToInt2 > 0 || subBytesToInt3 > 0)
            val z2 = subBytesToInt3 > 10000
            val z3 = subBytesToInt > 99999
            if (z || z2 || z3) {
                Logger.w(
                    "实时运动数据:数据异常 步数:%d,距离:%d,卡路里:%d",
                    Integer.valueOf(subBytesToInt),
                    Integer.valueOf(subBytesToInt2),
                    Integer.valueOf(subBytesToInt3)
                )
                return
            }
            Logger.d(
                "实时运动数据:步数:%d,距离:%d,卡路里:%d",
                Integer.valueOf(subBytesToInt),
                Integer.valueOf(subBytesToInt2),
                Integer.valueOf(subBytesToInt3)
            )
            Logger.i("实时运动数据:解析结束")
            //asyncSaveRealTimeData(subBytesToInt, subBytesToInt2, subBytesToInt3)

        }
    }
}