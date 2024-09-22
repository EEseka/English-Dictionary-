package com.example.dictionary.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dictionary.domain.model.WordInfo
import com.example.dictionary.domain.remote.DictionaryRepository
import com.example.dictionary.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DictionaryState())
    val state = _state.asStateFlow()
    private var searchJob: Job? = null
    private var likeJob: Job? = null


    fun onEvent(event: DictionaryEvents) {
        when (event) {
            is DictionaryEvents.OnClearClicked -> {
                deleteRecentWord("")
                getRecentWords()
            }

            is DictionaryEvents.OnUnlikeClicked -> {
                likeJob?.cancel()
                likeJob = viewModelScope.launch {
                    delay(1000L)
                    deleteWords(listOf(event.word))
                }
            }

            is DictionaryEvents.OnSwipeToDelete -> {
                Log.d("Dictionary ViewModel", "Swiping to delete word: ${event.word}")
                deleteWords(listOf(event.word))
            }

            is DictionaryEvents.OnSelectToDelete -> {
                Log.d("Dictionary ViewModel", "Deleting selected words: ${event.words}")
                deleteWords(event.words)
            }

            is DictionaryEvents.OnFavoritesSearchQueryChange -> {
                _state.value = _state.value.copy(
                    favoriteSearchQuery = event.query
                )
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getFavoriteWords()
                }
            }

            is DictionaryEvents.OnLikeClicked -> {
                likeJob?.cancel()
                likeJob = viewModelScope.launch {
                    delay(1000L)
                    addWord(event.wordInfo)
                }
            }

            is DictionaryEvents.OnRecentSearchQueryChange -> {
                _state.value = _state.value.copy(
                    recentSearchQuery = event.query
                )
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getRecentWords()
                }
            }

            is DictionaryEvents.OnRecentSwipeToDelete -> {
                deleteRecentWord(event.word)
            }

            is DictionaryEvents.OnSearchQueryChange -> {
                _state.value = _state.value.copy(
                    searchQuery = event.query
                )
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getWords()
                }
            }

            is DictionaryEvents.OnSuggestedWordClicked -> {
                getWordInfo()
            }
        }
    }

    private fun getWords(query: String = _state.value.searchQuery.lowercase().trim()) {
        viewModelScope.launch {
            dictionaryRepository.getWords(query, false).collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                wordsError = result.message
                            )
                        }

                        is Resource.Loading -> {
                            _state.value = _state.value.copy(
                                isWordsLoading = result.isLoading
                            )
                        }

                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                words = result.data ?: emptyList()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getFavoriteWords(
        query: String = _state.value.favoriteSearchQuery.lowercase().trim()
    ) {
        viewModelScope.launch {
            dictionaryRepository.getWords(query, true).collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                favoriteWordsError = result.message
                            )
                        }

                        is Resource.Loading -> {
                            _state.value = _state.value.copy(
                                isFavoriteLoading = result.isLoading
                            )
                        }

                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                favoriteWords = result.data?.toSet()?.toList() ?: emptyList()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getRecentWords(query: String = _state.value.recentSearchQuery.lowercase().trim()) {
        viewModelScope.launch {
            dictionaryRepository.getRecentWords(query).collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                recentWordsError = result.message
                            )
                        }

                        is Resource.Loading -> {
                            _state.value = _state.value.copy(
                                isRecentLoading = result.isLoading
                            )
                        }

                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                recentWords = result.data ?: emptyList()
                            )
                        }
                    }
                }
            }
        }
    }

//    private fun getWordOfTheDay() {
//        viewModelScope.launch {
//            dictionaryRepository.getWordOfTheDay().collect { result ->
//                withContext(Dispatchers.Main) {
//                    when (result) {
//                        is Resource.Error -> {
//                            _state.value = _state.value.copy(
//                                wordOfTheDayError = result.message
//                            )
//                        }
//
//                        is Resource.Loading -> {
//                            _state.value = _state.value.copy(
//                                isWordOfTheDayLoading = result.isLoading
//                            )
//                        }
//
//                        is Resource.Success -> {
//                            _state.value = _state.value.copy(
//                                wordOfTheDay = result.data
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun getWordInfo(
        word: String = savedStateHandle.get<String>("word") ?: "",
        shouldAddToRecent: Boolean = true
    ) {
        viewModelScope.launch {
            dictionaryRepository.getWordInfo(word, shouldAddToRecent).collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                wordInfoError = result.message
                            )
                        }

                        is Resource.Loading -> {
                            _state.value = _state.value.copy(
                                isWordInfoLoading = result.isLoading
                            )
                        }

                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                wordInfo = result.data ?: emptyList()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun addWord(word: List<WordInfo>) {
        viewModelScope.launch {
            dictionaryRepository.addWord(word).collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Error -> {
                            Log.e(
                                "DictionaryViewModel", "Error adding word: ${result.message}"
                            )
                        }

                        is Resource.Loading -> {
                            Log.d(
                                "DictionaryViewModel", "Adding word: ${result.isLoading}"
                            )
                        }

                        is Resource.Success -> {
                            Log.d("DictionaryViewModel", "Word added successfully")
                        }
                    }
                }
            }
        }
    }

    private fun deleteWords(word: List<String>) {
        viewModelScope.launch {
            dictionaryRepository.deleteWords(word).collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Error -> {
                            Log.e(
                                "DictionaryViewModel", "Error deleting word: ${result.message}"
                            )
                        }

                        is Resource.Loading -> {
                            Log.d(
                                "DictionaryViewModel", "Deleting word: ${result.isLoading}"
                            )
                        }

                        is Resource.Success -> {
                            Log.d("DictionaryViewModel", "Word deleted successfully")
                        }
                    }
                }
            }
        }
    }

    private fun deleteRecentWord(word: String) {
        viewModelScope.launch {
            dictionaryRepository.deleteRecentWord(word).collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Error -> {
                            Log.e(
                                "DictionaryViewModel",
                                "Error deleting recent word: ${result.message}"
                            )
                        }

                        is Resource.Loading -> {
                            Log.d(
                                "DictionaryViewModel", "Deleting recent word: ${result.isLoading}"
                            )
                        }

                        is Resource.Success -> {
                            Log.d("DictionaryViewModel", "Recent word deleted successfully")
                        }
                    }
                }
            }
        }
    }
}