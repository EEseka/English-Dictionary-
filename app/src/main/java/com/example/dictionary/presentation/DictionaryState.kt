package com.example.dictionary.presentation

import com.example.dictionary.domain.model.WordInfo

data class DictionaryState(
    val words: List<String> = emptyList(),
    val favoriteWords: List<String> = emptyList(),
    val recentWords: List<String> = emptyList(),
    val wordInfo: List<WordInfo> = emptyList(),
    val isWordsLoading: Boolean = false,
    val isFavoriteLoading: Boolean = false,
    val isRecentLoading: Boolean = false,
    val isWordInfoLoading: Boolean = false,
    val searchQuery: String = "",
    val favoriteSearchQuery: String = "",
    val recentSearchQuery: String = "",
    val wordsError: String? = null,
    val favoriteWordsError: String? = null,
    val recentWordsError: String? = null,
    val wordInfoError: String? = null,
)