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

package com.tomg.githubreleasemonitor.main.business

import android.os.Parcelable
import com.tomg.githubreleasemonitor.Empty
import kotlinx.parcelize.Parcelize

sealed class MainSideEffect : Parcelable {

    sealed class Show(
        open val url: String = String.Empty
    ) : MainSideEffect() {

        @Parcelize
        data class GitHubUserAvatar(override val url: String) : Show()

        @Parcelize
        data class GitHubRepositoryRelease(override val url: String) : Show()
    }

    sealed class GitHubRepository : MainSideEffect() {

        sealed class Add : GitHubRepository() {

            @Parcelize
            data object Success : Add()

            @Parcelize
            data object Failure : Add()

            @Parcelize
            data object NotFound : Add()
        }

        sealed class Delete : GitHubRepository() {

            @Parcelize
            data object Success : Delete()

            @Parcelize
            data object Failure : Delete()
        }

        sealed class Update : GitHubRepository() {

            @Parcelize
            data object Success : Update()

            @Parcelize
            data object Failure : Update()

            @Parcelize
            data object Latest : Update()
        }
    }
}
