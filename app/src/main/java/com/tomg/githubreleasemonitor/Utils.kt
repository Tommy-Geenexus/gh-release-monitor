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

package com.tomg.githubreleasemonitor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

const val TOP_LEVEL_PACKAGE_NAME = "com.tomg.githubreleasemonitor."
const val MIME_TYPE_JSON = "application/json"

val String.Companion.Empty get() = ""

@Composable
fun <S> rememberSideEffects(sideEffectFlow: Flow<S>): Flow<S> {
    val lifecycleOwner = LocalLifecycleOwner.current
    return remember(sideEffectFlow, lifecycleOwner) {
        sideEffectFlow.flowWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            minActiveState = Lifecycle.State.STARTED
        )
    }
}

@Composable
fun <S> rememberState(state: Flow<S>): Flow<S> {
    val lifecycleOwner = LocalLifecycleOwner.current
    return remember(state, lifecycleOwner) {
        state.flowWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            minActiveState = Lifecycle.State.STARTED
        )
    }
}

@Composable
inline fun <S> CollectInLaunchedEffect(
    flow: Flow<S>,
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend (S) -> Unit
) {
    LaunchedEffect(flow) {
        withContext(context) {
            flow.collect { sideEffect ->
                block(sideEffect)
            }
        }
    }
}
