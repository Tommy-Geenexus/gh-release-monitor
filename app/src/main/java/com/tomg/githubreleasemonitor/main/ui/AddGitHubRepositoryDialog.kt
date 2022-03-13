/*
 * Copyright (c) 2020-2022, Tom Geiselmann (tomgapplicationsdevelopment@gmail.com)
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.main.MAX_CHAR_OWNER
import com.tomg.githubreleasemonitor.main.MAX_CHAR_REPO
import com.tomg.githubreleasemonitor.main.business.AddRepositoryViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun AddGitHubRepositoryDialog(
    viewModel: AddRepositoryViewModel,
    onDismiss: () -> Unit,
    onConfirm: (repositoryOwner: String, repositoryName: String) -> Unit
) {
    val state by viewModel.collectAsState()
    AlertDialog(
        onDismissRequest = {
            onDismiss()
            viewModel.clearRepositoryOwnerAndName()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(state.repositoryOwner, state.repositoryName)
                    viewModel.clearRepositoryOwnerAndName()
                },
                enabled = state.isValidOwner && state.isValidRepositoryName
            ) {
                Text(text = stringResource(id = R.string.add))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    viewModel.clearRepositoryOwnerAndName()
                }
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(id = R.string.add_repo),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = state.repositoryOwner,
                    onValueChange = { value ->
                        viewModel.updateRepositoryOwner(value.trim())
                    },
                    modifier = Modifier.padding(top = 24.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    label = {
                        Text(text = stringResource(id = R.string.git_owner))
                    },
                    isError = !state.isValidOwner,
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary
                    )
                )
                TextFieldBottomIndicator(
                    helperText = stringResource(id = R.string.required),
                    errorText = stringResource(id = R.string.invalid),
                    isError = state.repositoryOwner.isNotEmpty() && !state.isValidOwner,
                    charCnt = state.repositoryOwner.length,
                    maxCharCnt = MAX_CHAR_OWNER
                )
                OutlinedTextField(
                    value = state.repositoryName,
                    onValueChange = { value ->
                        viewModel.updateRepositoryName(value.trim())
                    },
                    modifier = Modifier.padding(top = 24.dp),
                    label = {
                        Text(text = stringResource(id = R.string.git_repo))
                    },
                    isError = !state.isValidRepositoryName,
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary
                    )
                )
                TextFieldBottomIndicator(
                    helperText = stringResource(id = R.string.required),
                    errorText = stringResource(id = R.string.invalid),
                    isError = state.repositoryName.isNotEmpty() &&
                        !state.isValidRepositoryName,
                    charCnt = state.repositoryName.length,
                    maxCharCnt = MAX_CHAR_REPO
                )
            }
        }
    )
}

@Composable
fun TextFieldBottomIndicator(
    helperText: String,
    errorText: String,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    errorTextColor: Color = MaterialTheme.colorScheme.error,
    isError: Boolean = false,
    charCnt: Int = 0,
    maxCharCnt: Int = Integer.MAX_VALUE
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                top = 4.dp
            ),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = if (isError) errorText else helperText,
                color = if (isError) errorTextColor else textColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Row(
            modifier = Modifier.padding(
                end = 16.dp,
                top = 4.dp
            ),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "$charCnt/$maxCharCnt",
                color = textColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
