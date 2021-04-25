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

package com.tomg.githubreleasemonitor.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tomg.githubreleasemonitor.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
    override val dataStore: DataStore<Preferences>
) : SettingsRepositoryContract {

    override val monitorIntervalKey = stringPreferencesKey("monitor_interval")

    override val monitorIntervalEntries = mapOf(
        15L.minutesToMillisString() to context.getString(R.string.minutes, 15),
        30L.minutesToMillisString() to context.getString(R.string.minutes, 30),
        1L.hoursToMillisString() to context.getString(R.string.hour, 1),
        2L.hoursToMillisString() to context.getString(R.string.hours, 2),
        4L.hoursToMillisString() to context.getString(R.string.hours, 4),
        8L.hoursToMillisString() to context.getString(R.string.hours, 8),
        16L.hoursToMillisString() to context.getString(R.string.hours, 16),
        1L.daysToMillisString() to context.getString(R.string.day, 1),
    )

    override val monitorIntervalDefaultValue = monitorIntervalEntries.entries.last().toPair()

    private fun Long.minutesToMillisString() = Duration.ofMinutes(this).toMillis().toString()

    private fun Long.hoursToMillisString() = Duration.ofHours(this).toMillis().toString()

    private fun Long.daysToMillisString() = Duration.ofDays(this).toMillis().toString()

    override fun getMonitorInterval(): Flow<Pair<String, String>> = dataStore
        .data
        .catch { exception ->
            Timber.e(exception)
            emit(emptyPreferences())
        }
        .map { preferences ->
            monitorIntervalEntries
                .entries
                .find { entry ->
                    entry.key == preferences[monitorIntervalKey]
                }
                ?.toPair()
                ?: monitorIntervalDefaultValue
        }
}
