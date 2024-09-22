package com.example.dictionary.data.repository

import android.app.Application
import com.example.dictionary.data.json.JSONParser
import com.example.dictionary.data.local.DictionaryDataBase
import com.example.dictionary.data.local.entity.RecentWordEntity
import com.example.dictionary.data.local.entity.WordEntity
import com.example.dictionary.data.mapper.toWordInfo
import com.example.dictionary.data.mapper.toWordInfoWithMeaningsAndDefinitions
import com.example.dictionary.data.remote.DictionaryApi
import com.example.dictionary.data.remote.RandomWordApi
import com.example.dictionary.domain.model.WordInfo
import com.example.dictionary.domain.remote.DictionaryRepository
import com.example.dictionary.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictionaryRepositoryImpl @Inject constructor(
    private val api: DictionaryApi,
    private val randomWordApi: RandomWordApi,
    private val db: DictionaryDataBase,
    private val parser: JSONParser<WordEntity>,
    private val context: Application
) : DictionaryRepository {

    private val dao = db.dao
    override suspend fun initializeWords(): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading(isLoading = true))
            try {
                // Check if words are already loaded to prevent unnecessary loading
                val wordCount = dao.getWords("").size
                if (wordCount == 0) {
                    // Open the JSON file from assets
                    val inputStream = context.assets.open("words_dictionary.json")

                    // Parse the JSON file into a list of WordEntity
                    val wordEntities = parser.parse(inputStream)

                    // Insert the words into the database
                    dao.insertWords(wordEntities)
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Success(Unit))
                }
            } catch (e: Exception) {
                emit(Resource.Error(message = e.message ?: "Unknown Error"))
            } finally {
                emit(Resource.Loading(isLoading = false))
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getWords(
        query: String,
        isFavorite: Boolean
    ): Flow<Resource<List<String>>> {
        return flow {
            emit(Resource.Loading(isLoading = true))
            if (isFavorite) {
                try {
                    val favoriteWords = dao.getFavoriteWords(query)
                    emit(Resource.Success(favoriteWords.map { it.toWordInfo(true).word }))
                    emit(Resource.Loading(isLoading = false))
                    return@flow
                } catch (e: Exception) {
                    emit(Resource.Error(e.message ?: "Unknown error occurred"))
                }
            }
            try {
                emit(Resource.Success(dao.getWords(query).map { it.word }))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            } finally {
                emit(Resource.Loading(isLoading = false))
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getWordOfTheDay(): Flow<Resource<String?>> {
        return flow {
            emit(Resource.Loading(isLoading = true))
            try {
                val randomWord = randomWordApi.getRandomWord()
                emit(Resource.Success(randomWord.firstOrNull()))
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            } finally {
                emit(Resource.Loading(isLoading = false))
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getWordInfo(
        word: String,
        shouldAddToRecent: Boolean
    ): Flow<Resource<List<WordInfo>>> {
        return flow {
            emit(Resource.Loading(isLoading = true))
            try {
                val favoriteWords = dao.getFavoriteWords(word)
                emit(Resource.Success(favoriteWords.map { it.toWordInfo(true) }))
                if (favoriteWords.isNotEmpty()) {
                    emit(Resource.Loading(isLoading = false))
                    return@flow
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            }

            try {
                val remoteWordInfo = withTimeout(10000) { api.getWordInfo(word) }
                val wordInfoEntity =
                    remoteWordInfo.map { it.toWordInfoWithMeaningsAndDefinitions() }
                if (shouldAddToRecent) {
                    dao.addRecentWord(RecentWordEntity(word, System.currentTimeMillis()))
                }
                emit(Resource.Success(wordInfoEntity.map { it.toWordInfo(false) }))
            } catch (e: TimeoutCancellationException) {
                emit(Resource.Error("Request timed out. Please try again."))
            } catch (e: HttpException) {
                e.printStackTrace()
                if (e.code() == 404) {
                    emit(Resource.Error("Word meaning not found in the dictionary"))
                } else {
                    emit(Resource.Error("Server error: ${e.message()}"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            } finally {
                emit(Resource.Loading(isLoading = false))
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun addWord(words: List<WordInfo>): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading(isLoading = true))
            try {
                val wordsEntity = words.map { it.toWordInfoWithMeaningsAndDefinitions() }
                dao.insertWordsWithMeaningsAndDefinitions(wordsEntity)
                emit(Resource.Success(Unit))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            } finally {
                emit(Resource.Loading(isLoading = false))
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun deleteWords(words: List<String>): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading(isLoading = true))
            try {
                words.forEach { word ->
                    val wordsEntity = dao.getFavoriteWords(word)
                    dao.deleteWordsWithMeaningsAndDefinitions(wordsEntity)
                    emit(Resource.Success(Unit))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            } finally {
                emit(Resource.Loading(isLoading = false))
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun deleteRecentWord(word: String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading(isLoading = true))
            try {
                if (word == "") dao.clearRecentWords()
                else dao.deleteRecentWord(word)
                emit(Resource.Success(Unit))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            } finally {
                emit(Resource.Loading(isLoading = false))
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getRecentWords(word: String): Flow<Resource<List<String>>> {
        return flow {
            emit(Resource.Loading(isLoading = true))
            try {
                val recentWords = dao.getRecentWords(word)
                emit(Resource.Success(recentWords.map { it.word }))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            } finally {
                emit(Resource.Loading(isLoading = false))
            }
        }.flowOn(Dispatchers.IO)
    }
}