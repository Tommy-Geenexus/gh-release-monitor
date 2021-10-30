/*
 * Copyright (c) 2020-2021, Tom Geiselmann (tomgapplicationsdevelopment@gmail.com)
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

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tomg.githubreleasemonitor.CollectInLaunchedEffect
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.main.SortOrder
import com.tomg.githubreleasemonitor.main.business.AddRepositoryViewModel
import com.tomg.githubreleasemonitor.main.business.MainSideEffect
import com.tomg.githubreleasemonitor.main.business.MainState
import com.tomg.githubreleasemonitor.main.business.MainViewModel
import com.tomg.githubreleasemonitor.main.data.GitHubRepository
import com.tomg.githubreleasemonitor.rememberSideEffects
import com.tomg.githubreleasemonitor.rememberState
import timber.log.Timber

@ExperimentalCoilApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun MainScreen(
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
    val sideEffects = rememberSideEffects(mainViewModel.container.sideEffectFlow)
    val scaffoldState = rememberScaffoldState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    val context = LocalContext.current
    CollectInLaunchedEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            MainSideEffect.GitHubRepository.Add.Failure -> {
                scaffoldState.snackbarHostState.showSnackbar(message = repositoryAddFailed)
            }
            MainSideEffect.GitHubRepository.Add.Success -> {
                scaffoldState.snackbarHostState.showSnackbar(message = repositoryAdded)
            }
            MainSideEffect.GitHubRepository.Add.NotFound -> {
                scaffoldState.snackbarHostState.showSnackbar(message = repositoryNotFound)
            }
            MainSideEffect.GitHubRepository.Delete.Failure -> {
                scaffoldState.snackbarHostState.showSnackbar(message = repositoryDeleteFailed)
            }
            MainSideEffect.GitHubRepository.Delete.Success -> {
                scaffoldState.snackbarHostState.showSnackbar(message = repositoryDeleted)
            }
            MainSideEffect.GitHubRepository.Update.Failure -> {
                swipeRefreshState.isRefreshing = false
                scaffoldState.snackbarHostState.showSnackbar(message = repositoryUpdateFailed)
            }
            MainSideEffect.GitHubRepository.Update.Latest -> {
                swipeRefreshState.isRefreshing = false
                scaffoldState.snackbarHostState.showSnackbar(message = repositoryNoUpdate)
            }
            MainSideEffect.GitHubRepository.Update.Success -> {
                swipeRefreshState.isRefreshing = false
                scaffoldState.snackbarHostState.showSnackbar(message = repositoryUpdated)
            }
            is MainSideEffect.Show -> {
                sideEffect.url.toViewIntentOrNull()?.let { intent ->
                    context.startActivitySafe(intent)
                }
            }
        }
    }
    val state by rememberState(mainViewModel.container.stateFlow).collectAsState(
        initial = MainState()
    )
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
    MainScreen(
        scaffoldState = scaffoldState,
        swipeRefreshState = swipeRefreshState,
        gitHubRepositories = gitHubRepositories,
        defaultSortOrder = state.sortOrder,
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
            swipeRefreshState.isRefreshing = true
            mainViewModel.updateRepositories(gitHubRepositories.itemSnapshotList.items)
        },
        onDelete = { gitHubRepository ->
            mainViewModel.deleteRepository(gitHubRepository)
        }
    )
}

@ExperimentalCoilApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun MainScreen(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    swipeRefreshState: SwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false),
    gitHubRepositories: LazyPagingItems<GitHubRepository>? = null,
    defaultSortOrder: SortOrder = SortOrder.Asc.RepositoryOwner,
    onAddGitHubRepository: () -> Unit = {},
    onApplySortOrder: (SortOrder) -> Unit = {},
    onShowSettings: () -> Unit = {},
    onUserAvatarSelected: (String) -> Unit = {},
    onReleaseSelected: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    onDelete: (GitHubRepository) -> Unit = {}
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MainTopBar()
        },
        bottomBar = {
            MainBottomBar(
                defaultSortOrder = defaultSortOrder,
                onApplySortOrder = onApplySortOrder,
                onRefresh = onRefresh,
                onShowSettings = onShowSettings
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAddGitHubRepository()
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSecondary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        isFloatingActionButtonDocked = true
    ) { innerPadding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                swipeRefreshState.isRefreshing = true
                onRefresh()
            },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    contentColor = MaterialTheme.colors.secondary
                )
            }
        ) {
            if (gitHubRepositories == null) {
                return@SwipeRefresh
            }
            LazyColumn(
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                items(items = gitHubRepositories) { gitHubRepository ->
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
                        Spinner(
                            modifier = Modifier.fillMaxSize(),
                            progressModifier = Modifier.padding(all = 16.dp)
                        )
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
                        Spinner(
                            modifier = Modifier.fillMaxSize(),
                            progressModifier = Modifier.padding(all = 16.dp)
                        )
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
                        Spinner(
                            modifier = Modifier.fillMaxSize(),
                            progressModifier = Modifier.padding(all = 16.dp)
                        )
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

@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Preview(name = "Main Screen")
@Composable
fun MainScreenPreview() {
    MainScreen()
}

@Composable
fun Spinner(
    modifier: Modifier = Modifier,
    progressModifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    color: Color = MaterialTheme.colors.secondary,
    strokeWidth: Dp = ProgressIndicatorDefaults.StrokeWidth
) {
    Box(
        contentAlignment = alignment,
        modifier = modifier
    ) {
        CircularProgressIndicator(
            modifier = progressModifier,
            color = color,
            strokeWidth = strokeWidth
        )
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
                contentDescription = null,
                tint = MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.medium)
            )
        }
    }
}

private fun Context.startActivitySafe(intent: Intent): Boolean {
    return runCatching {
        startActivity(intent)
        true
    }.getOrElse { exception ->
        Timber.e(exception)
        false
    }
}

private fun String.toViewIntentOrNull(): Intent? {
    return runCatching {
        Intent(Intent.ACTION_VIEW, Uri.parse(this))
    }.getOrElse { exception ->
        Timber.e(exception)
        null
    }
}
