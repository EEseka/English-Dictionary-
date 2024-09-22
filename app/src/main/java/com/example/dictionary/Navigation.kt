package com.example.dictionary

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dictionary.presentation.word_info.WordInfoScreen
import com.example.dictionary.presentation.word_listings.FavoriteScreen
import com.example.dictionary.presentation.word_listings.HomeScreen
import com.example.dictionary.presentation.word_listings.RecentSearchScreen
import com.example.dictionary.presentation.word_listings.WordListingsScreen
import com.example.dictionary.util.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    sharedWord: String?,
    isConnected: Boolean
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestinationRoute = currentBackStackEntry?.destination?.route
    val previousBackStackEntry = navController.previousBackStackEntry
    val previousDestinationRoute = previousBackStackEntry?.destination?.route
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isItemsSelected by remember { mutableStateOf(false) }
    var isTopBarDeleteClicked by remember { mutableStateOf(false) }
    var isRecentScreenEmpty by remember { mutableStateOf(false) }

    var receivedSharedWord by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sharedWord) {
        val word = sharedWord?.trim()?.split(" ")?.first()
        receivedSharedWord = word
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val isMainPage =
                currentDestinationRoute == Screen.HomeScreen.route ||
                        currentDestinationRoute == Screen.FavoritesScreen.route
            val title = when (currentDestinationRoute) {
                Screen.HomeScreen.route -> Screen.HomeScreen.title
                Screen.WordListingsScreen.route -> Screen.WordListingsScreen.title
                Screen.FavoritesScreen.route -> Screen.FavoritesScreen.title
                Screen.RecentSearchScreen.route -> Screen.RecentSearchScreen.title
                Screen.WordOfTheDay.route -> Screen.WordOfTheDay.title
                Screen.WordInfoScreen.route -> Screen.WordInfoScreen.title
                else -> ""
            }

            TopBar(
                title = title,
                onBackClicked = {
                    navController.popBackStack()
                },
                isMainPage = isMainPage,
                isRecentScreen = currentDestinationRoute == Screen.RecentSearchScreen.route,
                isItemsSelected = isItemsSelected,
                isRecentScreenEmpty = isRecentScreenEmpty,
                onDeleteClicked = {
                    isTopBarDeleteClicked = true
                    isItemsSelected = false
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            val isHomeSelected =
                currentDestinationRoute == Screen.HomeScreen.route ||
                        (previousDestinationRoute == Screen.HomeScreen.route
                                && currentDestinationRoute != Screen.FavoritesScreen.route) ||
                        (previousDestinationRoute == Screen.WordListingsScreen.route
                                && currentDestinationRoute != Screen.FavoritesScreen.route)
            val isFavoritesSelected =
                currentDestinationRoute == Screen.FavoritesScreen.route ||
                        (previousDestinationRoute == Screen.FavoritesScreen.route
                                && currentDestinationRoute != Screen.HomeScreen.route)

            BottomBar(
                isHomeSelected = isHomeSelected,
                isFavoritesSelected = isFavoritesSelected,
                onHomeClicked = { navController.navigate(Screen.HomeScreen.route) },
                onFavoritesClicked = { navController.navigate(Screen.FavoritesScreen.route) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.HomeScreen.route,
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 8.dp)
        ) {
            composable(Screen.HomeScreen.route) {
                HomeScreen(
                    scrollBehavior = scrollBehavior,
                    sharedWord = receivedSharedWord,
                    onSharedWordAvailable = {
                        navController.navigate(Screen.WordInfoScreen.createRoute(it))
                        receivedSharedWord = null
                    },
                    onSearchBarClicked = {
                        navController.navigate(Screen.WordListingsScreen.route)
                    },
                    onViewAllClicked = {
                        navController.navigate(Screen.RecentSearchScreen.route)
                    },
                    onRecentClicked = { recentWord ->
                        navController.navigate(Screen.WordInfoScreen.createRoute(recentWord))
                    }
                ) { wordOfTheDay ->
                    navController.navigate(Screen.WordOfTheDay.createRoute(wordOfTheDay))
                }
            }
            composable(Screen.WordListingsScreen.route) {
                WordListingsScreen { word ->
                    navController.navigate(Screen.WordInfoScreen.createRoute(word))
                }
            }
            composable(Screen.FavoritesScreen.route) {
                FavoriteScreen(
                    scrollBehavior = scrollBehavior,
                    isTopBarDeleteClicked = isTopBarDeleteClicked,
                    isItemsSelected = { isItemsSelected = it },
                ) { word ->
                    navController.navigate(Screen.WordInfoScreen.createRoute(word))
                }
                // Reset isTopBarDeleteClicked after the delete action
                LaunchedEffect(isTopBarDeleteClicked) {
                    if (isTopBarDeleteClicked) {
                        // Reset the delete click state after the deletion process is triggered
                        isTopBarDeleteClicked = false
                    }
                }
            }
            composable(Screen.RecentSearchScreen.route) {
                RecentSearchScreen(
                    scrollBehavior = scrollBehavior,
                    isTopBarDeleteClicked = isTopBarDeleteClicked,
                    isListEmpty = { isRecentListEmpty ->
                        isRecentScreenEmpty = isRecentListEmpty
                    }
                ) { recentWord ->
                    navController.navigate(Screen.WordInfoScreen.createRoute(recentWord))
                }
                // Reset isRecentScreenEmpty after the delete action
                LaunchedEffect(isRecentScreenEmpty) {
                    if (isRecentScreenEmpty) {
                        isRecentScreenEmpty = false
                    }
                }
                // Reset isTopBarDeleteClicked after the delete action
                LaunchedEffect(isTopBarDeleteClicked) {
                    if (isTopBarDeleteClicked) {
                        // Reset the delete click state after the deletion process is triggered
                        isTopBarDeleteClicked = false
                    }
                }
            }
            composable(Screen.WordOfTheDay.route) {
                val word = it.arguments?.getString("word")
                if (word != null) {
                    WordInfoScreen(
                        word = word,
                        isConnected = isConnected,
                        coroutineScope = coroutineScope,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
            composable(Screen.WordInfoScreen.route) { backStackEntry ->
                val word = backStackEntry.arguments?.getString("word")
                if (word != null) {
                    WordInfoScreen(
                        word = word,
                        isConnected = isConnected,
                        coroutineScope = coroutineScope,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onBackClicked: () -> Unit,
    isMainPage: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    isRecentScreen: Boolean,
    isItemsSelected: Boolean,
    isRecentScreenEmpty: Boolean,
    onDeleteClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isMainPage || isRecentScreen) {
        MediumTopAppBar(
            modifier = modifier,
            title = {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            scrollBehavior = scrollBehavior,
            actions = {
                if (isItemsSelected || (isRecentScreen && !isRecentScreenEmpty)) {
                    IconButton(onClick = { onDeleteClicked() }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        )
    } else {
        TopAppBar(
            modifier = modifier,
            title = {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )
    }
}

@Composable
fun BottomBar(
    isHomeSelected: Boolean,
    isFavoritesSelected: Boolean,
    onHomeClicked: () -> Unit,
    onFavoritesClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(modifier = modifier) {
        NavigationBarItem(
            selected = isHomeSelected,
            onClick = onHomeClicked,
            icon = {
                if (isHomeSelected) {
                    Icon(imageVector = Icons.Filled.Book, contentDescription = "Dictionary")
                } else {
                    Icon(imageVector = Icons.Outlined.Book, contentDescription = "Dictionary")
                }
            },
            label = {
                Text(text = "Dictionary")
            }
        )
        NavigationBarItem(
            selected = isFavoritesSelected,
            onClick = onFavoritesClicked,
            icon = {
                if (isFavoritesSelected) {
                    Icon(imageVector = Icons.Filled.Bookmarks, contentDescription = "Bookmarks")
                } else {
                    Icon(imageVector = Icons.Outlined.Bookmarks, contentDescription = "Bookmarks")
                }
            },
            label = {
                Text(text = "Favorites")
            }
        )
    }
}