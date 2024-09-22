package com.example.dictionary.presentation.word_info

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.example.dictionary.domain.model.WordInfo

@Composable
fun CopyToClipboardButton(
    modifier: Modifier = Modifier,
    wordInfos: List<WordInfo>
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val formattedText = buildString {
        wordInfos.forEachIndexed { index, wordInfo ->
            if (wordInfos.size == 1) {
                append("${wordInfo.word} ${wordInfo.text}")
            } else {
                append("\n${index + 1}. ${wordInfo.word} ${wordInfo.text}")
            }
            wordInfo.meanings.forEachIndexed { meaningIndex, meaning ->
                if (wordInfo.meanings.size == 1) {
                    append("\n${meaning.partOfSpeech}")
                } else {
                    append("\n${meaningIndex + 1}. ${meaning.partOfSpeech}")
                }
                append("\nDefinitions: ")
                meaning.definitions.forEach { definition ->
                    append("\n- ${definition.definition}")
                    if (definition.example.isNotEmpty()) {
                        append("\nExample : ${definition.example}")
                    }
                }
                if (meaning.synonyms.isNotEmpty()) {
                    append("\nSynonyms: ${meaning.synonyms.joinToString(", ")}")
                }
                if (meaning.antonyms.isNotEmpty()) {
                    append("\nAntonyms: ${meaning.antonyms.joinToString(", ")}")
                }
            }
        }
    }

    IconButton(
        onClick = {
            clipboardManager.setText(AnnotatedString(formattedText))
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        },
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary
        ),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy to clipboard"
        )
    }
}