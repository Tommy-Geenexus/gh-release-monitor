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

package com.tomg.githubreleasemonitor.login.ui

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Login
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.tomg.githubreleasemonitor.CollectInLaunchedEffect
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.login.business.LoginSideEffect
import com.tomg.githubreleasemonitor.login.business.LoginState
import com.tomg.githubreleasemonitor.login.business.LoginViewModel
import com.tomg.githubreleasemonitor.rememberSideEffects
import com.tomg.githubreleasemonitor.rememberState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToMain: () -> Unit
) {
    val sideEffects = rememberSideEffects(viewModel.container.sideEffectFlow)
    CollectInLaunchedEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            LoginSideEffect.UserLoggedIn -> {
                onNavigateToMain()
            }
        }
    }
    val state by rememberState(viewModel.container.stateFlow).collectAsState(initial = LoginState())
    val context = LocalContext.current as Activity
    LoginScreen(
        signInEnabled = !state.isAuthenticating,
        onLoginRequested = {
            viewModel.performSignIn { firebaseAuth: FirebaseAuth, oAuthProvider: OAuthProvider ->
                firebaseAuth.startActivityForSignInWithProvider(context, oAuthProvider)
            }
        }
    )
}

@Composable
fun LoginScreen(
    signInEnabled: Boolean = false,
    onLoginRequested: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            color = MaterialTheme.colors.secondary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.h6
        )
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = null,
            modifier = Modifier
                .padding(all = 16.dp)
                .size(108.dp)
        )
    }
    Row(
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        if (signInEnabled) {
            TextButton(onClick = onLoginRequested) {
                Icon(
                    imageVector = Icons.Outlined.Login,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                )
                Text(
                    text = stringResource(id = R.string.sign_in),
                    modifier = Modifier.padding(all = 8.dp),
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.button
                )
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(24.dp),
                color = MaterialTheme.colors.secondary,
                strokeWidth = 2.dp
            )
        }
    }
}

@Preview(name = "Login Screen")
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
