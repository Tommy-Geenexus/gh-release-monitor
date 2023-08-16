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

internal val monitorIntervalDefaultValue = Duration.ofDays(1).toMillis().toString()

internal val Context.monitorIntervalEntries: Map<String, String>
    get() = mapOf(
        Duration.ofMinutes(15).toMillis().toString() to getString(R.string.minute, 15),
        Duration.ofMinutes(30).toMillis().toString() to getString(R.string.minute, 30),
        Duration.ofHours(1).toMillis().toString() to getString(R.string.hour, 1),
        Duration.ofHours(2).toMillis().toString() to getString(R.string.hour, 2),
        Duration.ofHours(4).toMillis().toString() to getString(R.string.hour, 4),
        Duration.ofHours(8).toMillis().toString() to getString(R.string.hour, 8),
        Duration.ofHours(16).toMillis().toString() to getString(R.string.hour, 16),
        Duration.ofDays(1).toMillis().toString() to getString(R.string.day, 1)
    )
