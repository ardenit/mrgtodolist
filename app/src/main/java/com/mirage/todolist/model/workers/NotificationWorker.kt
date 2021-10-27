package com.mirage.todolist.model.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mirage.todolist.R
import com.mirage.todolist.model.repository.TaskPeriod
import com.mirage.todolist.ui.lockscreen.LockScreenActivity

/**
 * Worker for creating push notifications about future tasks
 */
class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val id = inputData.getLong(NOTIFICATION_ID, 0)
        val workName = inputData.getString(NOTIFICATION_WORK_NAME) ?: ""
        val taskTitle = inputData.getString(NOTIFICATION_TASK_NAME) ?: ""
        val taskTimeText = inputData.getString(NOTIFICATION_TASK_TIME_TEXT) ?: ""
        val taskPeriodId = inputData.getInt(NOTIFICATION_TASK_PERIOD_ID, 0)
        val taskPeriod = TaskPeriod.values()[taskPeriodId.coerceIn(TaskPeriod.values().indices)]
        val taskInitTimeMillis = inputData.getLong(NOTIFICATION_TASK_INITIAL_TIME_MILLIS, 0L)
        sendNotification(id.toInt(), taskTitle, taskTimeText)
        scheduleNextNotification(applicationContext, id, workName, taskTitle, taskTimeText, taskInitTimeMillis, taskPeriod, true)
        return Result.success()
    }

    private fun sendNotification(
        id: Int,
        taskName: String,
        taskTimeText: String
    ) {
        println("SENDING NOTIFICATION FOR TASK $taskName AT TIME $taskTimeText ID $id")
        val channelId = "fadgfdasfasdfasd"
        //2021-09-14 02:40:41.608 554-2981/system_process E/NotificationService: No Channel found for pkg=com.mirage.todolist, channelId=mirage_todo_channel_01, id=-979562117, tag=null, opPkg=com.mirage.todolist, callingUid=10121, userId=0, incomingUserId=0, notificationUid=10121, notification=Notification(channel=mirage_todo_channel_01 shortcut=null contentView=null vibrate=null sound=null defaults=0x0 flags=0x10 color=0x00000000 vis=PRIVATE)
        //val notificationManager = NotificationManagerCompat.from(applicationContext)
        val bitmap = ContextCompat.getDrawable(applicationContext, R.drawable.baseline_event_24)?.toBitmap()
        val titleNotification = "Title notification $taskName"
        val subtitleNotification = "Subtitle notification $taskTimeText"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, titleNotification, importance)
            notificationManager.createNotificationChannel(channel)
            builder = NotificationCompat.Builder(applicationContext, channelId)
            val intent = Intent(applicationContext, LockScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
            builder.setContentTitle(titleNotification)
                .setSmallIcon(R.drawable.baseline_add_24)
                .setContentText(subtitleNotification)
                .setDefaults(DEFAULT_ALL)
                .setAutoCancel(true)
        }
        else {
            builder = NotificationCompat.Builder(applicationContext, channelId)
            val intent = Intent(applicationContext, LockScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
            builder.setContentTitle(titleNotification)
                .setSmallIcon(R.drawable.baseline_add_24)
                .setContentText(subtitleNotification)
                .setDefaults(DEFAULT_ALL)
                .setAutoCancel(true)
        }
        val notification = builder.build()
        notificationManager.notify(id, notification)
    }

    companion object {

        const val DATETIME_NOTIFICATION_WORKER_TAG = "mirage_todo_datetime_notification"

        const val NOTIFICATION_ID = "mirage_todo_notification_id"
        const val NOTIFICATION_CHANNEL = "mirage_todo_channel_01"
        const val NOTIFICATION_WORK_NAME = "mirage_todo_notification"

        const val NOTIFICATION_TASK_NAME = "mirage_todo_notification_task_name"
        const val NOTIFICATION_TASK_TIME_TEXT = "mirage_todo_notification_task_time_text"
        const val NOTIFICATION_TASK_INITIAL_TIME_MILLIS = "mirage_todo_notification_task_time_millis"
        const val NOTIFICATION_TASK_PERIOD_ID = "mirage_todo_notification_task_period_id"
    }
}