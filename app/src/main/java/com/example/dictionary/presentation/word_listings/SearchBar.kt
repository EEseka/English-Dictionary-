package com.example.dictionary.presentation.word_listings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onClearClicked: () -> Unit,
    onSearchClicked: () -> Unit,
    focusRequester: FocusRequester
) {
    var isFocused by remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused },
        value = value,
        shape = MaterialTheme.shapes.medium,
        onValueChange = { newValue ->
            onValueChange(newValue)
        },
        leadingIcon = {
            if (!isFocused) {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search")
            }
        },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (isFocused) {
                    IconButton(onClick = onClearClicked) {
                        Icon(imageVector = Icons.Outlined.Clear, contentDescription = "Clear")
                    }
                    IconButton(onClick = { onSearchClicked() }) {
                        Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search")
                    }
                }
            }
        },
        singleLine = true,
        placeholder = {
            Text(text = "Search")
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchClicked() }
        )
    )
}