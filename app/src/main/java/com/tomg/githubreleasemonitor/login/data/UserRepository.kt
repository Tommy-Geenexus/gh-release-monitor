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

package com.tomg.githubreleasemonitor.login.data

import androidx.datastore.core.DataStore
import com.tomg.githubreleasemonitor.Empty
import com.tomg.githubreleasemonitor.UserProto
import com.tomg.githubreleasemonitor.di.DispatcherIo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val dataStore: DataStore<UserProto>,
    @DispatcherIo private val dispatcher: CoroutineDispatcher
) {

    suspend fun deleteUser(): Boolean {
        return withContext(dispatcher) {
            runCatching {
                dataStore.updateData { storedUser ->
                    storedUser.copy(
                        uid = String.Empty,
                        access_token = String.Empty
                    )
                }
                true
            }.getOrElse { exception ->
                Timber.e(exception)
                false
            }
        }
    }

    suspend fun putUser(user: UserProto): Boolean {
        return withContext(dispatcher) {
            runCatching {
                dataStore.updateData { storedUser ->
                    storedUser.copy(
                        uid = user.uid,
                        access_token = user.access_token
                    )
                }
                true
            }.getOrElse { exception ->
                Timber.e(exception)
                false
            }
        }
    }

    suspend fun getUser(): Flow<UserProto> {
        return dataStore
            .data
            .catch { exception ->
                Timber.e(exception)
                emit(UserProto())
            }
    }
}
