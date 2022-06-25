/*
 * Copyright (c) 2020-2022, Tom Geiselmann (tomgapplicationsdevelopment@gmail.com)
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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.systemuicontroller.SystemUiController
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.main.SortOrder
import com.tomg.githubreleasemonitor.main.business.AddRepositoryViewModel
import com.tomg.githubreleasemonitor.main.business.MainSideEffect
import com.tomg.githubreleasemonitor.main.business.MainViewModel
import com.tomg.githubreleasemonitor.main.data.GitHubRepository
import com.tomg.githubreleasemonitor.main.startActivitySafe
import com.tomg.githubreleasemonitor.main.toViewIntentOrNull
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlin.math.ln

@Composable
fun MainScreen(
    systemUiController: SystemUiController,
    mainViewModel: MainViewModel,
    addRepositoryViewModel: AddRepositoryViewModel,
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
    val context = LocalContext.current
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
                sideEffect.url.toViewIntentOrNull()?.let { intent ->
                    context.startActivitySafe(intent)
                }
            }
        }
    }
    val state by mainViewModel.collectAsState()
    val gitHubRepositories = mainViewModel.repositoryFlow.collectAsLazyPagingItems()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        AddGitHubRepositoryDialog(
            viewModel = addRepositoryViewModel,
            onDismiss = {
                showDialog = false
            },
            onConfirm = { repositoryOwner: String, repositoryName: String ->
                showDialog = false
                mainViewModel.addRepository(repositoryOwner, repositoryName)
            }
        )
    }
    val surfaceColorEl2 = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    SideEffect {
        systemUiController.setNavigationBarColor(surfaceColorEl2)
    }
    MainScreen(
        snackBarHostState = snackBarHostState,
        gitHubRepositories = gitHubRepositories,
        defaultSortOrder = state.sortOrder,
        isLoading = state.isLoading,
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
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    gitHubRepositories: LazyPagingItems<GitHubRepository>? = null,
    defaultSortOrder: SortOrder = SortOrder.Asc.RepositoryOwner,
    isLoading: Boolean = false,
    onAddGitHubRepository: () -> Unit = {},
    onApplySortOrder: (SortOrder) -> Unit = {},
    onShowSettings: () -> Unit = {},
    onUserAvatarSelected: (String) -> Unit = {},
    onReleaseSelected: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    onDelete: (GitHubRepository) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .displayCutoutPadding(),
        topBar = {
            SmallTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                }
            )
        },
        bottomBar = {
            BottomBar(
                defaultSortOrder = defaultSortOrder,
                onApplySortOrder = onApplySortOrder,
                onRefresh = onRefresh,
                onShowSettings = onShowSettings,
                onAddGitHubRepository = onAddGitHubRepository
            )
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
            AnimatedVisibility(visible = isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (gitHubRepositories == null) {
                return@Scaffold
            }
            LazyColumn {
                items(
                    items = gitHubRepositories,
                    key = { gitHubRepository ->
                        gitHubRepository.id
                    }
                ) { gitHubRepository ->
                    if (gitHubRepository != null) {
                        GitHubRepositoryItem(
                            gitHubRepository = gitHubRepository,
                            onGitHubUserAvatarSelected = onUserAvatarSelected,
                            onGitHubRepositoryReleaseSelected = onReleaseSelected,
                            onDeleteGitHubRepository = onDelete
                        )
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
    defaultSortOrder: SortOrder,
    onApplySortOrder: (SortOrder) -> Unit,
    onRefresh: () -> Unit,
    onShowSettings: () -> Unit,
    onAddGitHubRepository: () -> Unit,
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
    BottomAppBar(
        icons = {
            IconButton(
                onClick = {
                    showDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sort,
                    contentDescription = null
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null
                )
            }
            IconButton(onClick = onShowSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null
                )
            }
        },
        modifier = Modifier.navigationBarsPadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAddGitHubRepository()
                },
                elevation = BottomAppBarDefaults.floatingActionButtonElevation()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null
                )
            }
        }
    )
}

private fun ColorScheme.surfaceColorAtElevation(
    elevation: Dp,
): Color {
    if (elevation == 0.dp) return surface
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return surfaceTint.copy(alpha = alpha).compositeOver(surface)
}
