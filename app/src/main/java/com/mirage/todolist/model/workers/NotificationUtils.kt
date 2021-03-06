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
import com.mirage.todolist.model.repository.LiveTask
import com.mirage.todolist.model.repository.TaskPeriod
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

fun scheduleAllDatetimeNotifications(appCtx: Context, tasks: Collection<LiveTask>) {
    val workManager = WorkManager.getInstance(appCtx)
    workManager.cancelAllWorkByTag(NotificationWorker.DATETIME_NOTIFICATION_WORKER_TAG)
    Timber.v("scheduleAllDatetimeNotifications $tasks")
    for (task in tasks) {
        Timber.v("task $task")
        val taskDate = task.date.value ?: continue
        val taskTime = task.time.value ?: continue
        if (!taskDate.dateSet || !taskTime.timeSet) continue
        val taskPeriod = task.period.value ?: continue
        val taskTitle = task.title.value ?: continue
        val calendar = Calendar.getInstance()
        calendar.set(taskDate.date.year, taskDate.date.monthValue - 1, taskDate.date.dayOfMonth, taskTime.time.hour, taskTime.time.minute)
        Timber.v("taskInitial ${calendar.toStr()} taskPeriod $taskPeriod")
        val taskInitialTimeMillis = calendar.timeInMillis
        val workName = NotificationWorker.NOTIFICATION_WORK_NAME + "@" + task.taskId.toString()
        val workId = task.taskId.mostSignificantBits + task.taskId.leastSignificantBits
        val taskTimeText = twoDigits(taskTime.time.hour) + ":" + twoDigits(taskTime.time.minute)
        scheduleNextNotification(appCtx, workId, workName, taskTitle, taskTimeText, taskInitialTimeMillis, taskPeriod, false)
    }
}

fun scheduleNextNotification(
    appCtx: Context,
    workId: Long,
    workName: String,
    taskTitle: String,
    taskTimeText: String,
    taskInitialTimeMillis: Long,
    taskPeriod: TaskPeriod,
    isRepeat: Boolean
) {
    val data = Data.Builder()
        .putLong(NotificationWorker.NOTIFICATION_ID, workId)
        .putString(NotificationWorker.NOTIFICATION_TASK_NAME, taskTitle)
        .putString(NotificationWorker.NOTIFICATION_TASK_TIME_TEXT, taskTimeText)
        .putString(NotificationWorker.NOTIFICATION_WORK_NAME, workName)
        .putLong(NotificationWorker.NOTIFICATION_TASK_INITIAL_TIME_MILLIS, taskInitialTimeMillis)
        .putInt(NotificationWorker.NOTIFICATION_TASK_PERIOD_ID, taskPeriod.ordinal)
        .build()
    val nextTime = getNextNotificationTime(appCtx, taskInitialTimeMillis, taskPeriod, isRepeat)
    val timeBeforeNotify = getTimeBeforeNotify(appCtx.resources, PreferenceManager.getDefaultSharedPreferences(appCtx))
    val targetTime = nextTime - timeBeforeNotify
    val currentTime = System.currentTimeMillis()
    if (currentTime > nextTime || currentTime > targetTime) {
        Timber.e("Invalid nextTime? nextTime=$nextTime currentTime=$currentTime")
    } else {
        val delay = targetTime - currentTime
        scheduleNotification(appCtx, delay, data, workName)
    }
}

private fun getNextNotificationTime(appCtx: Context, taskInitialTimeMillis: Long, taskPeriod: TaskPeriod, isRepeat: Boolean): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = taskInitialTimeMillis
    val timeBeforeNotify = getTimeBeforeNotify(appCtx.resources, PreferenceManager.getDefaultSharedPreferences(appCtx))
    val dimension: Int = when (taskPeriod) {
        TaskPeriod.NOT_REPEATABLE -> return if (isRepeat) -1L else taskInitialTimeMillis
        TaskPeriod.DAILY -> Calendar.DAY_OF_YEAR
        TaskPeriod.WEEKLY -> Calendar.WEEK_OF_YEAR
        TaskPeriod.MONTHLY -> Calendar.MONTH
        TaskPeriod.YEARLY -> Calendar.YEAR
    }
    val currentTime = System.currentTimeMillis()
    while (currentTime > calendar.timeInMillis - timeBeforeNotify - 1000L) {
        Timber.v("${calendar.toStr()} - skipped (currentTime=$currentTime calendarTime=${calendar.timeInMillis} target=${calendar.timeInMillis - timeBeforeNotify - 1000L})")
        calendar.add(dimension, 1)
    }
    Timber.v("getNextNotificationTime initial $taskInitialTimeMillis period $taskPeriod curTime $currentTime result ${calendar.toStr()} ${calendar.timeInMillis}")
    return calendar.timeInMillis
}

private fun scheduleNotification(appCtx: Context, delay: Long, data: Data, workName: String) {
    Timber.v("scheduleNotification delay $delay ms (${delay / 1000L / 60L} minutes)")
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

private fun Calendar.toStr(): String {
    return "${get(Calendar.DAY_OF_MONTH)}.${get(Calendar.MONTH) + 1}.${get(Calendar.YEAR)} ${get(Calendar.HOUR_OF_DAY)}:${get(Calendar.MINUTE)}"
}