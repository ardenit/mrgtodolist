package com.mirage.todolist.model.workers

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.mirage.todolist.R
import com.mirage.todolist.model.tasks.LiveTask
import com.mirage.todolist.model.tasks.TaskPeriod
import java.util.*
import java.util.concurrent.TimeUnit

fun scheduleAllDatetimeNotifications(appCtx: Context, tasks: Collection<LiveTask>) {
    val workManager = WorkManager.getInstance(appCtx)
    workManager.cancelAllWorkByTag(NotificationWorker.DATETIME_NOTIFICATION_WORKER_TAG)
    for (task in tasks) {
        val taskDate = task.date.value ?: continue
        val taskTime = task.time.value ?: continue
        if (!taskDate.isValid() || !taskTime.isValid()) continue
        val taskPeriod = task.period.value ?: continue
        val taskTitle = task.title.value ?: continue
        val calendar = Calendar.getInstance()
        calendar.set(taskDate.year, taskDate.monthOfYear, taskDate.dayOfMonth, taskTime.hour, taskTime.minute)
        val taskInitialTimeMillis = calendar.timeInMillis
        val workName = NotificationWorker.NOTIFICATION_WORK_NAME + "@" + task.taskID.toString()
        val workId = task.taskID.mostSignificantBits + task.taskID.leastSignificantBits
        val taskTimeText = twoDigits(taskTime.hour) + ":" + twoDigits(taskTime.minute)
        scheduleNextNotification(appCtx, workId, workName, taskTitle, taskTimeText, taskInitialTimeMillis, taskPeriod)
    }
}

fun scheduleNextNotification(
    appCtx: Context,
    workId: Long,
    workName: String,
    taskTitle: String,
    taskTimeText: String,
    taskInitialTimeMillis: Long,
    taskPeriod: TaskPeriod
) {
    val data = Data.Builder()
        .putLong(NotificationWorker.NOTIFICATION_ID, workId)
        .putString(NotificationWorker.NOTIFICATION_TASK_NAME, taskTitle)
        .putString(NotificationWorker.NOTIFICATION_TASK_TIME_TEXT, taskTimeText)
        .putString(NotificationWorker.NOTIFICATION_WORK_NAME, workName)
        .putLong(NotificationWorker.NOTIFICATION_TASK_INITIAL_TIME_MILLIS, taskInitialTimeMillis)
        .putInt(NotificationWorker.NOTIFICATION_TASK_PERIOD_ID, taskPeriod.ordinal)
        .build()
    val nextTime = getNextNotificationTime(taskInitialTimeMillis, taskPeriod)
    val timeBeforeNotify = getTimeBeforeNotify(appCtx.resources, PreferenceManager.getDefaultSharedPreferences(appCtx))
    val delay = nextTime - timeBeforeNotify - System.currentTimeMillis()
    if (delay < 0) return
    scheduleNotification(appCtx, delay, data, workName)
}

private fun getNextNotificationTime(taskInitialTimeMillis: Long, taskPeriod: TaskPeriod): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = taskInitialTimeMillis
    val dimension: Int = when (taskPeriod) {
        TaskPeriod.NOT_REPEATABLE -> return taskInitialTimeMillis
        TaskPeriod.DAILY -> Calendar.DAY_OF_YEAR
        TaskPeriod.WEEKLY -> Calendar.WEEK_OF_YEAR
        TaskPeriod.MONTHLY -> Calendar.MONTH
        TaskPeriod.YEARLY -> Calendar.YEAR
    }
    val currentTime = System.currentTimeMillis()
    while (calendar.timeInMillis < currentTime) {
        calendar.add(dimension, 1)
    }
    return calendar.timeInMillis
}

private fun scheduleNotification(appCtx: Context, delay: Long, data: Data, workName: String) {
    val notificationWork = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .addTag(NotificationWorker.DATETIME_NOTIFICATION_WORKER_TAG)
        .build()
    val instanceWorkManager = WorkManager.getInstance(appCtx)
    instanceWorkManager.beginUniqueWork(workName, ExistingWorkPolicy.REPLACE, notificationWork).enqueue()
}

private fun getTimeBeforeNotify(resources: Resources, preferences: SharedPreferences): Long {
    val notifyKey = resources.getString(R.string.key_notify_on_datetime)
    val notifyNever = resources.getString(R.string.value_notify_never)
    val notify5min = resources.getString(R.string.value_notify_5_min)
    val notify10min = resources.getString(R.string.value_notify_10_min)
    val notify30min = resources.getString(R.string.value_notify_30_min)
    val notify1hour = resources.getString(R.string.value_notify_1_hour)
    val preferenceValue = preferences.getString(notifyKey, notifyNever)
    return when (preferenceValue) {
        notify5min -> 5 * 60 * 1000
        notify10min -> 10 * 60 * 1000
        notify30min -> 30 * 60 * 1000
        notify1hour -> 1 * 60 * 60 * 1000
        else -> -1
    }.toLong()
}

private fun twoDigits(number: Int): String =
    if (number < 10) "0$number" else number.toString()