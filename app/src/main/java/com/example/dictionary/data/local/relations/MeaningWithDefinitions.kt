package com.example.dictionary.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.dictionary.data.local.entity.DefinitionEntity
import com.example.dictionary.data.local.entity.MeaningEntity

// Define a data class to represent the relationship between MeaningEntity and DefinitionEntity
// This is one to many by the way or one to n
data class MeaningWithDefinitions(
    @Embedded val meaning: MeaningEntity,
    @Relation(
        parentColumn = "meaningId",
        entityColumn = "meaningId"
    )
    val definitions: List<DefinitionEntity>
)
