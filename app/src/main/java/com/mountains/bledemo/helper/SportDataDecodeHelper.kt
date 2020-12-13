package com.mountains.bledemo.helper

import com.mountains.bledemo.bean.SportBean
import com.mountains.bledemo.event.SportEvent
import com.mountains.bledemo.util.CalendarUtil
import com.mountains.bledemo.util.HexUtil
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import org.litepal.extension.find
import org.litepal.extension.saveAll


import java.util.*


class SportDataDecodeHelper : IDataDecodeHelper {
    var mStepDataCalendar: Calendar? = null
    var mDistanceDataCalendar: Calendar? = null
    var mCaloriesDataCalendar: Calendar? = null
    val steps = mutableListOf<SportBean.StepBean>()
    val distances = mutableListOf<SportBean.DistanceBean>()
    val calories = mutableListOf<SportBean.CalorieBean>()


    override fun decode(bArr: ByteArray,mac:String) {
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
            Logger.i(
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
            val index = bArr[3].toInt() and 255
            if (index <= 11) {
                if (index == 0) {
                    Logger.i("运动大数据:解析开始")
                    mStepDataCalendar = CalendarUtil.getTodayBeginCalendar()
                    steps.clear()
                }
                if (mStepDataCalendar != null) {
                    if (index != 0 && index % 6 == 0) {
                        mStepDataCalendar!!.add(Calendar.DAY_OF_MONTH, -2)
                    }
                    for (i in 4 until 20 step 2) {
                        var value = HexUtil.subBytesToInt(bArr, 2, i, i + 1)
                        val timeIndex = CalendarUtil.convertTimeToIndex(mStepDataCalendar!!, 30)
                        val date = CalendarUtil.format("yyyy-MM-dd HH:mm:ss", mStepDataCalendar!!)
                        Logger.i("计步大数据:%d,index:%d,时间:%s", value, timeIndex, date)
                        if (value > 10000) {
                            value = 0
                        }
                        steps.add(SportBean.StepBean(mac,mStepDataCalendar!!.timeInMillis, timeIndex, value))
                        mStepDataCalendar!!.add(Calendar.MINUTE, 30)
                    }
                } else {
                    return
                }
            } else {
                return
            }
        }

        if (HexUtil.bytes2HexString(bArr).startsWith("050705",true)) {
            Logger.d(bArr)
            val index2 = bArr[3].toInt() and 255
            if (index2 <= 11) {
                if (index2 == 0) {
                    mDistanceDataCalendar = CalendarUtil.getTodayBeginCalendar()
                    distances.clear()
                }
                if (this.mDistanceDataCalendar != null) {
                    if (index2 != 0 && index2 % 6 == 0) {
                        mDistanceDataCalendar!!.add(Calendar.DAY_OF_MONTH, -2)
                    }
                    for (i in 4 until 20 step 2) {
                        val value = HexUtil.subBytesToInt(bArr, 2, i, i + 1)
                        val timeIndex = CalendarUtil.convertTimeToIndex(mDistanceDataCalendar!!, 30)
                        val date = CalendarUtil.format("yyyy-MM-dd HH:mm:ss", mDistanceDataCalendar!!)
                        Logger.i("距离大数据:%d,index:%d,时间:%s", Integer.valueOf(value), Integer.valueOf(timeIndex), date)
                        this.distances.add(SportBean.DistanceBean(mac,mDistanceDataCalendar!!.timeInMillis, timeIndex, value))
                        mDistanceDataCalendar!!.add(Calendar.MINUTE, 30)
                    }

                } else {
                    return
                }
            } else {
                return
            }
        }

        if (HexUtil.bytes2HexString(bArr).startsWith("050706",true)) {
            Logger.d(bArr)
            val index3 = bArr[3].toInt() and 255
            if (index3 <= 11) {
                if (index3 == 0) {
                    this.mCaloriesDataCalendar = CalendarUtil.getTodayBeginCalendar()
                    calories.clear()
                }
                if (this.mCaloriesDataCalendar != null) {
                    if (index3 != 0 && index3 % 6 == 0) {
                        mCaloriesDataCalendar!!.add(Calendar.DAY_OF_MONTH, -2)
                    }
                    for (i in 4 until 20 step 2) {
                        val value = HexUtil.subBytesToInt(bArr, 2, i, i + 1)
                        val timeIndex = CalendarUtil.convertTimeToIndex(mCaloriesDataCalendar!!, 30)
                        val date = CalendarUtil.format("yyyy-MM-dd HH:mm:ss", mCaloriesDataCalendar!!)
                        Logger.i("卡路里大数据:%d,index:%d,时间:%s", value, timeIndex, date)
                        calories.add(SportBean.CalorieBean(mac,mCaloriesDataCalendar!!.timeInMillis, timeIndex, value))
                        mCaloriesDataCalendar!!.add(Calendar.MINUTE, 30)
                    }
                } else {
                    return
                }
            } else {
                return
            }
        }



        if (HexUtil.bytes2HexString(bArr).startsWith("0507ff",true)) {
            Logger.i("运动大数据:解析完成")
            saveData()

        }

    }

    fun saveData(){
        //stepsSaveOrUpdate()
        saveOrUpdate(steps)
        saveOrUpdate(distances)
        saveOrUpdate(calories)
    }


    private inline fun<reified T : SportBean.BaseSportBean> saveOrUpdate(list:MutableList<T>){
        if (!checkData(list)){
            Logger.w("运动大数据异常，不保存")
            return
        }

        val startTime = list.last().dateTime
        val endTime = list.first().dateTime
        val existsData = LitePal.where("datetime between ? and  ?", "$startTime","$endTime").order("datetime desc").find<T>()
        var updateCount = 0
        for (i in existsData.lastIndex downTo  0){
            for(j in list.lastIndex downTo  0 ){
                //相同的时间，当前数值大于已存在数值时更新
                if(existsData[i].dateTime == list[j].dateTime){
                    if(list[j].value > existsData[i].value){
                        list[j].update(existsData[i].id)
                        updateCount++
                    }
                    list.removeAt(j)
                }
            }
        }

        Logger.d("更新${T::class.java.simpleName}条目数量：$updateCount")
        Logger.d("新增${T::class.java.simpleName}条目数量：${list.size}")
        list.saveAll()
    }

    /**
     * 检测数据是否正确
     */
    private fun<T : SportBean.BaseSportBean> checkData(list:MutableList<T>):Boolean{
        if(list.isEmpty()){
            return false
        }
        list.sort()
        val tomorrowCalendar = CalendarUtil.getTomorrowCalendar()
        val yesterdayCalendar = CalendarUtil.getYesterdayCalendar()
        //数据只能为昨天00：00点到今天23：59分,超过说明数据异常
        if (list.first().dateTime >= tomorrowCalendar.timeInMillis){
            return false
        }else if (list.last().dateTime < yesterdayCalendar.timeInMillis){
            return false
        }
        return true
    }

}