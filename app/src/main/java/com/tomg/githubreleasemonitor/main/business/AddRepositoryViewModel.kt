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

package com.tomg.githubreleasemonitor.main.business

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.tomg.githubreleasemonitor.Empty
import com.tomg.githubreleasemonitor.main.MAX_CHAR_OWNER
import com.tomg.githubreleasemonitor.main.MAX_CHAR_REPO
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

@HiltViewModel
class AddRepositoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel(),
    ContainerHost<AddRepositoryState, Nothing> {

    override val container = container<AddRepositoryState, Nothing>(
        initialState = AddRepositoryState(),
        savedStateHandle = savedStateHandle
    )

    fun updateRepositoryOwner(owner: String) = intent {
        if (owner.length <= MAX_CHAR_OWNER) {
            reduce {
                state.copy(
                    repositoryOwner = owner,
                    isValidOwner = isValidGitHubRepositoryOwner(owner)
                )
            }
        }
    }

    fun updateRepositoryName(name: String) = intent {
        if (name.length <= MAX_CHAR_REPO) {
            reduce {
                state.copy(
                    repositoryName = name,
                    isValidRepositoryName = isValidGitHubRepositoryName(name)
                )
            }
        }
    }

    fun clearRepositoryOwnerAndName() = intent {
        reduce {
            state.copy(
                repositoryOwner = String.Empty,
                repositoryName = String.Empty,
            )
        }
    }

    private fun isValidGitHubRepositoryOwner(owner: String): Boolean {
        return owner.isNotEmpty() &&
            owner.length <= MAX_CHAR_OWNER &&
            !owner.startsWith('-') &&
            !owner.endsWith('-') &&
            !owner.contains("--") &&
            owner.none { c -> !c.isLetterOrDigit() && c != '-' }
    }

    private fun isValidGitHubRepositoryName(name: String): Boolean {
        return name.isNotEmpty() &&
            name.length <= MAX_CHAR_REPO &&
            name.none { c -> !c.isLetterOrDigit() && c != '-' && c != '_' && c != '.' } &&
            name != "." &&
            name != ".."
    }
}
