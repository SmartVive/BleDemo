package com.mountains.bledemo.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.mountains.bledemo.event.NotificationEvent
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.DeviceManager
import org.greenrobot.eventbus.EventBus

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        try {
            if (!sbn.notification.tickerText.isNullOrBlank()){
                val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
                val content = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
                if (title != null && content != null){
                    val pushMessageData = CommHelper.getPushMessageData("${title}:", content)
                    pushMessageData.forEach {
                        DeviceManager.writeCharacteristic(it)
                    }
                    EventBus.getDefault().post(NotificationEvent(title,content))
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}