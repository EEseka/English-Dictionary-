package com.example.dictionary.presentation.word_listings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dictionary.R
import com.example.dictionary.presentation.DictionaryEvents
import com.example.dictionary.presentation.DictionaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: DictionaryViewModel = hiltViewModel(),
    scrollBehavior: TopAppBarScrollBehavior,
    isTopBarDeleteClicked: Boolean,
    isListEmpty: (Boolean) -> Unit,
    onItemClicked: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isExpanded by remember {
        mutableStateOf(false)
    }

    var recentWords by remember {
        mutableStateOf(emptyList<String>())
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(DictionaryEvents.OnRecentSearchQueryChange(""))
    }

    LaunchedEffect(state.recentWords) {
        recentWords = state.recentWords
    }

    LaunchedEffect(isTopBarDeleteClicked) {
        if (isTopBarDeleteClicked) {
            isExpanded = true
        }
    }

    if (isExpanded) {
        AlertDialog(
            title = {
                Text(
                    text = "Recent entries",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear all words in Recent?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onDismissRequest = { isExpanded = false },
            dismissButton = {
                TextButton(onClick = { isExpanded = false }) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(DictionaryEvents.OnClearClicked)
                    recentWords = emptyList()
                    isExpanded = false
                }) {
                    Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SearchBar(
                modifier = Modifier.fillMaxWidth(),
                value = state.recentSearchQuery,
                onValueChange = {
                    viewModel.onEvent(DictionaryEvents.OnRecentSearchQueryChange(it))
                },
                onClearClicked = {
                    if (state.recentSearchQuery.isNotEmpty()) {
                        viewModel.onEvent(DictionaryEvents.OnFavoritesSearchQueryChange(""))
                    } else {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                },
                onSearchClicked = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                focusRequester = focusRequester
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (state.isRecentLoading) {
            item {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (state.recentWordsError != null) {
            item {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.recentWordsError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = {
                                viewModel.onEvent(
                                    DictionaryEvents.OnRecentSearchQueryChange("")
                                )
                            }
                        ) {
                            Text(text = "Retry")
                        }
                    }
                }
            }
        } else {
            if (recentWords.isEmpty()) {
                isListEmpty(true)
                item {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.document_clock_timer_icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Recent list is empty",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Search for definitions and they will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                isListEmpty(false)
                items(items = recentWords, key = { it }) { recentWord ->
                    ListCard(
                        word = recentWord,
                        onDelete = { wordToDelete ->
                            recentWords = recentWords.filter { it != recentWord }
                            viewModel.onEvent(DictionaryEvents.OnRecentSwipeToDelete(wordToDelete))
                        },
                        onClick = { onItemClicked(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ListCard(
    modifier: Modifier = Modifier,
    word: String,
    onDelete: (String) -> Unit,
    onClick: (String) -> Unit
) {
    SwipeToDeleteContainer(
        item = word,
        onDelete = { onDelete(it) }
    ) {
        OutlinedCard(
            onClick = { onClick(it) },
            shape = MaterialTheme.shapes.medium,
            modifier = modifier.fillMaxWidth()
        ) {
            ListItem(
                headlineContent = { Text(text = word, fontWeight = FontWeight.Bold) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null
                    )
                }
            )
        }
    }
}