package com.example.dictionary.domain.model

data class Meaning(
    val meaningId: String,
    val partOfSpeech: String,
    val definitions: List<Definition>,
    val synonyms: List<String>,
    val antonyms: List<String>
)
