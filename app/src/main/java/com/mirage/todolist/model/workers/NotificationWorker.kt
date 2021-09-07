package com.mirage.todolist.model.workers

import android.app.NotificationManager
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mirage.todolist.R
import com.mirage.todolist.model.tasks.TaskPeriod
import com.mirage.todolist.view.lockscreen.LockScreenActivity

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
        scheduleNextNotification(applicationContext, id, workName, taskTitle, taskTimeText, taskInitTimeMillis, taskPeriod)
        return Result.success()
    }

    private fun sendNotification(
        id: Int,
        taskName: String,
        taskTimeText: String
    ) {
        val intent = Intent(applicationContext, LockScreenActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(NOTIFICATION_ID, id)
        intent.putExtra(NOTIFICATION_TASK_NAME, taskName)

        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val bitmap = ContextCompat.getDrawable(applicationContext, R.drawable.baseline_event_24)?.toBitmap()
        val titleNotification = "Title notification $taskName"
        val subtitleNotification = "Subtitle notification $taskTimeText"
        val pendingIntent = getActivity(applicationContext, 0, intent, 0)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setLargeIcon(bitmap).setSmallIcon(R.drawable.mirage_todo_app_icon)
            .setContentTitle(titleNotification).setContentText(subtitleNotification)
            .setDefaults(DEFAULT_ALL).setContentIntent(pendingIntent).setAutoCancel(true)

        notification.priority = PRIORITY_MAX
        notificationManager.notify(id, notification.build())
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