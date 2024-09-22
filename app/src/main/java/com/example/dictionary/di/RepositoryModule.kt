package com.example.dictionary.di


import com.example.dictionary.data.json.JSONParser
import com.example.dictionary.data.json.WordsParser
import com.example.dictionary.data.local.entity.WordEntity
import com.example.dictionary.data.repository.DictionaryRepositoryImpl
import com.example.dictionary.domain.remote.DictionaryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindWordsParser(
        wordsParser: WordsParser
    ): JSONParser<WordEntity>

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        dictionaryRepositoryImpl: DictionaryRepositoryImpl
    ): DictionaryRepository
}