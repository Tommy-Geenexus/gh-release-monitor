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

package com.tomg.githubreleasemonitor.monitor.data

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import com.tomg.githubreleasemonitor.di.DispatcherDefault
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubReleaseMonitorRepository @Inject constructor(
    private val workManager: WorkManager,
    @DispatcherDefault private val dispatcher: CoroutineDispatcher
) {

    suspend fun cancelGitHubRepositoryReleaseWork() {
        withContext(dispatcher) {
            workManager.cancelAllWorkByTag(GitHubRepositoryReleaseWorker.TAG).await()
        }
    }

    suspend fun enqueueGitHubRepositoryReleaseWork(millis: Long) {
        withContext(dispatcher) {
            workManager
                .enqueueUniquePeriodicWork(
                    GitHubRepositoryReleaseWorker.TAG,
                    ExistingPeriodicWorkPolicy.KEEP,
                    PeriodicWorkRequestBuilder<GitHubRepositoryReleaseWorker>(
                        Duration.ofMillis(
                            millis
                        )
                    )
                        .addTag(GitHubRepositoryReleaseWorker.TAG)
                        .setConstraints(
                            Constraints
                                .Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                )
                .await()
        }
    }
}
