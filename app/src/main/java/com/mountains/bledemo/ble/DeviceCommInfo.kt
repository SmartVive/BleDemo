package com.mountains.bledemo.ble

/**
 * 设备操作前需要设置的信息
 */
class DeviceCommInfo private constructor(
    var operationType: Int?,
    var serviceUUID: String?,
    var characteristicUUID: String?,
    var descriptorUUID: String?
) {

    companion object{
        //写
        const val WRITE_OPERATION = 1
        //读
        const val READ_OPERATION = 2
        //通知
        const val NOTIFY_OPERATION = 3
        //指示器
        const val INDICATE_OPERATION = 4
    }

    class Builder{
        private var operationType:Int? = null
        private var serviceUUID: String? = null
        private var characteristicUUID: String? = null
        private var descriptorUUID: String? = null

        fun setOperationType(type:Int):Builder{
            operationType = type
            return this
        }

        fun setServiceUUID(uuid:String):Builder{
            serviceUUID = uuid
            return this
        }

        fun setCharacteristicUUID(uuid:String):Builder{
            characteristicUUID = uuid
            return this
        }

        fun setDescriptorUUID(uuid:String):Builder{
            descriptorUUID = uuid
            return this
        }

        fun build():DeviceCommInfo{
            return DeviceCommInfo(operationType, serviceUUID, characteristicUUID, descriptorUUID)
        }
    }
}