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

package com.tomg.githubreleasemonitor.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tomg.githubreleasemonitor.CollectInLaunchedEffect
import com.tomg.githubreleasemonitor.Empty
import com.tomg.githubreleasemonitor.MIME_TYPE_JSON
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.rememberSideEffects
import com.tomg.githubreleasemonitor.rememberState
import com.tomg.githubreleasemonitor.settings.business.SettingsSideEffect
import com.tomg.githubreleasemonitor.settings.business.SettingsState
import com.tomg.githubreleasemonitor.settings.business.SettingsViewModel
import de.schnettler.datastore.compose.ui.PreferenceScreen
import de.schnettler.datastore.manager.PreferenceRequest
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun SettingsScreen(
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
        contract = ActivityResultContracts.CreateDocument(),
        onResult = { uri ->
            viewModel.exportGitHubRepositories(uri)
        }
    )
    val exportFailed = stringResource(id = R.string.export_failed)
    val exportSuccessful = stringResource(id = R.string.export_successful)
    val importFailed = stringResource(id = R.string.import_failed)
    val importSuccessful = stringResource(id = R.string.import_successful)
    val sideEffects = rememberSideEffects(viewModel.container.sideEffectFlow)
    val signOutFailed = stringResource(id = R.string.sign_out_failed)
    val scaffoldState = rememberScaffoldState()
    CollectInLaunchedEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            SettingsSideEffect.Export.Failure -> {
                scaffoldState.snackbarHostState.showSnackbar(message = exportFailed)
            }
            SettingsSideEffect.Export.Success -> {
                scaffoldState.snackbarHostState.showSnackbar(message = exportSuccessful)
            }
            SettingsSideEffect.Import.Failure -> {
                scaffoldState.snackbarHostState.showSnackbar(message = importFailed)
            }
            SettingsSideEffect.Import.Success -> {
                scaffoldState.snackbarHostState.showSnackbar(message = importSuccessful)
            }
            SettingsSideEffect.UserSignOut.Failure -> {
                scaffoldState.snackbarHostState.showSnackbar(message = signOutFailed)
            }
            SettingsSideEffect.UserSignOut.Success -> {
                onNavigateToLogin()
            }
        }
    }
    CollectInLaunchedEffect(viewModel.getMonitorInterval()) { monitorInterval ->
        viewModel.updateMonitorInterval(monitorInterval)
    }
    val state by rememberState(viewModel.container.stateFlow).collectAsState(
        initial = SettingsState(
            monitorInterval = viewModel.monitorIntervalDefaultValue,
            monitorIntervalEntries = viewModel.monitorIntervalEntries
        )
    )
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
    SettingScreen(
        scaffoldState = scaffoldState,
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

@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun SettingScreen(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
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
        scaffoldState = scaffoldState,
        topBar = {
            SettingsTopBar(onNavigateUp = onNavigateUp)
        }
    ) {
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
            dataStore = dataStore
        )
    }
}

@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Preview(name = "Settings Screen")
@Composable
fun SettingsScreen() {
    SettingScreen()
}

val mockDataStore = object : DataStore<Preferences> {

    override val data: Flow<Preferences> = flowOf()

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences) =
        emptyPreferences()
}

val emptyPreferenceRequest = PreferenceRequest(
    key = stringPreferencesKey(String.Empty),
    defaultValue = String.Empty
)
