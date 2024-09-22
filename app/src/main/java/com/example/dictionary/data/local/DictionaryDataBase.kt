package com.example.dictionary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dictionary.data.local.entity.DefinitionEntity
import com.example.dictionary.data.local.entity.MeaningEntity
import com.example.dictionary.data.local.entity.RecentWordEntity
import com.example.dictionary.data.local.entity.WordEntity
import com.example.dictionary.data.local.entity.WordInfoEntity

@Database(
    entities = [
        WordInfoEntity::class, MeaningEntity::class,
        DefinitionEntity::class, WordEntity::class,
        RecentWordEntity::class
    ],
    version = 3
)
@TypeConverters(Converters::class)
abstract class DictionaryDataBase : RoomDatabase() {
    abstract val dao: DictionaryDao
}