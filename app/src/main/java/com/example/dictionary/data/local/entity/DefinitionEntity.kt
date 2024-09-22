package com.example.dictionary.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
data class DefinitionEntity(
    @PrimaryKey(autoGenerate = true) val definitionId: Long = 0L,
    val meaningId: String,
    val definition: String,
    val example: String
)
