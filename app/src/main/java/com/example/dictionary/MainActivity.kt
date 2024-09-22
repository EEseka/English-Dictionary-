package com.example.dictionary

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.example.dictionary.data.worker.WorkerScheduler
import com.example.dictionary.presentation.shared.handleSharedText
import com.example.dictionary.ui.theme.DictionaryTheme
import com.example.dictionary.util.NetworkConnectionMonitor
import com.example.dictionary.util.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var networkConnectionMonitor: NetworkConnectionMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        networkConnectionMonitor = NetworkConnectionMonitor(this)

        // Initialize NotificationHelper, create the notification channel, and request notification permissions
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()
        notificationHelper.requestNotificationPermission(this)

        checkAndScheduleWorker()  // Schedule 'Word of the Day' worker

        installSplashScreen().apply {
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_X, 0.4f, 0.0f)
                zoomX.interpolator = OvershootInterpolator()
                zoomX.duration = 500L
                zoomX.doOnEnd { screen.remove() }
                val zoomY = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_Y, 0.4f, 0.0f)
                zoomY.interpolator = OvershootInterpolator()
                zoomY.duration = 500L
                zoomY.doOnEnd { screen.remove() }

                zoomX.start()
                zoomY.start()
            }
        }

        setContent {
            val isConnected =
                networkConnectionMonitor.networkStatusFlow.collectAsState(initial = true)
            DictionaryTheme {
                // Handle both shared text and word from notification
                val sharedWord = handleSharedText(intent) ?: intent.getStringExtra("word")
                Navigation(sharedWord = sharedWord, isConnected = isConnected.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)  // Update the intent used by the activity

        networkConnectionMonitor = NetworkConnectionMonitor(this)

        installSplashScreen().apply {
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_X, 0.4f, 0.0f)
                zoomX.interpolator = OvershootInterpolator()
                zoomX.duration = 500L
                zoomX.doOnEnd { screen.remove() }
                val zoomY = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_Y, 0.4f, 0.0f)
                zoomY.interpolator = OvershootInterpolator()
                zoomY.duration = 500L
                zoomY.doOnEnd { screen.remove() }

                zoomX.start()
                zoomY.start()
            }
        }

        // Recompose the UI to handle the new intent
        setContent {
            val isConnected =
                networkConnectionMonitor.networkStatusFlow.collectAsState(initial = true)
            DictionaryTheme {
                val sharedWord = handleSharedText(intent) ?: intent.getStringExtra("word")
                Navigation(sharedWord = sharedWord, isConnected = isConnected.value)
            }
        }
    }

    // Function to schedule the WordOfTheDay worker
    private fun checkAndScheduleWorker() {
        val workManager = WorkManager.getInstance(applicationContext)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val workInfos = workManager.getWorkInfosByTag("WordOfTheDayWorker").get()

                // If no worker exists or if the worker has finished, schedule a new one
                if (workInfos.isEmpty() || workInfos.all { it.state.isFinished }) {
                    WorkerScheduler.scheduleWordOfTheDayWorker(applicationContext)
                }
            }
        }
    }
}
