package com.mountains.bledemo.ui.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.app.NotificationManagerCompat
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseFragment
import com.mountains.bledemo.event.DeviceInfoEvent
import com.mountains.bledemo.event.DeviceStateEvent
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.presenter.DevicePresenter
import com.mountains.bledemo.service.NotificationService
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
        initView()
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
        when {
            electricity < 25 -> {
                ivElectricity.setImageResource(R.drawable.ic_electricity_lv1)
            }
            electricity < 50 -> {
                ivElectricity.setImageResource(R.drawable.ic_electricity_lv2)
            }
            electricity < 75 -> {
                ivElectricity.setImageResource(R.drawable.ic_electricity_lv3)
            }
            else -> {
                ivElectricity.setImageResource(R.drawable.ic_electricity_lv4)
            }
        }
    }



    private fun initView(){
        layoutFindDevice.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setMessage("手环震动中...")
                .show()
            dialog.setOnDismissListener {
                DeviceManager.writeCharacteristic(CommHelper.findDevice(false))
            }

            DeviceManager.writeCharacteristic(CommHelper.findDevice(true))
        }


        switchPush.setOnCheckedChangeListener { compoundButton, b ->
            if (!compoundButton.isPressed ) return@setOnCheckedChangeListener
            if (b){
                enableNotificationService()
                if (!isNotificationListenerEnabled()){
                    switchPush.isChecked = false
                    openNotificationListenSettings()
                }
            }else{
               disableNotificationService()
            }
        }

        switchAutoHeartRateDetection.setOnCheckedChangeListener { compoundButton, b ->
            if (!compoundButton.isPressed) return@setOnCheckedChangeListener
            if (b){
                DeviceManager.writeCharacteristic(CommHelper.setDeviceOtherInfo(true,false,true))
            }else{
                DeviceManager.writeCharacteristic(CommHelper.setDeviceOtherInfo(true,false,false))
            }
        }

        if (isEnabledNotificationService()){
            switchPush.isChecked = true
        }
    }

    //启用通知监听
    private fun enableNotificationService(){
        val packageManager = requireContext().packageManager
        packageManager.setComponentEnabledSetting(ComponentName(requireContext(),NotificationService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    //禁用通知监听
    private fun disableNotificationService(){
        val packageManager = requireContext().packageManager
        packageManager.setComponentEnabledSetting(ComponentName(requireContext(),NotificationService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
    }

    private fun isEnabledNotificationService():Boolean{
        val packageManager = requireContext().packageManager
        return packageManager.getComponentEnabledSetting(ComponentName(requireContext(),NotificationService::class.java)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(requireContext())
        return packageNames.contains(requireContext().packageName)
    }

    private fun openNotificationListenSettings() {
        try {
            val intent: Intent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            } else {
                intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            }
            startActivityForResult(intent,100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (isNotificationListenerEnabled()) {
                switchPush.isChecked = true
            }
        }
    }


    private fun setDeviceState(deviceMac:String,deviceName: String?,deviceState:String){
        tvDeviceName.text = deviceName
        tvDeviceMac.text = deviceMac
        tvDeviceState.text = deviceState
    }
}