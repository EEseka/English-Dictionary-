package com.example.dictionary.data.json

import com.example.dictionary.data.local.entity.WordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordsParser @Inject constructor() : JSONParser<WordEntity> {
    override suspend fun parse(inputStream: InputStream): List<WordEntity> {
        return withContext(Dispatchers.IO) {
            // Read the input stream as a String
            val json = inputStream.bufferedReader().use { it.readText() }

            // Convert the string into a JSONObject
            val jsonObject = JSONObject(json)

            // Extract the keys (words) and create WordEntity objects
            val wordList = mutableListOf<WordEntity>()
            jsonObject.keys().forEach { key ->
                wordList.add(WordEntity(word = key))
            }
            wordList
        }
    }
}