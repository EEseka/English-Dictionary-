package com.example.dictionary.presentation.word_listings

data class FavoriteScreenItemsState(
    val favoriteWord: String,
    val isFavoriteWordSelected: Boolean = false
)