package com.example.dictionary.presentation.shared

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

// Function to handle shared text intents
@Composable
fun handleSharedText(intent: Intent?): String? {
    val word = remember { mutableStateOf<String?>(null) }

    // Check if the activity was launched with a SEND intent and extract the text
    LaunchedEffect(intent) {
        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                word.value = intent.getStringExtra(Intent.EXTRA_TEXT)
            }
        }
    }

    return word.value
}