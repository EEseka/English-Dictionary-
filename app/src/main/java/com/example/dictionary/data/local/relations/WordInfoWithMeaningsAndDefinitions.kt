package com.example.dictionary.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.dictionary.data.local.entity.MeaningEntity
import com.example.dictionary.data.local.entity.WordInfoEntity

// Nested Relationships, can affect performance so i no fit spam am :(
data class WordInfoWithMeaningsAndDefinitions(
    @Embedded val word: WordInfoEntity,
    @Relation(
        entity = MeaningEntity::class,
        parentColumn = "wordId",
        entityColumn = "wordId"
    )
    val meanings: List<MeaningWithDefinitions>
)
