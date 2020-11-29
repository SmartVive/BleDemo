package com.mountains.bledemo.util

import com.mountains.bledemo.bean.HeartRateBean
import org.litepal.extension.saveAll

object DaoManager {

    fun saveOrUpdate(oldDataList: MutableList<HeartRateBean>, newDataList: MutableList<HeartRateBean>) {
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

        var updateCount = 0
        var newDataIndex = newDataList.size - 1
        var oldDataIndex = oldDataList.size - 1
        while (newDataIndex >= 0 && oldDataIndex >= 0) {
            while(oldDataList[oldDataIndex].index != newDataList[newDataIndex].index){
                oldDataIndex--
            }
            //相同的index
            if(oldDataIndex >= 0){
                val newValue = newDataList[newDataIndex].value
                val oldValue = oldDataList[oldDataIndex].value
                if(newValue!= 0 && oldValue == 0){
                    //替换成新的数据
                    newDataList[newDataIndex].update(oldDataList[oldDataIndex].id)
                    updateCount++
                }else if (newValue!=0 && oldValue!=0){
                    //取平均值
                    newDataList[newDataIndex].value = (newValue+oldValue)/2
                    newDataList[newDataIndex].update(oldDataList[oldDataIndex].id)
                    updateCount++
                }
                newDataList.removeAt(newDataIndex)
            }
            oldDataIndex--
            newDataIndex--
        }
    }
}