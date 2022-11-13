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

package com.tomg.githubreleasemonitor.settings.business

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.tomg.githubreleasemonitor.login.data.GitHubAuthenticationRepository
import com.tomg.githubreleasemonitor.login.data.UserRepository
import com.tomg.githubreleasemonitor.main.data.GitHubRepositoryRepository
import com.tomg.githubreleasemonitor.monitor.data.GitHubReleaseMonitorRepository
import com.tomg.githubreleasemonitor.settings.data.GitHubRepositoryJsonRepository
import com.tomg.githubreleasemonitor.settings.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gitHubAuthenticationRepository: GitHubAuthenticationRepository,
    private val gitHubReleaseMonitorRepository: GitHubReleaseMonitorRepository,
    private val gitHubRepositoryJsonRepository: GitHubRepositoryJsonRepository,
    private val gitHubRepositoryRepository: GitHubRepositoryRepository,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) : ViewModel(),
    ContainerHost<SettingsState, SettingsSideEffect> {

    override val container = container<SettingsState, SettingsSideEffect>(
        initialState = SettingsState(),
        savedStateHandle = savedStateHandle,
        onCreate = {
            readDefaultValues()
        }
    )

    private fun readDefaultValues() = intent {
        val monitorInterval = settingsRepository.getMonitorInterval().firstOrNull()
        if (monitorInterval != null) {
            reduce {
                state.copy(monitorInterval = monitorInterval)
            }
        }
    }

    fun storeMonitorInterval(monitorInterval: String) = intent {
        val newInterval = state.monitorInterval != monitorInterval
        if (!newInterval) {
            return@intent
        }
        val success = settingsRepository.putMonitorInterval(monitorInterval)
        if (success) {
            reduce {
                state.copy(monitorInterval = monitorInterval)
            }
            val millis = monitorInterval.toLongOrNull()
            if (millis != null) {
                gitHubReleaseMonitorRepository.run {
                    cancelGitHubRepositoryReleaseWork()
                    enqueueGitHubRepositoryReleaseWork(millis)
                }
            }
        }
    }

    fun importGitHubRepositories(uri: Uri?) = intent {
        if (uri == null || uri == Uri.EMPTY) {
            return@intent
        }
        reduce {
            state.copy(loading = true)
        }
        val json = gitHubRepositoryJsonRepository.importFrom(uri)
        val success = if (!json.isNullOrEmpty()) {
            val gitHubRepositories = gitHubRepositoryJsonRepository.fromJson(json)
            if (!gitHubRepositories.isNullOrEmpty()) {
                gitHubRepositoryRepository.insertRepositories(*gitHubRepositories)
            } else {
                false
            }
        } else {
            false
        }
        reduce {
            state.copy(loading = false)
        }
        postSideEffect(
            if (success) {
                SettingsSideEffect.Import.Success
            } else {
                SettingsSideEffect.Import.Failure
            }
        )
    }

    fun exportGitHubRepositories(uri: Uri?) = intent {
        if (uri == null || uri == Uri.EMPTY) {
            return@intent
        }
        reduce {
            state.copy(loading = true)
        }
        gitHubRepositoryRepository.getRepositories().collect { gitHubRepositories ->
            val success = if (gitHubRepositories.isNotEmpty()) {
                val json = gitHubRepositoryJsonRepository.toJson(gitHubRepositories)
                if (!json.isNullOrEmpty()) {
                    gitHubRepositoryJsonRepository.exportTo(uri, json)
                } else {
                    false
                }
            } else {
                false
            }
            if (!success) {
                gitHubRepositoryJsonRepository.deleteDocument(uri)
            }
            reduce {
                state.copy(loading = false)
            }
            postSideEffect(
                if (success) {
                    SettingsSideEffect.Export.Success
                } else {
                    SettingsSideEffect.Export.Failure
                }
            )
        }
    }

    fun performSignOut() = intent {
        reduce {
            state.copy(loading = true)
        }
        var success = gitHubAuthenticationRepository.performSignOut()
        if (success) {
            success = userRepository.deleteUser()
        }
        reduce {
            state.copy(loading = false)
        }
        postSideEffect(
            if (success) {
                SettingsSideEffect.UserSignOut.Success
            } else {
                SettingsSideEffect.UserSignOut.Failure
            }
        )
    }
}
