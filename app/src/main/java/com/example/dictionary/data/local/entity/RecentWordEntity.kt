package com.example.dictionary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentWordEntity(
    @PrimaryKey val word: String,
    val timestamp: Long
)
