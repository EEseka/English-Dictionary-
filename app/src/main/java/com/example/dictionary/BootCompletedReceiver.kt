package com.example.dictionary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.dictionary.data.worker.WorkerScheduler

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule the worker when the device is booted
            WorkerScheduler.scheduleWordOfTheDayWorker(context)
        }
    }
}