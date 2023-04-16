/*
 * Copyright (c) 2020-2023, Tom Geiselmann (tomgapplicationsdevelopment@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY,WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tomg.githubreleasemonitor.main.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.accompanist.systemuicontroller.SystemUiController
import com.tomg.githubreleasemonitor.Empty
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.main.SortOrder
import com.tomg.githubreleasemonitor.main.business.AddGitHubRepositoryViewModel
import com.tomg.githubreleasemonitor.main.business.MainSideEffect
import com.tomg.githubreleasemonitor.main.business.MainViewModel
import com.tomg.githubreleasemonitor.main.data.GitHubRepository
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MainScreen(
    systemUiController: SystemUiController,
    mainViewModel: MainViewModel,
    addGitHubRepositoryViewModel: AddGitHubRepositoryViewModel,
    onNavigateToSettings: () -> Unit
) {
    val repositoryAddFailed = stringResource(id = R.string.repository_add_failed)
    val repositoryAdded = stringResource(id = R.string.repository_added)
    val repositoryNotFound = stringResource(id = R.string.repository_not_found)
    val repositoryDeleteFailed = stringResource(id = R.string.repository_delete_failed)
    val repositoryDeleted = stringResource(id = R.string.repository_deleted)
    val repositoryUpdateFailed = stringResource(id = R.string.repository_update_failed)
    val repositoryUpdated = stringResource(id = R.string.repository_updated)
    val repositoryNoUpdate = stringResource(id = R.string.repository_no_update)
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val startActivity = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {}
    )
    mainViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            MainSideEffect.GitHubRepository.Add.Failure -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = repositoryAddFailed)
                }
            }
            MainSideEffect.GitHubRepository.Add.Success -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = repositoryAdded)
                }
            }
            MainSideEffect.GitHubRepository.Add.NotFound -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = repositoryNotFound)
                }
            }
            MainSideEffect.GitHubRepository.Delete.Failure -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = repositoryDeleteFailed)
                }
            }
            MainSideEffect.GitHubRepository.Delete.Success -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = repositoryDeleted)
                }
            }
            MainSideEffect.GitHubRepository.Update.Failure -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = repositoryUpdateFailed)
                }
            }
            MainSideEffect.GitHubRepository.Update.Latest -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = repositoryNoUpdate)
                }
            }
            MainSideEffect.GitHubRepository.Update.Success -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = repositoryUpdated)
                }
            }
            is MainSideEffect.Show -> {
                runCatching {
                    startActivity.launch(Intent(Intent.ACTION_VIEW, Uri.parse(sideEffect.url)))
                }
            }
        }
    }
    val state by mainViewModel.collectAsState()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        )
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME &&
                    !permissionState.status.isGranted &&
                    !permissionState.status.shouldShowRationale
                ) {
                    permissionState.launchPermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        AddGitHubRepositoryDialog(
            viewModel = addGitHubRepositoryViewModel,
            onDismiss = {
                showDialog = false
            },
            onConfirm = { repositoryOwner: String, repositoryName: String ->
                showDialog = false
                mainViewModel.addRepository(repositoryOwner, repositoryName)
            }
        )
    }
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceColorEl2 = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    val surfaceColorEl3 = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
    SideEffect {
        if (state.searchActive) {
            systemUiController.setSystemBarsColor(surfaceColorEl3)
        } else {
            systemUiController.setStatusBarColor(surfaceColor)
            systemUiController.setNavigationBarColor(surfaceColorEl2)
        }
    }
    val gitHubRepositories = mainViewModel.repositoryFlow.collectAsLazyPagingItems()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()
    MainScreen(
        lazyListState = lazyListState,
        snackBarHostState = snackBarHostState,
        gitHubRepositories = gitHubRepositories,
        defaultSortOrder = state.sortOrder,
        isLoading = state.isLoading,
        focusRequester = focusRequester,
        searchActive = state.searchActive,
        searchQuery = state.searchQuery,
        onSearchQueryChange = { query ->
            mainViewModel.updateSearchQuery(query)
        },
        onSearchRequested = {
            focusManager.clearFocus()
            mainViewModel.toggleSearchActive(false)
            gitHubRepositories.refresh()
        },
        onSearchActiveChange = { active ->
            mainViewModel.toggleSearchActive(active)
            if (active) {
                focusRequester.requestFocus()
            } else {
                focusManager.clearFocus()
            }
            gitHubRepositories.refresh()
        },
        onAddGitHubRepository = {
            showDialog = true
        },
        onUserAvatarSelected = { avatarUrl ->
            mainViewModel.showGitHubUserAvatar(avatarUrl)
        },
        onApplySortOrder = { sortOrder ->
            mainViewModel.applySortOrder(sortOrder)
        },
        onShowSettings = onNavigateToSettings,
        onScrollToTop = {
            scope.launch {
                lazyListState.animateScrollToItem(0)
            }
        },
        onReleaseSelected = { releaseUrl ->
            mainViewModel.showGitHubRepositoryRelease(releaseUrl)
        },
        onRefresh = {
            mainViewModel.updateRepositories(gitHubRepositories.itemSnapshotList.items)
        },
        onDelete = { gitHubRepository ->
            mainViewModel.deleteRepository(gitHubRepository)
        }
    )
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    gitHubRepositories: LazyPagingItems<GitHubRepository>? = null,
    defaultSortOrder: SortOrder = SortOrder.Asc.RepositoryOwner,
    isLoading: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    searchActive: Boolean = false,
    searchQuery: String = String.Empty,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchRequested: (String) -> Unit = {},
    onSearchActiveChange: (Boolean) -> Unit = {},
    onAddGitHubRepository: () -> Unit = {},
    onApplySortOrder: (SortOrder) -> Unit = {},
    onShowSettings: () -> Unit = {},
    onScrollToTop: () -> Unit = {},
    onUserAvatarSelected: (String) -> Unit = {},
    onReleaseSelected: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    onDelete: (GitHubRepository) -> Unit = {}
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = onSearchRequested,
                    active = searchActive,
                    onActiveChange = onSearchActiveChange,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(text = stringResource(id = R.string.search_releases))
                    },
                    leadingIcon = {
                        if (searchActive) {
                            IconButton(onClick = { onSearchActiveChange(false) }) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange(String.Empty) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = null
                                )
                            }
                        } else if (!searchActive) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = null
                            )
                        }
                    }
                ) {
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !searchActive,
                enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight / 2 }),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight / 2 })
            ) {
                val canScrollUp by remember {
                    derivedStateOf { lazyListState.firstVisibleItemIndex > 0 }
                }
                BottomBar(
                    isLoading = isLoading,
                    canScrollUp = canScrollUp,
                    defaultSortOrder = defaultSortOrder,
                    onApplySortOrder = onApplySortOrder,
                    onRefresh = onRefresh,
                    onFocusSearch = { onSearchActiveChange(true) },
                    onShowSettings = onShowSettings,
                    onAddGitHubRepository = onAddGitHubRepository,
                    onScrollToTop = onScrollToTop
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
        ) {
            if (gitHubRepositories == null) {
                return@Scaffold
            }
            LazyColumn(state = lazyListState) {
                items(
                    items = gitHubRepositories,
                    key = { gitHubRepository ->
                        gitHubRepository.id
                    }
                ) { gitHubRepository ->
                    if (gitHubRepository != null) {
                        var deleteGitHubRepository by remember { mutableStateOf(false) }
                        LaunchedEffect(deleteGitHubRepository) {
                            if (deleteGitHubRepository) {
                                onDelete(gitHubRepository)
                            }
                        }
                        val dismissState = rememberDismissState(
                            confirmValueChange = { value ->
                                if (value == DismissValue.DismissedToEnd) {
                                    deleteGitHubRepository = !deleteGitHubRepository
                                }
                                value != DismissValue.DismissedToEnd
                            }
                        )
                        val isDismissed = dismissState.isDismissed(DismissDirection.StartToEnd)
                        AnimatedVisibility(visible = !isDismissed) {
                            GitHubRepositoryItem(
                                dismissState = dismissState,
                                gitHubRepository = gitHubRepository,
                                onGitHubUserAvatarSelected = onUserAvatarSelected,
                                onGitHubRepositoryReleaseSelected = onReleaseSelected
                            )
                        }
                    }
                }
                when (gitHubRepositories.loadState.prepend) {
                    is LoadState.NotLoading -> {
                        // Ignore
                    }
                    LoadState.Loading -> item {
                        Spinner(modifier = Modifier.fillMaxSize())
                    }
                    is LoadState.Error -> item {
                        Refresh {
                            gitHubRepositories.retry()
                        }
                    }
                }
                when (gitHubRepositories.loadState.append) {
                    is LoadState.NotLoading -> {
                        // Ignore
                    }
                    LoadState.Loading -> item {
                        Spinner(modifier = Modifier.fillMaxSize())
                    }
                    is LoadState.Error -> item {
                        Refresh {
                            gitHubRepositories.retry()
                        }
                    }
                }
                when (gitHubRepositories.loadState.refresh) {
                    is LoadState.NotLoading -> {
                        // Ignore
                    }
                    LoadState.Loading -> item {
                        Spinner(modifier = Modifier.fillMaxSize())
                    }
                    is LoadState.Error -> item {
                        Refresh {
                            gitHubRepositories.refresh()
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Main Screen")
@Composable
fun MainScreenPreview() {
    MainScreen()
}

@Composable
fun Spinner(
    modifier: Modifier = Modifier,
    progressModifier: Modifier = Modifier.padding(all = 16.dp),
    alignment: Alignment = Alignment.Center
) {
    Box(
        contentAlignment = alignment,
        modifier = modifier
    ) {
        CircularProgressIndicator(modifier = progressModifier)
    }
}

@Composable
fun Refresh(
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    onRefresh: () -> Unit
) {
    Box(
        contentAlignment = alignment,
        modifier = modifier
    ) {
        IconButton(onClick = { onRefresh() }) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null
            )
        }
    }
}

@Composable
fun BottomBar(
    isLoading: Boolean,
    canScrollUp: Boolean,
    defaultSortOrder: SortOrder,
    onApplySortOrder: (SortOrder) -> Unit,
    onRefresh: () -> Unit,
    onFocusSearch: () -> Unit,
    onShowSettings: () -> Unit,
    onAddGitHubRepository: () -> Unit,
    onScrollToTop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        SortOrderDialog(
            defaultSortOrder = defaultSortOrder,
            onDismiss = {
                showDialog = false
            },
            onConfirm = { sortOrder ->
                showDialog = false
                onApplySortOrder(sortOrder)
            }
        )
    }
    Column {
        var showMore by rememberSaveable { mutableStateOf(false) }
        DropdownMenu(
            expanded = showMore,
            onDismissRequest = { showMore = false },
            offset = DpOffset(x = 16.dp, y = 8.dp)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.settings),
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                onClick = {
                    showMore = false
                    onShowSettings()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.refresh),
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                onClick = {
                    showMore = false
                    onRefresh()
                }
            )
        }
        AnimatedVisibility(visible = isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        BottomAppBar(
            actions = {
                IconButton(onClick = { showMore = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = null
                    )
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Sort,
                        contentDescription = null
                    )
                }
                IconButton(onClick = onFocusSearch) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null
                    )
                }
                AnimatedVisibility(
                    visible = canScrollUp,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight / 2 }
                    ),
                    exit = fadeOut() + slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight / 2 }
                    )
                ) {
                    IconButton(onClick = onScrollToTop) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowUpward,
                            contentDescription = null
                        )
                    }
                }
            },
            modifier = modifier.navigationBarsPadding(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        onAddGitHubRepository()
                    },
                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null
                    )
                }
            }
        )
    }
}
