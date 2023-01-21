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

package com.tomg.githubreleasemonitor.login.business

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.tomg.githubreleasemonitor.login.data.GitHubAuthenticationRepository
import com.tomg.githubreleasemonitor.login.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gitHubAuthenticationRepository: GitHubAuthenticationRepository,
    private val userRepository: UserRepository
) : ViewModel(),
    ContainerHost<LoginState, LoginSideEffect> {

    override val container = container<LoginState, LoginSideEffect>(
        initialState = LoginState(),
        savedStateHandle = savedStateHandle,
        onCreate = {
            checkIfUserIsSignedIn()
        }
    )

    private fun checkIfUserIsSignedIn() = intent {
        val currentUid = gitHubAuthenticationRepository.getCurrentUserUid()
        userRepository.getUser().collect { user ->
            if (user.uid == currentUid) {
                postSideEffect(LoginSideEffect.UserLoggedIn)
            } else {
                reduce {
                    state.copy(isAuthenticating = false)
                }
            }
        }
    }

    fun performSignIn(
        startActivityForSignInWithProvider: (FirebaseAuth, OAuthProvider) -> Task<AuthResult?>
    ) = intent {
        reduce {
            state.copy(isAuthenticating = true)
        }
        var success = false
        if (!gitHubAuthenticationRepository.isAuthPending()) {
            val user = gitHubAuthenticationRepository.performSignInWithProvider(
                startActivityForSignInWithProvider
            )
            if (user != null) {
                success = userRepository.putUser(user)
                if (success) {
                    postSideEffect(LoginSideEffect.UserLoggedIn)
                }
            }
        }
        if (!success) {
            reduce {
                state.copy(isAuthenticating = false)
            }
        }
    }
}
