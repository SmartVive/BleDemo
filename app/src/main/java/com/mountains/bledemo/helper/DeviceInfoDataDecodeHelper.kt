package com.mountains.bledemo.helper

import android.util.Log
import com.mountains.bledemo.bean.DeviceInfoBean
import com.mountains.bledemo.util.HexUtil
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus
import kotlin.experimental.and


class DeviceInfoDataDecodeHelper : IDataDecodeHelper {

    override fun decode(bArr: ByteArray) {

        if (HexUtil.bytes2HexString(bArr).startsWith("050102")) {
            if (bArr.size < 10) {
                Logger.e("获取设备信息失败，数据异常")
                return
            }
            val mDeviceID = HexUtil.subBytesToInt(bArr, 2, 3, 4)
            val mDeviceVersion = bArr[5].toInt()
            val mDeviceBatteryStatus = bArr[6].toInt()
            val mDeviceBatteryLevel = bArr[7].toInt()
            val mCustomerID = HexUtil.subBytesToInt(bArr, 2, 8, 9);
            val mDeviceFeatures = bArr[10].toInt();
            //val mDeviceBatteryLevelStep = bArr[11].toInt()
            val isSupportHeartRateHistory = HexUtil.get1Bit(mDeviceFeatures, 0) == 1;
            val isSupportBloodOxygenHistory = HexUtil.get1Bit(mDeviceFeatures, 1) == 1;
            val isSupportBloodPressureHistory = HexUtil.get1Bit(mDeviceFeatures, 2) == 1;
            val isSupportSportModeHistory = HexUtil.get1Bit(mDeviceFeatures, 3) == 1;
            Logger.d("mDeviceID=$mDeviceID");
            Logger.d("mDeviceVersion=$mDeviceVersion");
            Logger.d("mDeviceBatteryStatus=$mDeviceBatteryStatus");
            Logger.d("mDeviceBatteryLevel=$mDeviceBatteryLevel");
            Logger.d("mCustomerID=$mCustomerID");
            Logger.d("mDeviceFeatures=$mDeviceFeatures");
            Logger.d("心率大数据支持=$isSupportHeartRateHistory");
            Logger.d("血氧大数据支持=$isSupportBloodOxygenHistory");
            Logger.d("血压大数据支持=$isSupportBloodPressureHistory");
            Logger.d("运动模式支持=$isSupportSportModeHistory");
            val deviceInfoBean = DeviceInfoBean(
                mDeviceID,
                mDeviceVersion,
                mDeviceBatteryStatus,
                mDeviceBatteryLevel,
                mCustomerID,
                isSupportHeartRateHistory,
                isSupportBloodOxygenHistory,
                isSupportBloodPressureHistory,
                isSupportSportModeHistory
            )
            EventBus.getDefault().post(deviceInfoBean)
        }


    }
}