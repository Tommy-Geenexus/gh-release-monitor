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

package com.tomg.githubreleasemonitor.settings.business

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class SettingsSideEffect : Parcelable {

    sealed class Import : SettingsSideEffect() {

        @Parcelize
        object Success : Import()

        @Parcelize
        object Failure : Import()
    }

    sealed class Export : SettingsSideEffect() {

        @Parcelize
        object Success : Export()

        @Parcelize
        object Failure : Export()
    }

    sealed class UserSignOut : SettingsSideEffect() {

        @Parcelize
        object Success : UserSignOut()

        @Parcelize
        object Failure : UserSignOut()
    }
}
