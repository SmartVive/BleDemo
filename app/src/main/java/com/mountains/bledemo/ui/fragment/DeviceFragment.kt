package com.mountains.bledemo.ui.fragment

import android.os.Bundle
import android.view.View
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseFragment
import com.mountains.bledemo.event.DeviceInfoEvent
import com.mountains.bledemo.event.DeviceStateEvent
import com.mountains.bledemo.presenter.DevicePresenter
import com.mountains.bledemo.view.DeviceView
import kotlinx.android.synthetic.main.fragment_device.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DeviceFragment : BaseFragment<DevicePresenter>(),DeviceView {
    override fun createPresenter(): DevicePresenter {
        return DevicePresenter()
    }

    override fun setContentView(): Int {
        return R.layout.fragment_device
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceConnectState(deviceStateEvent: DeviceStateEvent){
        val deviceMac = deviceStateEvent.deviceMac
        val deviceName = deviceStateEvent.deviceName
        when(deviceStateEvent.type){
            DeviceStateEvent.CONNECTED_TYPE->{
                setDeviceState(deviceMac,deviceName,"已连接")
            }
            DeviceStateEvent.CONNECT_FAIL_TYPE->{
                setDeviceState(deviceMac,deviceName,"连接失败")
            }
            DeviceStateEvent.DISCONNECT_TYPE->{
                setDeviceState(deviceMac,deviceName,"断开连接")
            }
            DeviceStateEvent.CONNECTING_TYPE->{
                setDeviceState(deviceMac,deviceName,"正在连接...")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceInfo(deviceInfoEvent: DeviceInfoEvent){
        val electricity = deviceInfoEvent.deviceInfoBean.electricity
        if (electricity < 25){
            ivElectricity.setImageResource(R.drawable.ic_electricity_lv1)
        }else if (electricity < 50){
            ivElectricity.setImageResource(R.drawable.ic_electricity_lv2)
        }else if (electricity < 75){
            ivElectricity.setImageResource(R.drawable.ic_electricity_lv3)
        }else{
            ivElectricity.setImageResource(R.drawable.ic_electricity_lv4)
        }
    }

    private fun setDeviceState(deviceMac:String,deviceName: String?,deviceState:String){
        tvDeviceName.text = deviceName
        tvDeviceMac.text = deviceMac
        tvDeviceState.text = deviceState
    }
}