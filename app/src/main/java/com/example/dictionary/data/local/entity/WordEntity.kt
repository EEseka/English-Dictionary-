package com.example.dictionary.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["word"])]) // Speeds up search
data class WordEntity(
    @PrimaryKey val word: String
)
