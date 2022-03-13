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

package com.tomg.githubreleasemonitor.main.data

import com.tomg.githubreleasemonitor.GitHubRepositoriesQuery
import com.tomg.githubreleasemonitor.GitHubRepositoryQuery
import com.tomg.githubreleasemonitor.di.DispatcherIo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubRepositoryReleaseRepository @Inject constructor(
    @DispatcherIo private val dispatcher: CoroutineDispatcher,
    private val rateLimit: RateLimit
) {

    suspend fun getGitHubRepositoryOrNull(
        repositoryOwner: String,
        repositoryName: String,
        accessToken: String
    ): GitHubRepository? {
        return withContext(dispatcher) {
            runCatching {
                val verified = rateLimit.verifyRateLimit(cost = 1)
                if (!verified) {
                    return@runCatching null
                }
                val response = apolloClient
                    .query(
                        GitHubRepositoryQuery(
                            repositoryOwner = repositoryOwner,
                            repositoryName = repositoryName
                        )
                    )
                    .addHttpHeader("Authorization", "bearer $accessToken")
                    .execute()
                val data = response.data
                val gitHubRateLimit = data?.rateLimit
                if (gitHubRateLimit != null) {
                    rateLimit.set(
                        remaining = gitHubRateLimit.remaining,
                        reset = ZonedDateTime.parse(gitHubRateLimit.resetAt as String)
                    )
                }
                if (!response.hasErrors()) {
                    val id = data?.repository?.id
                    val release = data?.repository?.latestRelease
                    if (id != null && release != null) {
                        return@runCatching GitHubRepository(
                            id = id,
                            owner = repositoryOwner,
                            name = repositoryName,
                            authorAvatarUrl = (release.author?.avatarUrl as? String).orEmpty(),
                            authorHtmlUrl = (release.author?.url as? String).orEmpty(),
                            latestReleaseHtmlUrl = (release.url as? String).orEmpty(),
                            latestReleaseName = release.name.orEmpty(),
                            latestReleaseTimestamp = (release.publishedAt as? String).orEmpty()
                        )
                    }
                }
                null
            }.getOrElse { exception ->
                Timber.e(exception)
                null
            }
        }
    }

    suspend fun getGitHubRepositoriesOrNull(
        gitHubRepositories: List<GitHubRepository>,
        accessToken: String
    ): List<GitHubRepository>? {
        return withContext(dispatcher) {
            runCatching {
                val verified = rateLimit.verifyRateLimit(cost = gitHubRepositories.size)
                if (!verified) {
                    return@runCatching null
                }
                val ids = gitHubRepositories.map { repository ->
                    repository.id
                }
                val response = apolloClient
                    .query(GitHubRepositoriesQuery(ids))
                    .addHttpHeader("Authorization", "bearer $accessToken")
                    .execute()
                val data = response.data
                val gitHubRateLimit = data?.rateLimit
                if (gitHubRateLimit != null) {
                    rateLimit.set(
                        remaining = gitHubRateLimit.remaining,
                        reset = ZonedDateTime.parse(gitHubRateLimit.resetAt as String)
                    )
                }
                if (response.hasErrors()) {
                    return@runCatching null
                }
                gitHubRepositories
                    .mapIndexed { index: Int, gitHubRepository: GitHubRepository ->
                        val latestRelease = data?.nodes?.get(index)?.onRepository?.latestRelease
                        val timestamp = (latestRelease?.publishedAt as? String)
                        val result = timestamp?.compareToTimestamp(
                            timestamp = gitHubRepository.latestReleaseTimestamp
                        )
                        runCatching {
                            ZonedDateTime
                                .parse(timestamp)
                                .compareTo(
                                    ZonedDateTime.parse(gitHubRepository.latestReleaseTimestamp)
                                )
                        }.getOrElse { exception ->
                            Timber.e(exception)
                            null
                        }
                        when {
                            result == 0 -> gitHubRepository
                            result != null && result > 0 -> {
                                gitHubRepository.copy(latestReleaseTimestamp = timestamp)
                            }
                            else -> null
                        }
                    }
                    .filterNotNull()
            }.getOrElse { exception ->
                Timber.e(exception)
                null
            }
        }
    }

    private fun String.compareToTimestamp(timestamp: String): Int? {
        return runCatching {
            ZonedDateTime
                .parse(this)
                .compareTo(ZonedDateTime.parse(timestamp))
        }.getOrElse { exception ->
            Timber.e(exception)
            null
        }
    }
}
