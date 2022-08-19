package com.lhr.common.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

/**
 * @author lhr
 * @date 2021/9/17
 * @des activity 启动工具
 */
object ActivityLaunchUtils : Application.ActivityLifecycleCallbacks {
    private val TAG = "AS_${ActivityLaunchUtils::class.java.simpleName}"
    private const val LAUNCH_SIGN = "launch_sign"
    private const val INTENT_CODE = 10102

    private const val NOTIFICATION_ID = 10001
    private const val NOTIFICATION_TAG = "Launch_TAG"
    private const val NOTIFICATION_NAME = "Notification"
    private const val NOTIFICATION_CHANNEL_ID = "ActivityLaunchUtils"
    private const val MSG_WHAT = 101

    private var app: Application? = null
    private var notificationManager: NotificationManager? = null
    private var alarmManager: AlarmManager? = null
    private var launchHandler: Handler? = null

    private var waitIntent: LaunchIntent? = null

    private class LaunchHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(message: Message) {
            super.handleMessage(message)
            if (message.what == MSG_WHAT) {
                try {
                    notificationManager?.cancel(NOTIFICATION_TAG, NOTIFICATION_ID)
                } catch (ignored: java.lang.Exception) {
                }
            }
        }
    }

    /**
     * 启动activity
     */
    fun launchActivity(context: Context, intent: Intent) {
        if (checkAndInit(context)) {
            app?.let {
                intent.putExtra(LAUNCH_SIGN, LAUNCH_SIGN)
                waitIntent = LaunchIntent(it,intent)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    launchActivityByAlarm(it, waitIntent!!)
                } else {
                    launchActivityByNotification(it, waitIntent!!)
                }
            }
        }
    }

    /**
     * 利用NotificationService发送一个类似通话的通知启动PendingIntent，适用于 version < 29
     */
    private fun launchActivityByNotification(context: Context, intent: LaunchIntent) {
        try {
            intent.pendingIntent.send()
        } catch (unused: PendingIntent.CanceledException) {
            context.startActivity(intent.intent)
        }
        try {
            notificationManager?.let {
                val channel = createNotificationChannel(it)
                it.cancel(NOTIFICATION_TAG, NOTIFICATION_ID)
                val notificationBuilder: NotificationCompat.Builder =
                    NotificationCompat.Builder(context, channel)
                        .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                        .setContentTitle("")
                        .setContentText("") //以下为关键的3行
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setFullScreenIntent(intent.pendingIntent, true)
                it.notify(
                    NOTIFICATION_TAG,
                    NOTIFICATION_ID,
                    notificationBuilder.build()
                )
            }

            launchHandler?.let {
                it.removeMessages(MSG_WHAT)
                it.sendEmptyMessageDelayed(MSG_WHAT, 1000)
            }
        } catch (ignored: Exception) {
        }
    }

    /**
     * 利用AlarmService服务执行定时任务启动PendingIntent，适用于 version >= 29
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun launchActivityByAlarm(context: Context, intent: LaunchIntent) {
        try {
            context.startActivity(intent.intent)
        } catch (ignored: java.lang.Exception) {
        }
        //实际时间远超过80ms
        alarmManager?.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 80, intent.pendingIntent)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager): String {
        if (Build.VERSION.SDK_INT < 26 || notificationManager.getNotificationChannel(
                NOTIFICATION_CHANNEL_ID
            ) != null
        ) {
            return NOTIFICATION_CHANNEL_ID
        }
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        notificationChannel.enableLights(false)
        notificationChannel.enableVibration(false)
        notificationChannel.setShowBadge(false)
        notificationChannel.name = " "
        notificationChannel.description = " "
        notificationChannel.setSound(null, null)
        notificationChannel.setBypassDnd(true)
        notificationManager.createNotificationChannel(notificationChannel)
        return NOTIFICATION_CHANNEL_ID
    }

    /**
     * 检查并初始化app工具
     */
    private fun checkAndInit(context: Context): Boolean {
        if (app == null) {
            app = try {
                context.applicationContext as Application
            } catch (e: Exception) {
                null
            }
            app?.let {
                notificationManager = it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                alarmManager = it.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                launchHandler = LaunchHandler(Looper.getMainLooper())
                it.registerActivityLifecycleCallbacks(this)
            }
        }
        return app != null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d(TAG,"on created $activity ")
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d(TAG,"on destroy $activity ")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity.intent.getStringExtra(LAUNCH_SIGN) == waitIntent?.intent?.getStringExtra(
                LAUNCH_SIGN
            )){
            launchHandler?.removeMessages(MSG_WHAT)
            try {
                if (waitIntent?.pendingIntent != null){
                    alarmManager?.cancel(waitIntent?.pendingIntent)
                }
            }catch (e: Throwable){

            }
            waitIntent = null
        }
    }

    private class LaunchIntent(context: Context, val intent: Intent){
        val pendingIntent:PendingIntent = PendingIntent.getActivity(
            context, INTENT_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        init {
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
    }
}