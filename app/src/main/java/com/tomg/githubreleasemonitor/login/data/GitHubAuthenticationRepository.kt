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

package com.tomg.githubreleasemonitor.login.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tomg.githubreleasemonitor.UserProto
import com.tomg.githubreleasemonitor.di.DispatcherIo
import com.tomg.githubreleasemonitor.suspendRunCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubAuthenticationRepository @Inject constructor(
    @DispatcherIo private val dispatcher: CoroutineDispatcher
) {

    suspend fun isAuthPending(): Boolean {
        return withContext(dispatcher) {
            coroutineContext.suspendRunCatching {
                Firebase.auth.pendingAuthResult?.await() != null
            }.getOrElse { exception ->
                Timber.e(exception)
                false
            }
        }
    }

    suspend fun performSignInWithProvider(
        startActivityForSignInWithProvider: (FirebaseAuth, OAuthProvider) -> Task<AuthResult?>
    ): UserProto? {
        return withContext(dispatcher) {
            coroutineContext.suspendRunCatching {
                val provider = OAuthProvider
                    .newBuilder("github.com")
                    .build()
                val result = startActivityForSignInWithProvider(Firebase.auth, provider).await()
                if (result != null) {
                    val credential = result.credential
                    if (credential is OAuthCredential) {
                        val accessToken = credential.accessToken
                        if (!accessToken.isNullOrEmpty()) {
                            val user = Firebase.auth.currentUser
                            if (user != null) {
                                return@suspendRunCatching UserProto(
                                    uid = user.uid,
                                    access_token = accessToken
                                )
                            }
                        }
                    }
                }
                null
            }.getOrElse { exception ->
                Timber.e(exception)
                null
            }
        }
    }

    suspend fun performSignOut(): Boolean {
        return withContext(dispatcher) {
            coroutineContext.suspendRunCatching {
                Firebase.auth.signOut()
                true
            }.getOrElse { exception ->
                Timber.e(exception)
                false
            }
        }
    }

    suspend fun getCurrentUserUid(): String? {
        return withContext(dispatcher) {
            coroutineContext.suspendRunCatching {
                Firebase.auth.currentUser?.uid
            }.getOrElse { exception ->
                Timber.e(exception)
                null
            }
        }
    }
}
