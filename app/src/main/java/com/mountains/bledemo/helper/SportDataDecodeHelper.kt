package com.mountains.bledemo.helper

import com.mountains.bledemo.bean.SportBean
import com.mountains.bledemo.event.SportEvent
import com.mountains.bledemo.util.HexUtil
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus
import org.litepal.extension.saveAll


import java.util.*
import java.text.ParseException
import java.text.SimpleDateFormat


class SportDataDecodeHelper : IDataDecodeHelper {
    var mStepDataCalendar: Calendar? = null
    var mDistanceDataCalendar: Calendar? = null
    var mCaloriesDataCalendar: Calendar? = null
    val steps = mutableListOf<SportBean.StepBean>()
    val distances = mutableListOf<SportBean.DistanceBean>()
    val calories = mutableListOf<SportBean.CalorieBean>()


    override fun decode(bArr: ByteArray?) {
        if (bArr == null) {
            return
        }
        if (HexUtil.bytes2HexString(bArr).startsWith("050701",true)) {
            Logger.d(bArr)
            Logger.i("实时运动数据:解析开始")
            val steps: Int = HexUtil.bytes2Int(HexUtil.subBytes(bArr, 3, 6))
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
            EventBus.getDefault().post(SportEvent(SportBean(steps, mileage, calorie)))
        }

        if (HexUtil.bytes2HexString(bArr).startsWith("050703",true)) {
            Logger.d(bArr)
            val index = bArr[3].toInt()
            if (index <= 11) {
                if (index == 0) {
                    Logger.d("运动大数据:解析开始")
                    mStepDataCalendar = getCurrentCalendarBegin();
                    steps.clear();
                }
                if (mStepDataCalendar != null) {
                    if (index != 0 && index % 6 == 0) {
                        mStepDataCalendar!!.add(Calendar.DAY_OF_MONTH, -2);
                    }
                    for (i in 4 until 20 step 2) {
                        var value = HexUtil.subBytesToInt(bArr, 2, i, i + 1);
                        val timeIndex = convertTimeToIndex(mStepDataCalendar!!, 30);
                        val date = getDate("yyyy-MM-dd HH:mm:ss", mStepDataCalendar!!);
                        Logger.d("计步大数据:%d,index:%d,时间:%s", value, timeIndex, date);
                        if (value > 10000) {
                            value = 0;
                        }
                        steps.add(SportBean.StepBean(date, timeIndex, value));
                        mStepDataCalendar!!.add(Calendar.MINUTE, 30);
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        if (HexUtil.bytes2HexString(bArr).startsWith("050705",true)) {
            Logger.d(bArr)
            val index2 = bArr[3].toInt()
            if (index2 <= 11) {
                if (index2 == 0) {
                    mDistanceDataCalendar = getCurrentCalendarBegin();
                    distances.clear();
                }
                if (this.mDistanceDataCalendar != null) {
                    if (index2 != 0 && index2 % 6 == 0) {
                        mDistanceDataCalendar!!.add(Calendar.DAY_OF_MONTH, -2);
                    }
                    for (i in 4 until 20 step 2) {
                        val value = HexUtil.subBytesToInt(bArr, 2, i, i + 1);
                        val timeIndex = convertTimeToIndex(mDistanceDataCalendar!!, 30);
                        val date = getDate("yyyy-MM-dd HH:mm:ss", mDistanceDataCalendar!!);

                        Logger.d("距离大数据:%d,index:%d,时间:%s", Integer.valueOf(value), Integer.valueOf(timeIndex), date);

                        this.distances.add(SportBean.DistanceBean(date, timeIndex, value));
                        mDistanceDataCalendar!!.add(Calendar.MINUTE, 30);
                    }

                } else {
                    return;
                }
            } else {
                return;
            }
        }

        if (HexUtil.bytes2HexString(bArr).startsWith("050706",true)) {
            Logger.d(bArr)
            val index3 = bArr[3].toInt()
            if (index3 <= 11) {
                if (index3 == 0) {
                    this.mCaloriesDataCalendar = getCurrentCalendarBegin();
                    calories.clear();
                }
                if (this.mCaloriesDataCalendar != null) {
                    if (index3 != 0 && index3 % 6 == 0) {
                        mCaloriesDataCalendar!!.add(Calendar.DAY_OF_MONTH, -2);
                    }
                    for (i in 4 until 20 step 2) {
                        val value = HexUtil.subBytesToInt(bArr, 2, i, i + 1)
                        val timeIndex = convertTimeToIndex(mCaloriesDataCalendar!!, 30)
                        val date = getDate("yyyy-MM-dd HH:mm:ss", mCaloriesDataCalendar!!)
                        Logger.d("卡路里大数据:%d,index:%d,时间:%s", value, timeIndex, date);
                        calories.add(SportBean.CalorieBean(date, timeIndex, value));
                        mCaloriesDataCalendar!!.add(Calendar.MINUTE, 30);
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }



        if (HexUtil.bytes2HexString(bArr).startsWith("0507ff",true)) {
            Logger.d("运动大数据:解析完成");
            steps.saveAll()
            distances.saveAll()
            calories.saveAll()
        }

    }


    fun getCurrentCalendarBegin(): Calendar {
        val c = Calendar.getInstance(Locale.ENGLISH);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    fun convertTimeToIndex(calendar: Calendar, mEveryMinutes: Int): Int {
        return convertTimeToIndex(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), mEveryMinutes);
    }

    fun convertTimeToIndex(hour: Int, minute: Int, mEveryMinutes: Int): Int {
        return ((60 / mEveryMinutes) * hour) + (minute / mEveryMinutes);
    }

    fun getDate(format: String, timeMillis: Long): String {
        return SimpleDateFormat(format, Locale.ENGLISH).format(timeMillis);
    }

    fun getDate(format: String, date: Date): String {
        return SimpleDateFormat(format, Locale.ENGLISH).format(date);
    }

    fun getDate(inFormat: String, outFormat: String, date: String): String {
        try {
            return convertStringToNewString(inFormat, outFormat, date);
        } catch (e: ParseException) {
            e.printStackTrace();
            return ""
        }
    }

    fun getDate(format: String, calendar: Calendar): String {
        return getDate(format, calendar.getTime());
    }

    fun convertStringToNewString(inFormat: String, outFormat: String, inDate: String): String {
        return getDate(outFormat, convertStringToDate(inFormat, inDate));
    }

    fun convertStringToDate(format: String, date: String): Date {
        return convertLongToDate(convertStringToLong(format, date));
    }

    fun convertLongToDate(timeInMillis: Long): Date {
        return Date(timeInMillis);
    }

    fun convertStringToLong(format: String, date: String): Long {
        return SimpleDateFormat(format, Locale.ENGLISH).parse(date).getTime();
    }


}