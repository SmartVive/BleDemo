package com.mountains.bledemo.helper

import com.mountains.bledemo.util.HexUtil
import com.orhanobut.logger.Logger
import java.util.*
import com.mountains.bledemo.bean.SleepBean
import com.mountains.bledemo.util.CalendarUtil
import org.litepal.LitePal
import org.litepal.extension.saveAll


class SleepDataDecodeHelper : IDataDecodeHelper {
    private var startCalendar: Calendar? = null
    private var endCalendar: Calendar? = null
    private var sleepDataCalendar: Calendar = Calendar.getInstance()
    private var mDeepSleepMinutes = 0
    private var mLightSleepMinutes = 0
    private var mSoberSleepMinutes = 0
    private var sleepDataList = mutableListOf<SleepBean.SleepData>()
    private val sleepBeanList = mutableListOf<SleepBean>()



    override fun decode(bArr: ByteArray) {
        if (HexUtil.bytes2HexString(bArr).startsWith("050704")) {
            Logger.d(bArr)
            val index = bArr[3].toInt()
            if (index == 0) {
                Logger.i("睡眠大数据:解析开始")
            }

            if (index % 4 == 0) {
                addData()
                startCalendar = getSleepCalendar(4, bArr)
                endCalendar = getSleepCalendar(9, bArr)
                Logger.i(
                    "睡眠大数据:入睡:%s,醒来:%s",
                    CalendarUtil.format("yyyy-MM-dd HH:mm", startCalendar!!),
                    CalendarUtil.format("yyyy-MM-dd HH:mm", endCalendar!!)
                )
                clearData(index == 0)
                sleepDataCalendar.time = startCalendar!!.time
            } else if (startCalendar != null) {
                for (i in 4 until 20 step 2) {
                    //先转成16bit数据，前2位为睡眠状态，后14位位睡眠时间
                    val src16Bit = convertTo16Bit(bArr[i], bArr[i + 1])
                    val sleepStatus = getSleepStatus(src16Bit)
                    val sleepMinutes = getSleepMinutes(src16Bit)
                    when (sleepStatus) {
                        SleepBean.STATUS_LIGHT -> mLightSleepMinutes += sleepMinutes;
                        SleepBean.STATUS_DEEP -> mDeepSleepMinutes += sleepMinutes;
                        SleepBean.STATUS_SOBER -> mSoberSleepMinutes += sleepMinutes;
                    }

                    if (sleepMinutes != 0) {
                        Logger.i(
                            "睡眠大数据:时间:%s,状态:%s,时长:%d",
                            CalendarUtil.format("yyyy-MM-dd HH:mm", sleepDataCalendar),
                            sleepStatus,
                            sleepMinutes
                        );
                    }

                    val sleepData = SleepBean.SleepData()
                    sleepData.minutes = sleepMinutes
                    sleepData.statuss = sleepStatus
                    sleepData.values = src16Bit
                    sleepData.beginTime = sleepDataCalendar.timeInMillis
                    sleepDataCalendar.add(Calendar.MINUTE, sleepMinutes);
                    sleepData.endTime = sleepDataCalendar.timeInMillis
                    sleepDataList.add(sleepData)
                }
            }
        }

        if (HexUtil.bytes2HexString(bArr).startsWith("0507FE", true)) {
            addData();
            Logger.i("睡眠大数据:解析完成");
            saveSleepData()
        }
    }

    private fun clearData(all: Boolean) {
        Logger.e("clearData")
        if (all){
            sleepBeanList.clear()
        }
        mLightSleepMinutes = 0
        mDeepSleepMinutes = 0
        mSoberSleepMinutes = 0
        sleepDataList = mutableListOf()
    }

    private fun addData() {
        if(sleepDataList.isNotEmpty() && startCalendar!=null && endCalendar != null){
            val sleepBean = SleepBean()
            sleepBean.beginDateTime = startCalendar!!.timeInMillis
            sleepBean.endDateTime = endCalendar!!.timeInMillis
            sleepBean.sleepData = sleepDataList
            sleepBean.light = mLightSleepMinutes
            sleepBean.deep = mDeepSleepMinutes
            sleepBean.sober = mSoberSleepMinutes
            sleepBeanList.add(sleepBean)
        }

    }

    /**
     * 保存睡眠数据
     */
    private fun saveSleepData(){
        if(sleepBeanList.isEmpty()){
            Logger.e("睡眠数据异常，不保存")
            return
        }

        for (sleepBean in sleepBeanList){
            if (sleepBean.sleepData.isEmpty()){
                Logger.e("睡眠详情数据异常，不保存")
                continue
            }
            val beginDateTime = sleepBean.beginDateTime
            val endDateTime = sleepBean.endDateTime
            sleepBean.sleepData.saveAll()
            sleepBean.save()
        }
    }


    private fun convertTo16Bit(num1: Byte, num2: Byte): Int {
        return ((num1.toInt() * 256) + num2.toInt()) and 65535
    }

    private fun getSleepStatus(src16Bit: Int): Int {
        return src16Bit shr 14
    }

    private fun getSleepMinutes(src16Bit: Int): Int {
        return src16Bit and 16383;
    }


    private fun getSleepCalendar(startIndex: Int, buffer: ByteArray): Calendar {
        val calendar = Calendar.getInstance();
        calendar.clear()
        val year = buffer[startIndex].toInt() + 2000
        val month = buffer[startIndex + 1].toInt() - 1
        val date = buffer[startIndex + 2].toInt()
        val hour = buffer[startIndex + 3].toInt()
        val minute = buffer[startIndex + 4].toInt()
        calendar.set(year, month, date, hour, minute, 0);
        return calendar;
    }

}