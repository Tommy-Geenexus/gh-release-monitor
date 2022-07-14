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

package com.tomg.githubreleasemonitor.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.accompanist.systemuicontroller.SystemUiController
import com.tomg.githubreleasemonitor.Empty
import com.tomg.githubreleasemonitor.MIME_TYPE_JSON
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.settings.business.SettingsSideEffect
import com.tomg.githubreleasemonitor.settings.business.SettingsViewModel
import com.tomg.githubreleasemonitor.settings.emptyPreferenceRequest
import com.tomg.githubreleasemonitor.settings.mockDataStore
import de.schnettler.datastore.compose.material3.PreferenceScreen
import de.schnettler.datastore.manager.PreferenceRequest
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
    val monitorIntervalFlow = viewModel.getMonitorInterval()
    LaunchedEffect(monitorIntervalFlow) {
        monitorIntervalFlow.collect { monitorInterval ->
            viewModel.updateMonitorInterval(monitorInterval)
        }
    }
    val state by viewModel.collectAsState()
    val displayName = stringResource(R.string.app_name) +
        "_" +
        LocalDateTime.now().toString() +
        ".json"
    var showDialog by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        SignOutDialog(
            onDismiss = {
                showDialog = false
            },
            onConfirm = {
                showDialog = false
                viewModel.performSignOut()
            }
        )
    }
    val useDarkIcons = !isSystemInDarkTheme()
    val surfaceColor = MaterialTheme.colorScheme.surface
    SideEffect {
        systemUiController.setNavigationBarColor(
            color = surfaceColor,
            darkIcons = useDarkIcons,
            navigationBarContrastEnforced = false
        )
    }
    SettingScreen(
        snackBarHostState = snackBarHostState,
        dataStore = viewModel.dataStore,
        monitorIntervalPreferenceRequest = PreferenceRequest(
            key = viewModel.monitorIntervalKey,
            defaultValue = viewModel.monitorIntervalDefaultValue.first
        ),
        monitorIntervalEntries = state.monitorIntervalEntries,
        monitorIntervalDisplayName = state.monitorInterval.second,
        onGitHubRepositoriesImport = {
            importGitHubRepositories.launch(arrayOf(MIME_TYPE_JSON))
        },
        onGitHubRepositoriesExport = {
            exportGitHubRepositories.launch(displayName)
        },
        onUserSignOutRequested = {
            showDialog = true
        },
        onNavigateUp = onNavigateUp
    )
}

@Composable
fun SettingScreen(
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    dataStore: DataStore<Preferences> = mockDataStore,
    monitorIntervalPreferenceRequest: PreferenceRequest<String> = emptyPreferenceRequest,
    monitorIntervalEntries: Map<String, String> = mapOf(),
    monitorIntervalDisplayName: String = String.Empty,
    onGitHubRepositoriesImport: () -> Unit = {},
    onGitHubRepositoriesExport: () -> Unit = {},
    onUserSignOutRequested: () -> Unit = {},
    onNavigateUp: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            SmallTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = null
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
        PreferenceScreen(
            items = listOf(
                settingsServiceItem(
                    monitorIntervalPreferenceRequest = monitorIntervalPreferenceRequest,
                    monitorIntervalEntries = monitorIntervalEntries,
                    monitorIntervalDisplayName = monitorIntervalDisplayName
                ),
                settingsManageItem(
                    onGitHubRepositoriesImport = onGitHubRepositoriesImport,
                    onGitHubRepositoriesExport = onGitHubRepositoriesExport
                ),
                settingsAccountItem(onUserSignOutRequested = onUserSignOutRequested)
            ),
            dataStore = dataStore,
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
        )
    }
}

@Preview(name = "Settings Screen")
@Composable
fun SettingsScreen() {
    SettingScreen()
}
