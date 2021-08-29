package com.example.access

import android.os.Build
import android.service.notification.NotificationListenerService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class NotificationListener : NotificationListenerService() {

}