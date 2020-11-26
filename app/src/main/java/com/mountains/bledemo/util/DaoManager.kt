package com.mountains.bledemo.util

import com.mountains.bledemo.bean.HeartRateBean
import org.litepal.extension.saveAll

object DaoManager {

    fun saveOrUpdate(oldDataList: MutableList<HeartRateBean>, newDataList: MutableList<HeartRateBean>) {
        for (newData in newDataList) {
            for (oldData in oldDataList) {
                if (newData.index == oldData.index && CalendarUtil.isSameDay(newData.dateTime, oldData.dateTime)) {

                    break
                }
            }
        }


        for (i in newDataList.size - 1 downTo 0) {
            for (j in oldDataList.size - 1 downTo 0) {
                val newData = newDataList[i]
                val oldData = oldDataList[j]
                if (newData.index == oldData.index && CalendarUtil.isSameDay(newData.dateTime, oldData.dateTime)) {
                    newData.update(oldData.id)
                    newDataList.removeAt(i)
                    break
                }
            }
        }
        newDataList.saveAll()
    }
}