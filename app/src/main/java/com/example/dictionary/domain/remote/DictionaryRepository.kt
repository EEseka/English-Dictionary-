package com.example.dictionary.domain.remote

import com.example.dictionary.domain.model.WordInfo
import com.example.dictionary.util.Resource
import kotlinx.coroutines.flow.Flow

interface DictionaryRepository {

    suspend fun initializeWords(): Flow<Resource<Unit>>

    suspend fun getWords(
        query: String,
        isFavorite: Boolean
    ): Flow<Resource<List<String>>>

    suspend fun getWordOfTheDay(): Flow<Resource<String?>>

    suspend fun getWordInfo(
        word: String,
        shouldAddToRecent: Boolean
    ): Flow<Resource<List<WordInfo>>>

    suspend fun addWord(
        words: List<WordInfo>
    ): Flow<Resource<Unit>>

    suspend fun deleteWords(
        words: List<String>
    ): Flow<Resource<Unit>>

    suspend fun deleteRecentWord(
        word: String
    ): Flow<Resource<Unit>>

    suspend fun getRecentWords(
        word: String
    ): Flow<Resource<List<String>>>

}