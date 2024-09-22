package com.example.dictionary.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkerScheduler {
    fun scheduleWordOfTheDayWorker(context: Context) {
        // Define constraints: Requires network connection
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Get the time until the next morning
        val currentTime = Calendar.getInstance()
        val nextMidnight = Calendar.getInstance().apply {
            timeInMillis = currentTime.timeInMillis
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Move to the next day if it's already past 8 AM today
            if (currentTime.after(this)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Calculate the delay (in milliseconds) until next midnight
        val initialDelay = nextMidnight.timeInMillis - currentTime.timeInMillis

        val wordOfTheDayRequest = PeriodicWorkRequestBuilder<WordOfTheDayWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        Log.d(
            "WorkerScheduler",
            "Scheduled WordOfTheDayWorker with initial delay: $initialDelay ms"
        )

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WordOfTheDayWorker",
            ExistingPeriodicWorkPolicy.UPDATE, // Replace if already exists
            wordOfTheDayRequest
        )
    }
}
