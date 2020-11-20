package com.mountains.bledemo.helper

import com.mountains.bledemo.bean.SportBean
import com.mountains.bledemo.event.SportEvent
import com.mountains.bledemo.util.HexUtil
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus

class SportDataDecodeHelper : IDataDecodeHelper{
    override fun decode(bArr: ByteArray?) {
        if (HexUtil.bytes2HexString(bArr).startsWith("050701")) {
            Logger.d(bArr)
            Logger.i("实时运动数据:解析开始")
            val steps: Int = HexUtil.bytes2Int(HexUtil.subBytes(bArr, 3, 6) )
            val mileage: Int = HexUtil.bytes2Int(HexUtil.subBytes(bArr, 7, 10))
            val calorie: Int = HexUtil.bytes2Int(HexUtil.subBytes(bArr, 11, 14))
            val z = steps == 0 && (mileage > 0 || calorie > 0)
            val z2 = calorie > 10000
            val z3 = steps > 99999
            if (z || z2 || z3) {
                Logger.w(
                    "实时运动数据:数据异常 步数:%d,距离:%d,卡路里:%d",
                    Integer.valueOf(steps),
                    Integer.valueOf(mileage),
                    Integer.valueOf(calorie)
                )
                return
            }
            Logger.d(
                "实时运动数据:步数:%d,距离:%d,卡路里:%d",
                Integer.valueOf(steps),
                Integer.valueOf(mileage),
                Integer.valueOf(calorie)
            )
            Logger.i("实时运动数据:解析结束")
            //asyncSaveRealTimeData(subBytesToInt, subBytesToInt2, subBytesToInt3)
            EventBus.getDefault().post(SportEvent(SportBean(steps,mileage,calorie)))
        }
    }
}