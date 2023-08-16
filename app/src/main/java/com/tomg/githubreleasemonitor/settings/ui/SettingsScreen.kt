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

package com.tomg.githubreleasemonitor.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.systemuicontroller.SystemUiController
import com.tomg.githubreleasemonitor.MIME_TYPE_JSON
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.settings.business.SettingsSideEffect
import com.tomg.githubreleasemonitor.settings.business.SettingsViewModel
import com.tomg.githubreleasemonitor.settings.monitorIntervalDefaultValue
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDateTime

@Composable
fun SettingsScreen(
    systemUiController: SystemUiController,
    viewModel: SettingsViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val importGitHubRepositories = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            viewModel.importGitHubRepositories(uri)
        }
    )
    val exportGitHubRepositories = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(MIME_TYPE_JSON),
        onResult = { uri ->
            viewModel.exportGitHubRepositories(uri)
        }
    )
    val useDarkIcons = !isSystemInDarkTheme()
    val surfaceColor = MaterialTheme.colorScheme.surface
    SideEffect {
        systemUiController.setNavigationBarColor(
            color = surfaceColor,
            darkIcons = useDarkIcons,
            navigationBarContrastEnforced = false
        )
    }
    val exportFailed = stringResource(id = R.string.export_failed)
    val exportSuccessful = stringResource(id = R.string.export_successful)
    val importFailed = stringResource(id = R.string.import_failed)
    val importSuccessful = stringResource(id = R.string.import_successful)
    val signOutFailed = stringResource(id = R.string.sign_out_failed)
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            SettingsSideEffect.Export.Failure -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = exportFailed)
                }
            }
            SettingsSideEffect.Export.Success -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = exportSuccessful)
                }
            }
            SettingsSideEffect.Import.Failure -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = importFailed)
                }
            }
            SettingsSideEffect.Import.Success -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = importSuccessful)
                }
            }
            SettingsSideEffect.UserSignOut.Failure -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = signOutFailed)
                }
            }
            SettingsSideEffect.UserSignOut.Success -> {
                onNavigateToLogin()
            }
        }
    }
    val state by viewModel.collectAsState()
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }
    if (showSignOutDialog) {
        SignOutDialog(
            onDismiss = {
                showSignOutDialog = false
            },
            onConfirm = {
                showSignOutDialog = false
                viewModel.performSignOut()
            }
        )
    }
    var showMonitorIntervalDialog by rememberSaveable { mutableStateOf(false) }
    if (showMonitorIntervalDialog) {
        MonitorIntervalDialog(
            defaultMonitorInterval = state.monitorInterval,
            onDismiss = {
                showMonitorIntervalDialog = false
            },
            onConfirm = { monitorInterval ->
                showMonitorIntervalDialog = false
                viewModel.storeMonitorInterval(monitorInterval)
            }
        )
    }
    val displayName = stringResource(R.string.app_name) +
        "_" +
        LocalDateTime.now().toString() +
        ".json"
    SettingScreen(
        snackBarHostState = snackBarHostState,
        monitorInterval = state.monitorInterval,
        isLoading = with(state) {
            isImportingGitHubRepositories || isExportingGitHubRepositories || isSigningOut
        },
        onMonitorIntervalUpdateRequested = {
            showMonitorIntervalDialog = true
        },
        onGitHubRepositoriesImport = {
            importGitHubRepositories.launch(arrayOf(MIME_TYPE_JSON))
        },
        onGitHubRepositoriesExport = {
            exportGitHubRepositories.launch(displayName)
        },
        onUserSignOutRequested = {
            showSignOutDialog = true
        },
        onNavigateUp = onNavigateUp
    )
}

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    monitorInterval: String = monitorIntervalDefaultValue,
    isLoading: Boolean = false,
    onMonitorIntervalUpdateRequested: () -> Unit = {},
    onGitHubRepositoriesImport: () -> Unit = {},
    onGitHubRepositoriesExport: () -> Unit = {},
    onUserSignOutRequested: () -> Unit = {},
    onNavigateUp: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = {
            val insetsPadding = WindowInsets.navigationBars.asPaddingValues()
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.padding(bottom = insetsPadding.calculateBottomPadding())
            )
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
            LazyColumn {
                item {
                    SettingsServiceItem(
                        monitorInterval = monitorInterval,
                        onMonitorIntervalUpdateRequested = onMonitorIntervalUpdateRequested
                    )
                    Divider()
                }
                item {
                    SettingsManageItem(
                        onGitHubRepositoriesImport = onGitHubRepositoriesImport,
                        onGitHubRepositoriesExport = onGitHubRepositoriesExport
                    )
                    Divider()
                }
                item {
                    SettingsAccountItem(onUserSignOutRequested = onUserSignOutRequested)
                }
            }
        }
    }
}

@Preview(name = "Settings Screen")
@Composable
fun SettingsScreen() {
    SettingScreen()
}
