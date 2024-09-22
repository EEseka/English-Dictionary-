package com.example.dictionary.presentation.word_listings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dictionary.presentation.DictionaryEvents
import com.example.dictionary.presentation.DictionaryViewModel

@Composable
fun WordListingsScreen(
    modifier: Modifier = Modifier,
    viewModel: DictionaryViewModel = hiltViewModel(),
    onItemClicked: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        viewModel.onEvent(DictionaryEvents.OnSearchQueryChange(""))
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            value = state.searchQuery,
            onValueChange = { viewModel.onEvent(DictionaryEvents.OnSearchQueryChange(it)) },
            onClearClicked = {
                if (state.searchQuery.isNotEmpty()) {
                    viewModel.onEvent(DictionaryEvents.OnSearchQueryChange(""))
                } else {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            },
            onSearchClicked = {
                if (state.words.isNotEmpty()) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                } else {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onItemClicked(state.searchQuery)
                }
            },
            focusRequester = focusRequester
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            if (state.isWordsLoading) {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.wordsError != null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.wordsError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(state.words.size) { i ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                onItemClicked(state.words[i])
                            }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.words[i],
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (i < state.words.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}