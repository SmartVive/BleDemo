package com.mountains.bledemo.ui.fragment

import android.app.AlertDialog
import android.content.ComponentName
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
import com.mountains.bledemo.event.DisconnectAllDeviceEvent
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import com.mountains.bledemo.helper.DeviceStorage
import com.mountains.bledemo.presenter.DevicePresenter
import com.mountains.bledemo.service.DeviceConnectService
import com.mountains.bledemo.service.NotificationService
import com.mountains.bledemo.ui.activity.AlarmClockActivity
import com.mountains.bledemo.ui.activity.AlarmClockAddActivity
import com.mountains.bledemo.ui.activity.BindDeviceActivity
import com.mountains.bledemo.ui.activity.WallpaperActivity
import com.mountains.bledemo.view.DeviceView
import kotlinx.android.synthetic.main.fragment_device.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class DeviceFragment : BaseFragment<DevicePresenter>(),DeviceView {

    companion object{
        const val BIND_DEVICE_REQUEST_CODE = 100
    }

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
        if (DeviceStorage.getInstance().mac == null){
            layoutNotBindDevice.visibility = View.VISIBLE
            btnBindDevice.setOnClickListener {
                val intent = Intent(context, BindDeviceActivity::class.java)
                startActivityForResult(intent,BIND_DEVICE_REQUEST_CODE)
            }
            return
        }

        layoutNotBindDevice.visibility = View.GONE
        tvDeviceName.text = DeviceStorage.getInstance().name
        tvDeviceMac.text = DeviceStorage.getInstance().mac

        //解除绑定
        unBindDevice.setOnClickListener {
            EventBus.getDefault().post(DisconnectAllDeviceEvent())
            DeviceStorage.getInstance().delete()
            initView()
        }

        layoutWallpaper.setOnClickListener {
            val intent = Intent(requireContext(), WallpaperActivity::class.java)
            startActivity(intent)
        }

        //查找手环
        layoutFindDevice.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setMessage("手环震动中...")
                .show()
            dialog.setOnDismissListener {
                DeviceManager.writeCharacteristic(CommHelper.findDevice(false))
            }

            DeviceManager.writeCharacteristic(CommHelper.findDevice(true))
        }

        //闹钟
        layoutAlarmClock.setOnClickListener {
            val intent = Intent(requireContext(), AlarmClockActivity::class.java)
            startActivity(intent)
        }

        //推送
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

        //自动检测心率
        switchAutoHeartRateDetection.setOnCheckedChangeListener { compoundButton, b ->
            if (!compoundButton.isPressed) return@setOnCheckedChangeListener
            presenter.setAutoHeartRateDetection(b)
        }

        //抬腕亮屏
        switchLiftWristBrightScreen.setOnCheckedChangeListener {  compoundButton, b ->
            if (!compoundButton.isPressed) return@setOnCheckedChangeListener
            presenter.setLiftWristBrightScreen(b)
        }

        if (isEnabledNotificationService()){
            switchPush.isChecked = true
        }

        switchAutoHeartRateDetection.isChecked = presenter.isAutoHeartRateDetection
        switchLiftWristBrightScreen.isChecked = presenter.isLiftWristBrightScreen
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
            if (requestCode == BIND_DEVICE_REQUEST_CODE){
                initView()
            }
        }
    }


    private fun setDeviceState(deviceMac:String,deviceName: String?,deviceState:String){
        tvDeviceState.text = deviceState
    }
}