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

package com.tomg.githubreleasemonitor.settings.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.squareup.moshi.JsonAdapter
import com.tomg.githubreleasemonitor.di.DispatcherIo
import com.tomg.githubreleasemonitor.main.data.GitHubRepository
import com.tomg.githubreleasemonitor.suspendRunCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubRepositoryJsonRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adapter: JsonAdapter<GitHubRepositoryJson>,
    @DispatcherIo private val dispatcher: CoroutineDispatcher
) {

    suspend fun fromJson(json: String): Array<GitHubRepository>? {
        return withContext(dispatcher) {
            coroutineContext.suspendRunCatching {
                adapter.fromJson(json)?.gitHubRepositories?.toTypedArray()
            }.getOrElse { exception ->
                Timber.e(exception)
                null
            }
        }
    }

    suspend fun toJson(gitHubRepositories: List<GitHubRepository>): String? {
        return withContext(dispatcher) {
            coroutineContext.suspendRunCatching {
                adapter.toJson(GitHubRepositoryJson(gitHubRepositories))
            }.getOrElse { exception ->
                Timber.e(exception)
                null
            }
        }
    }

    suspend fun importFrom(uri: Uri): String? {
        return withContext(dispatcher) {
            coroutineContext.suspendRunCatching {
                context.contentResolver.openInputStream(uri)?.source()?.buffer()?.use { source ->
                    source.readUtf8()
                }
            }.getOrElse { exception ->
                Timber.e(exception)
                null
            }
        }
    }

    suspend fun exportTo(
        uri: Uri,
        payload: String
    ): Boolean {
        return withContext(dispatcher) {
            coroutineContext.suspendRunCatching {
                context.contentResolver.openOutputStream(uri)?.sink()?.buffer()?.use { sink ->
                    sink.writeUtf8(payload)
                }
                true
            }.getOrElse { exception ->
                Timber.e(exception)
                false
            }
        }
    }

    suspend fun deleteDocument(uri: Uri) {
        return withContext(dispatcher) {
            coroutineContext.suspendRunCatching {
                DocumentsContract.deleteDocument(context.contentResolver, uri)
            }.getOrElse { exception ->
                Timber.e(exception)
            }
        }
    }
}
