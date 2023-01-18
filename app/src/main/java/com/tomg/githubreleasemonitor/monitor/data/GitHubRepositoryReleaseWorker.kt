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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.TOP_LEVEL_PACKAGE_NAME
import com.tomg.githubreleasemonitor.di.DispatcherIo
import com.tomg.githubreleasemonitor.login.data.UserRepository
import com.tomg.githubreleasemonitor.main.data.GitHubRepositoryReleaseRepository
import com.tomg.githubreleasemonitor.main.data.GitHubRepositoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

@HiltWorker
class GitHubRepositoryReleaseWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val gitHubRepositoryReleaseRepository: GitHubRepositoryReleaseRepository,
    private val gitHubRepositoryRepository: GitHubRepositoryRepository,
    private val userRepository: UserRepository,
    @DispatcherIo private val dispatcher: CoroutineDispatcher
) : CoroutineWorker(context, workerParameters) {

    companion object {

        const val TAG = TOP_LEVEL_PACKAGE_NAME + "GitHubRepositoryReleaseWorker"
        private const val CHANNEL_ID = TOP_LEVEL_PACKAGE_NAME + "CHANNEL_ID"
        private const val GRAPHQL_API_COST_MAX = 500000
        private const val GRAPHQL_REQUEST_SIZE_MAX = GRAPHQL_API_COST_MAX / 100
    }

    override suspend fun doWork(): Result {
        return withContext(dispatcher) {
            val accessToken = userRepository.getUser().firstOrNull()?.access_token
            if (accessToken.isNullOrEmpty()) {
                return@withContext Result.failure()
            }
            val gitHubRepositories = gitHubRepositoryRepository
                .getRepositories()
                .firstOrNull()
                ?: return@withContext Result.failure()
            val step = GRAPHQL_REQUEST_SIZE_MAX
            val size = gitHubRepositories.size
            var fromIndex = 0
            var toIndex = minOf(step, size)
            var updateCnt = 0
            while (fromIndex < size) {
                val partition = gitHubRepositories.subList(
                    fromIndex = fromIndex,
                    toIndex = minOf(toIndex, size)
                )
                val results = gitHubRepositoryReleaseRepository.getGitHubRepositoriesOrNull(
                    gitHubRepositories = partition,
                    accessToken = accessToken
                ) ?: return@withContext Result.failure()
                results.forEachIndexed { index, result ->
                    if (result != partition[index]) {
                        updateCnt++
                    }
                }
                val success = gitHubRepositoryRepository.updateRepositories(*results.toTypedArray())
                if (!success) {
                    return@withContext Result.failure()
                }
                val partitionSize = partition.size
                fromIndex += partitionSize
                toIndex += partitionSize
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (updateCnt > 0 && nm.areNotificationsEnabled()) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
                nm.notify(0, createNotification(updateCnt))
            }
            Result.success()
        }
    }

    private fun createNotification(updateCnt: Int): Notification {
        return Notification
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_update)
            .setContentTitle(context.getString(R.string.repository_updates_available))
            .setContentText(context.getString(R.string.repository_updates_count, updateCnt))
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    context.packageManager.getLaunchIntentForPackage(context.packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setAutoCancel(true)
            .build()
    }
}
