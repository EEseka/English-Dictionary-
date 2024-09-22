package com.example.dictionary.util

sealed class Screen(val route: String, val title: String) {
    data object HomeScreen : Screen("home_screen", "Dictionary")
    data object WordListingsScreen : Screen("word_listings_screen", "Dictionary")
    data object FavoritesScreen : Screen("favorites_screen", "Favorites")
    data object RecentSearchScreen : Screen("recent_screen", "Recent")
    data object WordOfTheDay : Screen("word_of_the_day_screen/{word}", "Word of the Day") {
        fun createRoute(word: String) = "word_of_the_day_screen/$word"
    }
    data object WordInfoScreen : Screen("word_info_screen/{word}", "Dictionary") {
        fun createRoute(word: String) = "word_info_screen/$word"
    }
}