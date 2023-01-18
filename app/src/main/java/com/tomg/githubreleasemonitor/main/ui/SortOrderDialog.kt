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

package com.tomg.githubreleasemonitor.main.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.main.SortOrder

@Composable
fun SortOrderDialog(
    defaultSortOrder: SortOrder,
    onDismiss: () -> Unit,
    onConfirm: (SortOrder) -> Unit
) {
    var sortOrder by rememberSaveable { mutableStateOf(defaultSortOrder) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(sortOrder)
                }
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.sort_order))
        },
        text = {
            val repoOwner = stringResource(id = R.string.repo_owner)
            val repoName = stringResource(id = R.string.repo_name)
            val releaseDate = stringResource(id = R.string.release_date)
            val sortOrders = listOf(
                repoOwner to SortOrder.Asc.RepositoryOwner,
                repoName to SortOrder.Asc.RepositoryName,
                releaseDate to SortOrder.Asc.RepositoryReleaseDate,
                repoOwner to SortOrder.Desc.RepositoryOwner,
                repoName to SortOrder.Desc.RepositoryName,
                releaseDate to SortOrder.Desc.RepositoryReleaseDate
            )
            val lastSortOrder = sortOrders.last().second
            LazyColumn {
                for ((text, order) in sortOrders) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = sortOrder == order,
                                onClick = {
                                    sortOrder = order
                                },
                                colors = RadioButtonDefaults.colors()
                            )
                            if (order is SortOrder.Asc) {
                                Icon(
                                    imageVector = Icons.Outlined.SortByAlpha,
                                    contentDescription = null,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(
                                        id = R.drawable.ic_sort_by_alpha_reverse
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                            }
                            Text(
                                text = text,
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp
                                )
                            )
                        }
                        if (order != lastSortOrder) {
                            Spacer(modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }
                }
            }
        }
    )
}
