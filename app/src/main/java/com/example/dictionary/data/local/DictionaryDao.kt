package com.example.dictionary.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.dictionary.data.local.entity.DefinitionEntity
import com.example.dictionary.data.local.entity.MeaningEntity
import com.example.dictionary.data.local.entity.RecentWordEntity
import com.example.dictionary.data.local.entity.WordEntity
import com.example.dictionary.data.local.entity.WordInfoEntity
import com.example.dictionary.data.local.relations.WordInfoWithMeaningsAndDefinitions

@Dao
interface DictionaryDao {

    // For the list of English words
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(word: List<WordEntity>)

    @Query("SELECT * FROM wordentity WHERE word LIKE LOWER(:query) || '%' LIMIT 50")
    suspend fun getWords(query: String): List<WordEntity>

    // For the API response
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordInfo(word: List<WordInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeanings(meaning: List<MeaningEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefinitions(definition: List<DefinitionEntity>)

    @Transaction
    suspend fun insertWordsWithMeaningsAndDefinitions(wordsWithMeaningsAndDefinitions: List<WordInfoWithMeaningsAndDefinitions>) {
        // Insert all words
        val words = wordsWithMeaningsAndDefinitions.map { it.word }
        insertWordInfo(words)

        // Insert meanings and definitions in batch
        val meanings = mutableListOf<MeaningEntity>()
        val definitions = mutableListOf<DefinitionEntity>()

        wordsWithMeaningsAndDefinitions.forEach { wordWithMeaningsAndDefinitions ->
            meanings.addAll(wordWithMeaningsAndDefinitions.meanings.map { it.meaning })
            wordWithMeaningsAndDefinitions.meanings.forEach { meaningWithDefinitions ->
                definitions.addAll(meaningWithDefinitions.definitions)
            }
        }

        insertMeanings(meanings)
        insertDefinitions(definitions)
    }

    @Delete
    suspend fun deleteWordInfo(word: List<WordInfoEntity>)

    @Delete
    suspend fun deleteMeanings(meaning: List<MeaningEntity>)

    @Delete
    suspend fun deleteDefinitions(definition: List<DefinitionEntity>)

    @Transaction
    suspend fun deleteWordsWithMeaningsAndDefinitions(wordsWithMeaningsAndDefinitions: List<WordInfoWithMeaningsAndDefinitions>) {
        // Delete all words
        val words = wordsWithMeaningsAndDefinitions.map { it.word }
        deleteWordInfo(words)

        // Delete all meanings and definitions in batch
        val meanings = mutableListOf<MeaningEntity>()
        val definitions = mutableListOf<DefinitionEntity>()

        wordsWithMeaningsAndDefinitions.forEach { wordWithMeaningsAndDefinitions ->
            meanings.addAll(wordWithMeaningsAndDefinitions.meanings.map { it.meaning })
            wordWithMeaningsAndDefinitions.meanings.forEach { meaningWithDefinitions ->
                definitions.addAll(meaningWithDefinitions.definitions)
            }
        }

        deleteMeanings(meanings)
        deleteDefinitions(definitions)
    }

    @Transaction
    @Query(
        """
        SELECT * FROM WordInfoEntity
        WHERE LOWER(word) LIKE LOWER(:word) || '%'
        """
    )
    suspend fun getFavoriteWords(word: String): List<WordInfoWithMeaningsAndDefinitions>

    // For the recent words
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecentWord(word: RecentWordEntity)

    @Query("DELETE FROM recentwordentity WHERE word = :word")
    suspend fun deleteRecentWord(word: String)

    @Query("DELETE FROM recentwordentity")
    suspend fun clearRecentWords()

    @Query(
        """
        SELECT * FROM recentwordentity
        WHERE LOWER(word) LIKE LOWER(:word) || '%'
        ORDER BY timestamp DESC
        """
    )
    suspend fun getRecentWords(word: String): List<RecentWordEntity>
}