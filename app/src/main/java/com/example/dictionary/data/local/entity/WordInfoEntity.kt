package com.example.dictionary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WordInfoEntity(
    @PrimaryKey val wordId: String,
    val word: String,
    val text: String,
    val audio: String
)
