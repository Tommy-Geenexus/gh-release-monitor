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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tomg.githubreleasemonitor.di.DispatcherIo
import com.tomg.githubreleasemonitor.settings.monitorIntervalDefaultValue
import com.tomg.githubreleasemonitor.suspendRunCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @DispatcherIo private val dispatcher: CoroutineDispatcher
) {

    private val keyMonitorInterval = stringPreferencesKey("monitor_interval")

    suspend fun getMonitorInterval(): Flow<String> = dataStore
        .data
        .catch { exception ->
            Timber.e(exception)
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[keyMonitorInterval] ?: monitorIntervalDefaultValue
        }
        .flowOn(dispatcher)

    suspend fun putMonitorInterval(monitorInterval: String): Boolean {
        return withContext(dispatcher) {
            coroutineContext.suspendRunCatching {
                dataStore.edit { preferences ->
                    preferences[keyMonitorInterval] = monitorInterval
                }
                true
            }.getOrElse { exception ->
                Timber.e(exception)
                false
            }
        }
    }
}
