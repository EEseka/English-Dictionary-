package com.example.dictionary.domain.model

data class WordInfo(
    val wordId: String,
    val word: String,
    val text: String,
    val audio: String,
    val meanings: List<Meaning>,
    val isLiked: Boolean
)
