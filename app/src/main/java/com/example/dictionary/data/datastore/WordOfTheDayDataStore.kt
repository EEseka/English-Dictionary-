package com.example.dictionary.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension to create the DataStore
val Context.dataStore by preferencesDataStore("word_of_the_day")

class WordOfTheDayDataStore(private val context: Context) {

    companion object {
        private val WORD_KEY = stringPreferencesKey("word_of_the_day")
        private val MEANING_KEY = stringPreferencesKey("meaning_of_the_day")
    }

    // Save the word and its meaning in DataStore
    suspend fun saveWordOfTheDay(word: String, meaning: String) {
        context.dataStore.edit { preferences ->
            preferences[WORD_KEY] = word
            preferences[MEANING_KEY] = meaning
        }
    }

    // Fetch the word of the day
    val wordOfTheDay: Flow<Pair<String, String>> = context.dataStore.data.map { preferences ->
        val word = preferences[WORD_KEY] ?: ""
        val meaning = preferences[MEANING_KEY] ?: ""
        Pair(word, meaning)
    }
}