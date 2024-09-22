package com.example.dictionary.di

import android.app.Application
import androidx.room.Room
import com.example.dictionary.data.local.DictionaryDataBase
import com.example.dictionary.data.remote.DictionaryApi
import com.example.dictionary.data.remote.RandomWordApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDictionaryApi(): DictionaryApi {
        return Retrofit.Builder()
            .baseUrl(DictionaryApi.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideRandomWordApi(): RandomWordApi {
        return Retrofit.Builder()
            .baseUrl(RandomWordApi.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideDictionaryDatabase(app: Application): DictionaryDataBase {
        return Room.databaseBuilder(
            app,
            DictionaryDataBase::class.java,
            "dictionarydb.db"
        ).fallbackToDestructiveMigration().build()
    }
}