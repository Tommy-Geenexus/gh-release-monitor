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

package com.tomg.githubreleasemonitor.main.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomAppBar
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tomg.githubreleasemonitor.main.SortOrder

@Composable
fun MainBottomBar(
    defaultSortOrder: SortOrder,
    onApplySortOrder: (SortOrder) -> Unit,
    onRefresh: () -> Unit,
    onShowSettings: () -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        SortOrderDialog(
            defaultSortOrder = defaultSortOrder,
            onDismiss = {
                showDialog = false
            },
            onConfirm = { sortOrder ->
                showDialog = false
                onApplySortOrder(sortOrder)
            }
        )
    }
    BottomAppBar {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sort,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.medium)
                )
            }
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .padding(start = 24.dp)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.medium)
                )
            }
            IconButton(
                onClick = onShowSettings,
                modifier = Modifier
                    .padding(start = 24.dp)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.medium)
                )
            }
        }
    }
}
