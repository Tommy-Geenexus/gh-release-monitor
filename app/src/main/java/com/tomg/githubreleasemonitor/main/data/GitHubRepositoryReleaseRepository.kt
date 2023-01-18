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
                    val authorAvatarUrl = (release?.author?.avatarUrl as? String).orEmpty()
                    val authorHtmlUrl = (release?.author?.url as? String).orEmpty()
                    val latestReleaseHtmlUrl = (release?.url as? String).orEmpty()
                    val latestReleaseName = release?.name.orEmpty()
                    val latestReleaseTimestamp = (release?.publishedAt as? String).orEmpty()
                    if (id != null &&
                        authorAvatarUrl.isNotEmpty() &&
                        authorHtmlUrl.isNotEmpty() &&
                        latestReleaseHtmlUrl.isNotEmpty() &&
                        latestReleaseName.isNotEmpty() &&
                        latestReleaseTimestamp.isNotEmpty()
                    ) {
                        return@runCatching GitHubRepository(
                            id = id,
                            owner = repositoryOwner,
                            name = repositoryName,
                            authorAvatarUrl = authorAvatarUrl,
                            authorHtmlUrl = authorHtmlUrl,
                            latestReleaseHtmlUrl = latestReleaseHtmlUrl,
                            latestReleaseName = latestReleaseName,
                            latestReleaseTimestamp = latestReleaseTimestamp
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
                gitHubRepositories.mapIndexed { index: Int, gitHubRepository: GitHubRepository ->
                    val repository = data?.nodes?.get(index)?.onRepository
                    val repositoryOwner = repository?.owner?.login.orEmpty()
                    val repositoryName = repository?.name.orEmpty()
                    val release = repository?.latestRelease
                    val authorAvatarUrl = (release?.author?.avatarUrl as? String).orEmpty()
                    val authorHtmlUrl = (release?.author?.url as? String).orEmpty()
                    val latestReleaseHtmlUrl = (release?.url as? String).orEmpty()
                    val latestReleaseName = release?.name.orEmpty()
                    val latestReleaseTimestamp = (release?.publishedAt as? String).orEmpty()
                    val result = runCatching {
                        ZonedDateTime
                            .parse(latestReleaseTimestamp)
                            .compareTo(ZonedDateTime.parse(gitHubRepository.latestReleaseTimestamp))
                    }.getOrElse { exception ->
                        Timber.e(exception)
                        -1
                    }
                    if (result > 0 &&
                        repositoryOwner.isNotEmpty() &&
                        repositoryName.isNotEmpty() &&
                        authorAvatarUrl.isNotEmpty() &&
                        authorHtmlUrl.isNotEmpty() &&
                        latestReleaseHtmlUrl.isNotEmpty() &&
                        latestReleaseName.isNotEmpty() &&
                        latestReleaseTimestamp.isNotEmpty()
                    ) {
                        gitHubRepository.copy(
                            owner = repositoryOwner,
                            name = repositoryName,
                            authorAvatarUrl = authorAvatarUrl,
                            authorHtmlUrl = authorHtmlUrl,
                            latestReleaseHtmlUrl = latestReleaseHtmlUrl,
                            latestReleaseName = latestReleaseName,
                            latestReleaseTimestamp = latestReleaseTimestamp
                        )
                    } else {
                        gitHubRepository
                    }
                }
            }.getOrElse { exception ->
                Timber.e(exception)
                null
            }
        }
    }
}
