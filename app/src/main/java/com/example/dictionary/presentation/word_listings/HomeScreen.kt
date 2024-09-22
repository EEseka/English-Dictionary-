package com.example.dictionary.presentation.word_listings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dictionary.data.datastore.WordOfTheDayDataStore
import com.example.dictionary.presentation.DictionaryEvents
import com.example.dictionary.presentation.DictionaryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: DictionaryViewModel = hiltViewModel(),
    scrollBehavior: TopAppBarScrollBehavior,
    sharedWord: String?,
    onSharedWordAvailable: (String) -> Unit,
    onSearchBarClicked: () -> Unit,
    onViewAllClicked: () -> Unit,
    onRecentClicked: (String) -> Unit,
    onWordOfTheDayClicked: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val wordOfTheDayDataStore = WordOfTheDayDataStore(context)
    val wordOfTheDay by wordOfTheDayDataStore.wordOfTheDay.collectAsState(initial = Pair("", ""))

    var isExpanded by remember {
        mutableStateOf(false)
    }
    var recentWords by remember {
        mutableStateOf(emptyList<String>())
    }

    val recentWordSize by remember {
        derivedStateOf { recentWords.size }
    }

    LaunchedEffect(sharedWord) {
        if (sharedWord != null) {
            onSharedWordAvailable(sharedWord)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(DictionaryEvents.OnRecentSearchQueryChange(""))
    }

    LaunchedEffect(state.recentWords) {
        if (state.recentWords.isNotEmpty()) {
            recentWords = state.recentWords
        }
    }

    if (isExpanded) {
        AlertDialog(
            title = {
                Text(
                    text = "Note",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = """The words in the "Word of the Day" feature are randomly generated.""",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onDismissRequest = { isExpanded = false },
            confirmButton = {
                TextButton(onClick = { isExpanded = false }) {
                    Text(text = "OK")
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
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onSearchBarClicked() }
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Search") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    value = "",
                    onValueChange = {},
                    shape = MaterialTheme.shapes.medium,
                    enabled = false
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Word of the Day",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                onClick = {
                    if (wordOfTheDay.first.isNotEmpty()) {
                        onWordOfTheDayClicked(wordOfTheDay.first)
                    } else {
                        onWordOfTheDayClicked("welcome")
                    }
                },
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val todayDate = getCurrentDate()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = todayDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        IconButton(onClick = { isExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    if (wordOfTheDay.first.isNotEmpty() && wordOfTheDay.second.isNotEmpty()) {
                        Text(
                            text = wordOfTheDay.first,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = wordOfTheDay.second,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Welcome",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "The act of greeting someone’s arrival, especially by saying \"Welcome!\"; reception.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            if (state.isRecentLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.recentWordsError != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.recentWordsError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                if (recentWords.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (recentWordSize > 3) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onViewAllClicked() }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        val threeRecentWords = recentWords.take(3)
                        threeRecentWords.forEachIndexed { index, word ->
                            RecentItem(word = word) {
                                onRecentClicked(word)
                            }
                            if (index < threeRecentWords.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentItem(
    modifier: Modifier = Modifier,
    word: String,
    onItemClick: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onItemClick(word) },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListItem(
            headlineContent = { Text(text = word, fontWeight = FontWeight.Bold) },
            trailingContent = {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null
                )
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDate(): String {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return currentDate.format(formatter)
}