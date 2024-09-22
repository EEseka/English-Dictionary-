package com.example.dictionary

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.dictionary.domain.remote.DictionaryRepository
import com.example.dictionary.util.Resource
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DictionaryApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var dictionaryRepository: DictionaryRepository

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        GlobalScope.launch(Dispatchers.IO) {
            dictionaryRepository.initializeWords().collect { result ->
                when (result) {
                    is Resource.Error -> {
                        Log.e("DictionaryApp", "Error initializing words: ${result.message}")
                    }

                    is Resource.Loading -> {
                        Log.d("DictionaryApp", "Initializing words: ${result.isLoading}")
                    }

                    is Resource.Success -> {
                        Log.d("DictionaryApp", "Words initialized successfully")
                    }
                }
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory) // Provide HiltWorkerFactory
            .build()
}
