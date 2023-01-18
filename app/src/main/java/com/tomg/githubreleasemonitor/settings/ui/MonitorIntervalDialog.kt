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

package com.tomg.githubreleasemonitor.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.settings.monitorIntervalEntries

@Composable
fun MonitorIntervalDialog(
    defaultMonitorInterval: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var monitorInterval by rememberSaveable { mutableStateOf(defaultMonitorInterval) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(monitorInterval)
                }
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.monitor_interval))
        },
        text = {
            val context = LocalContext.current
            val monitorIntervalEntries = context.monitorIntervalEntries
            LazyColumn {
                for (monitorIntervalEntry in monitorIntervalEntries) {
                    item {
                        Row(
                            modifier = Modifier.clickable {
                                onConfirm(monitorInterval)
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = monitorInterval == monitorIntervalEntry.key,
                                onClick = {
                                    monitorInterval = monitorIntervalEntry.key
                                },
                                colors = RadioButtonDefaults.colors()
                            )
                            Text(
                                text = monitorIntervalEntry.value,
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp
                                )
                            )
                        }
                        if (monitorIntervalEntry != monitorIntervalEntries.entries.lastOrNull()) {
                            Spacer(modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }
                }
            }
        }
    )
}
