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

package com.tomg.githubreleasemonitor.main.business

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.tomg.githubreleasemonitor.login.data.UserRepository
import com.tomg.githubreleasemonitor.main.SortOrder
import com.tomg.githubreleasemonitor.main.data.GitHubRepository
import com.tomg.githubreleasemonitor.main.data.GitHubRepositoryReleaseRepository
import com.tomg.githubreleasemonitor.main.data.GitHubRepositoryRepository
import com.tomg.githubreleasemonitor.main.pagingSourceFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

@HiltViewModel
class MainViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    pagingConfig: PagingConfig,
    private val gitHubRepositoryReleaseRepository: GitHubRepositoryReleaseRepository,
    private val gitHubRepositoryRepository: GitHubRepositoryRepository,
    private val userRepository: UserRepository,
) : ViewModel(),
    ContainerHost<MainState, MainSideEffect> {

    override val container = container<MainState, MainSideEffect>(
        initialState = MainState(),
        savedStateHandle = savedStateHandle,
    )

    private val pagingSourceFactory by pagingSourceFactory {
        gitHubRepositoryRepository.getRepositories(container.stateFlow.value.sortOrder)
    }
    val repositoryFlow = Pager(
        config = pagingConfig,
        pagingSourceFactory = pagingSourceFactory::create
    ).flow

    fun showGitHubUserAvatar(url: String) = intent {
        postSideEffect(MainSideEffect.Show.GitHubUserAvatar(url = url))
    }

    fun showGitHubRepositoryRelease(url: String) = intent {
        postSideEffect(MainSideEffect.Show.GitHubRepositoryRelease(url = url))
    }

    fun addRepository(
        repositoryOwner: String,
        repositoryName: String
    ) = intent {
        val accessToken = userRepository.getUser().firstOrNull()?.access_token
        val repository = if (accessToken != null && accessToken.isNotEmpty()) {
            gitHubRepositoryReleaseRepository.getGitHubRepositoryOrNull(
                repositoryOwner = repositoryOwner,
                repositoryName = repositoryName,
                accessToken = accessToken
            )
        } else {
            null
        }
        val success = if (repository != null) {
            gitHubRepositoryRepository.insertRepositories(repository)
        } else {
            false
        }
        postSideEffect(
            if (repository != null) {
                if (success) {
                    MainSideEffect.GitHubRepository.Add.Success
                } else {
                    MainSideEffect.GitHubRepository.Add.Failure
                }
            } else {
                MainSideEffect.GitHubRepository.Add.NotFound
            }
        )
    }

    fun deleteRepository(gitHubRepository: GitHubRepository) = intent {
        val success = gitHubRepositoryRepository.deleteRepository(gitHubRepository)
        postSideEffect(
            if (success) {
                MainSideEffect.GitHubRepository.Delete.Success
            } else {
                MainSideEffect.GitHubRepository.Delete.Failure
            }
        )
    }

    fun updateRepositories(gitHubRepositories: List<GitHubRepository>) = intent {
        val accessToken = userRepository.getUser().firstOrNull()?.access_token
        val repositories = if (accessToken != null && accessToken.isNotEmpty()) {
            gitHubRepositoryReleaseRepository.getGitHubRepositoriesOrNull(
                gitHubRepositories = gitHubRepositories,
                accessToken = accessToken
            )
        } else {
            null
        }
        val update = gitHubRepositories != repositories
        val success = if (repositories != null && update) {
            gitHubRepositoryRepository.updateRepositories(*repositories.toTypedArray())
        } else {
            false
        }
        postSideEffect(
            if (update) {
                if (success) {
                    MainSideEffect.GitHubRepository.Update.Success
                } else {
                    MainSideEffect.GitHubRepository.Update.Failure
                }
            } else {
                MainSideEffect.GitHubRepository.Update.Latest
            }
        )
    }

    fun applySortOrder(sortOrder: SortOrder) = intent {
        reduce {
            state.copy(sortOrder = sortOrder)
        }
        pagingSourceFactory.pagingSource.invalidate()
    }
}
