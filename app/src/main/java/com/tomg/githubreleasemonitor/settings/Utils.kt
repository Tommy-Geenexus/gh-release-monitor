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

package com.tomg.githubreleasemonitor.settings

import android.content.Context
import com.tomg.githubreleasemonitor.R
import java.time.Duration

internal val monitorIntervalDefaultValue = 1L.daysToMillisString()

internal val Context.monitorIntervalEntries: Map<String, String>
    get() = mapOf(
        15L.minutesToMillisString() to getString(R.string.minutes, 15),
        30L.minutesToMillisString() to getString(R.string.minutes, 30),
        1L.hoursToMillisString() to getString(R.string.hour, 1),
        2L.hoursToMillisString() to getString(R.string.hours, 2),
        4L.hoursToMillisString() to getString(R.string.hours, 4),
        8L.hoursToMillisString() to getString(R.string.hours, 8),
        16L.hoursToMillisString() to getString(R.string.hours, 16),
        1L.daysToMillisString() to getString(R.string.day, 1)
    )

internal fun Long.minutesToMillisString() = Duration.ofMinutes(this).toMillis().toString()

internal fun Long.hoursToMillisString() = Duration.ofHours(this).toMillis().toString()

internal fun Long.daysToMillisString() = Duration.ofDays(this).toMillis().toString()
