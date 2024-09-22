package com.example.dictionary.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dictionary.data.datastore.WordOfTheDayDataStore
import com.example.dictionary.domain.remote.DictionaryRepository
import com.example.dictionary.util.Resource
import com.example.dictionary.util.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WordOfTheDayWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dictionaryRepository: DictionaryRepository
) : CoroutineWorker(context, workerParams) {

    private val wordDataStore = WordOfTheDayDataStore(context)
    private val notificationHelper = NotificationHelper(context)

    override suspend fun doWork(): Result {
        Log.d("WordOfTheDayWorker", "doWork() called")
        return withContext(Dispatchers.IO) {
            try {
                var word = ""
                var wordMeaning: String? = null
                var retryCount = 0
                val maxRetries = 5

                // Retry until we find a valid word with meaning
                do {
                    dictionaryRepository.getWordOfTheDay().collect { result ->
                        when (result) {
                            is Resource.Error -> {
                                Log.e(
                                    "WordOfTheDayWorker",
                                    "Error fetching word of the day: ${result.message}"
                                )
                            }

                            is Resource.Loading -> {
                                Log.d(
                                    "WordOfTheDayWorker",
                                    "Loading word of the day: ${result.isLoading}"
                                )
                            }

                            is Resource.Success -> {
                                word = result.data ?: ""
                            }
                        }
                    }
                    dictionaryRepository.getWordInfo(word, false).collect { result ->
                        when (result) {
                            is Resource.Error -> {
                                Log.e(
                                    "WordOfTheDayWorker",
                                    "Error fetching word meaning of $word: ${result.message}"
                                )
                            }

                            is Resource.Loading -> {
                                Log.d(
                                    "WordOfTheDayWorker",
                                    "Loading word meaning of $word: ${result.isLoading}"
                                )
                            }

                            is Resource.Success -> {
                                wordMeaning =
                                    result.data?.firstOrNull()?.meanings?.firstOrNull()?.definitions?.firstOrNull()?.definition
                            }
                        }
                    }
                    retryCount++
                } while ((wordMeaning.isNullOrEmpty() || wordMeaning == "Word meaning not found in the dictionary")
                    && retryCount < maxRetries
                )

                // Check if valid word found
                if (wordMeaning.isNullOrEmpty()) {
                    Log.e(
                        "WordOfTheDayWorker",
                        "No valid word found after $maxRetries retries"
                    )
                    return@withContext Result.failure()
                }

                // Save the word and its meaning to DataStore
                wordDataStore.saveWordOfTheDay(word, wordMeaning ?: "")

                // Show notification with word and meaning
                notificationHelper.showWordOfTheDayNotification(word, wordMeaning ?: "")
                Result.success()
            } catch (e: Exception) {
                Log.e(
                    "WordOfTheDayWorker",
                    "Error fetching word of the day, retrying due to failure: ${e.message}"
                )
                Result.retry() // Retry on failure
            }
        }
    }
}
