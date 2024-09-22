package com.example.dictionary.presentation.word_listings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
fun FavoriteScreen(
    modifier: Modifier = Modifier,
    viewModel: DictionaryViewModel = hiltViewModel(),
    scrollBehavior: TopAppBarScrollBehavior,
    isTopBarDeleteClicked: Boolean,
    isItemsSelected: (Boolean) -> Unit,
    onItemClicked: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Multiselect Logic
    var selectableFavoriteWords by remember { mutableStateOf(emptyList<FavoriteScreenItemsState>()) }
    LaunchedEffect(state.favoriteWords) {
        selectableFavoriteWords = state.favoriteWords.map { word ->
            FavoriteScreenItemsState(favoriteWord = word)
        }
    }

    // Dynamically calculate if any item is selected
    val isAnyItemSelected by remember {
        derivedStateOf { selectableFavoriteWords.any { it.isFavoriteWordSelected } }
    }

    LaunchedEffect(isTopBarDeleteClicked) {
        if (isTopBarDeleteClicked) {
            val wordsToDelete = selectableFavoriteWords.filter { it.isFavoriteWordSelected }
            // Update the UI instantly by removing selected words
            selectableFavoriteWords = selectableFavoriteWords.filter { it !in wordsToDelete }

            // Trigger the delete function in ViewModel only if words are selected
            if (wordsToDelete.isNotEmpty()) {
                viewModel.onEvent(DictionaryEvents.OnSelectToDelete(wordsToDelete.map { it.favoriteWord }))
            }
            isItemsSelected(false) // Reset the icon state
        }
    }

    // End Of Multiselect Logic
    LaunchedEffect(Unit) {
        viewModel.onEvent(DictionaryEvents.OnFavoritesSearchQueryChange(""))
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
                value = state.favoriteSearchQuery,
                onValueChange = {
                    viewModel.onEvent(DictionaryEvents.OnFavoritesSearchQueryChange(it))
                },
                onClearClicked = {
                    if (state.favoriteSearchQuery.isNotEmpty()) {
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
        if (state.isFavoriteLoading) {
            item {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (state.favoriteWordsError != null) {
            item {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.favoriteWordsError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = {
                                viewModel.onEvent(
                                    DictionaryEvents.OnFavoritesSearchQueryChange("")
                                )
                            }
                        ) {
                            Text(text = "Retry")
                        }
                    }
                }
            }
        } else {
            if (selectableFavoriteWords.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.document_heart_love_icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Favorites list is empty",
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
                            text = "Search for definitions and tap on the ♡ icon to show them here.",
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
                items(items = selectableFavoriteWords, key = { it.favoriteWord }) { word ->
                    if (isAnyItemSelected) {
                        isItemsSelected(true)
                        ListCardWithCheck(
                            selectableWord = word,
                            onClick = {
                                // Find the word and update its selection status
                                selectableFavoriteWords = selectableFavoriteWords.map {
                                    if (it.favoriteWord == word.favoriteWord) {
                                        it.copy(isFavoriteWordSelected = !it.isFavoriteWordSelected)
                                    } else it
                                }

                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        isItemsSelected(false)
                        ListCard(
                            selectableWord = word,
                            onDelete = { wordToDelete ->
                                selectableFavoriteWords =
                                    selectableFavoriteWords.filter { it.favoriteWord != word.favoriteWord }
                                viewModel.onEvent(DictionaryEvents.OnSwipeToDelete(wordToDelete))
                            },
                            onClick = { onItemClicked(it) },
                            onLongClick = { selectable ->
                                // Find the word and update its selection status
                                selectableFavoriteWords = selectableFavoriteWords.map {
                                    if (it.favoriteWord == selectable.favoriteWord) {
                                        it.copy(isFavoriteWordSelected = true)
                                    } else it
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListCard(
    modifier: Modifier = Modifier,
    selectableWord: FavoriteScreenItemsState,
    onDelete: (String) -> Unit,
    onClick: (String) -> Unit,
    onLongClick: (FavoriteScreenItemsState) -> Unit
) {
    SwipeToDeleteContainer(
        item = selectableWord.favoriteWord,
        onDelete = { onDelete(it) }
    ) {
        OutlinedCard(
            shape = MaterialTheme.shapes.medium,
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick(it) },
                    onLongClick = { onLongClick(selectableWord) }
                )
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = selectableWord.favoriteWord,
                        fontWeight = FontWeight.Bold
                    )
                },
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

@Composable
fun ListCardWithCheck(
    modifier: Modifier = Modifier,
    selectableWord: FavoriteScreenItemsState,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = { onClick() },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = selectableWord.favoriteWord,
                    fontWeight = FontWeight.Bold
                )
            },
            leadingContent = {
                Checkbox(
                    checked = selectableWord.isFavoriteWordSelected,
                    onCheckedChange = null
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }
        )
    }
}