package com.example.dictionary.presentation

import com.example.dictionary.domain.model.WordInfo

sealed class DictionaryEvents {
    data class OnSearchQueryChange(val query: String) : DictionaryEvents()
    data class OnFavoritesSearchQueryChange(val query: String) : DictionaryEvents()
    data class OnRecentSearchQueryChange(val query: String) : DictionaryEvents()
    data object OnSuggestedWordClicked : DictionaryEvents()
    data class OnLikeClicked(val wordInfo: List<WordInfo>) : DictionaryEvents()
    data class OnUnlikeClicked(val word: String) : DictionaryEvents()
    data class OnSwipeToDelete(val word: String) : DictionaryEvents()
    data class OnSelectToDelete(val words: List<String>) : DictionaryEvents()
    data class OnRecentSwipeToDelete(val word: String) : DictionaryEvents()
    data object OnClearClicked : DictionaryEvents()
}