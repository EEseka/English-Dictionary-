package com.example.dictionary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MeaningEntity(
    @PrimaryKey val meaningId: String,
    val wordId: String,
    val partOfSpeech: String,
    val synonyms: List<String>,
    val antonyms: List<String>
)
