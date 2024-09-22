package com.example.dictionary.presentation.word_info

import android.media.MediaPlayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dictionary.domain.model.Meaning
import com.example.dictionary.presentation.DictionaryEvents
import com.example.dictionary.presentation.DictionaryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WordInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: DictionaryViewModel = hiltViewModel(),
    word: String,
    isConnected: Boolean,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()

    var isLiked by remember {
        mutableStateOf(state.wordInfo.firstOrNull()?.isLiked ?: false)
    }

    var isInitialLoad by remember { mutableStateOf(true) }

    val buttonColorState by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.secondary,
        label = ""
    )

    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.1f else 1f,
        label = ""
    )

    LaunchedEffect(word) {
        viewModel.onEvent(DictionaryEvents.OnSuggestedWordClicked)
    }

    LaunchedEffect(isLiked) {
        if (!isInitialLoad) { // Prevent triggering on initial load
            if (isLiked) {
                viewModel.onEvent(DictionaryEvents.OnLikeClicked(state.wordInfo))
            } else {
                viewModel.onEvent(DictionaryEvents.OnUnlikeClicked(word))
            }
        }
    }

    LaunchedEffect(state.wordInfo) {
        val currentIsLiked = state.wordInfo.firstOrNull()?.isLiked
        if (currentIsLiked != null) {
            isLiked = currentIsLiked
            // Mark the initial load as complete after the first data load
            isInitialLoad = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val wordInfos = state.wordInfo
        if (state.isWordInfoLoading) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.wordInfoError != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!isConnected) {
                        Text(
                            text = "No network connection! Please check your settings.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = state.wordInfoError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        if (state.wordInfoError != "Word meaning not found in the dictionary") {
                            OutlinedButton(
                                onClick = { viewModel.onEvent(DictionaryEvents.OnSuggestedWordClicked) }
                            ) {
                                Text(text = "Retry")
                            }
                        }
                    }
                }
            }
        } else {
            wordInfos.forEach { wordInfo ->
                WordInfoCard(
                    wordName = wordInfo.word,
                    transcription = wordInfo.text,
                    audio = wordInfo.audio,
                    meanings = wordInfo.meanings
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CopyToClipboardButton(wordInfos = wordInfos)
                IconButton(
                    onClick = {
                        if (isLiked) {
                            coroutineScope.launch(Dispatchers.Main) {
                                delay(700L)
                                snackbarHostState.showSnackbar(
                                    message = "Removed from favorites!",
                                    withDismissAction = true
                                )
                            }
                        } else {
                            coroutineScope.launch(Dispatchers.Main) {
                                delay(700L)
                                snackbarHostState.showSnackbar(
                                    message = "Added to favorites!",
                                    withDismissAction = true
                                )
                            }
                        }
                        isLiked = !isLiked
                    },
                    modifier = Modifier.graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    ),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = buttonColorState)
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike" else "Like"
                    )
                }
                ShareButton(wordInfos = wordInfos)
            }
        }
    }
}

@Composable
fun WordInfoCard(
    modifier: Modifier = Modifier,
    wordName: String,
    transcription: String,
    audio: String,
    meanings: List<Meaning>
) {
    val mediaPlayer = remember { MediaPlayer() }
    OutlinedCard(
        onClick = {},
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = wordName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                if (transcription.isNotEmpty()) {
                    Text(
                        text = transcription,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                if (audio.isNotEmpty()) {
                    IconButton(
                        onClick = { playAudio(mediaPlayer, audio) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier
                            .weight(0.5f)
                            .size(48.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Audio"
                        )
                    }
                }
            }
            meanings.forEach { meaning ->
                Spacer(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    text = "‣  ${meaning.partOfSpeech}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (meaning.definitions.size > 1) {
                    meaning.definitions.forEachIndexed { index, definition ->
                        Text(
                            text = "${index + 1}  ${definition.definition}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (definition.example.isNotEmpty()) {
                            Text(
                                text = "Example: ${definition.example}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                } else {
                    Text(
                        text = "◦  ${meaning.definitions[0].definition}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (meaning.definitions[0].example.isNotEmpty()) {
                        Text(
                            text = "Example: ${meaning.definitions[0].example}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                if (meaning.synonyms.isNotEmpty()) {
                    Text(
                        text = "Synonyms: ${meaning.synonyms.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic
                    )
                }
                if (meaning.antonyms.isNotEmpty()) {
                    Text(
                        text = "Antonyms: ${meaning.antonyms.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

private fun playAudio(mediaPlayer: MediaPlayer, audio: String) {
    mediaPlayer.reset()
    mediaPlayer.setDataSource(audio)
    mediaPlayer.prepareAsync()
    mediaPlayer.setOnPreparedListener {
        it.start()
    }
}