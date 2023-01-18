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

package com.tomg.githubreleasemonitor.navigation

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.systemuicontroller.SystemUiController
import com.tomg.githubreleasemonitor.login.ui.LoginScreen
import com.tomg.githubreleasemonitor.main.ui.MainScreen
import com.tomg.githubreleasemonitor.settings.ui.SettingsScreen

@Composable
fun NavGraph(
    systemUiController: SystemUiController,
    navController: NavHostController,
    startDestination: String = NavDestinations.ROUTE_LOGIN
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = NavDestinations.ROUTE_LOGIN) {
            LoginScreen(
                systemUiController = systemUiController,
                viewModel = hiltViewModel(),
                onNavigateToMain = {
                    navController.navigate(route = NavDestinations.ROUTE_MAIN) {
                        popUpTo(
                            route = NavDestinations.ROUTE_LOGIN,
                            popUpToBuilder = {
                                inclusive = true
                            }
                        )
                    }
                }
            )
        }
        composable(
            route = NavDestinations.ROUTE_MAIN,
            enterTransition = {
                materialSharedAxisZForward
            },
            exitTransition = {
                materialSharedAxisZBackward
            },
            popEnterTransition = {
                materialSharedAxisZForward
            },
            popExitTransition = {
                materialSharedAxisZBackward
            }
        ) {
            MainScreen(
                systemUiController = systemUiController,
                mainViewModel = hiltViewModel(),
                addRepositoryViewModel = hiltViewModel(),
                onNavigateToSettings = {
                    navController.navigate(route = NavDestinations.ROUTE_SETTINGS)
                }
            )
        }
        composable(
            route = NavDestinations.ROUTE_SETTINGS,
            enterTransition = {
                materialSharedAxisZForward
            },
            exitTransition = {
                materialSharedAxisZBackward
            },
            popEnterTransition = {
                materialSharedAxisZForward
            },
            popExitTransition = {
                materialSharedAxisZBackward
            }
        ) {
            SettingsScreen(
                systemUiController = systemUiController,
                viewModel = hiltViewModel(),
                onNavigateToLogin = {
                    navController.navigate(route = NavDestinations.ROUTE_LOGIN) {
                        popUpTo(
                            route = NavDestinations.ROUTE_MAIN,
                            popUpToBuilder = {
                                inclusive = true
                            }
                        )
                    }
                },
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }
    }
}

private val materialSharedAxisZForward =
    fadeIn(
        animationSpec = tween(
            durationMillis = 210,
            delayMillis = 90,
            easing = LinearOutSlowInEasing
        )
    ) + scaleIn(
        initialScale = 0.8f,
        animationSpec = tween()
    )

private val materialSharedAxisZBackward =
    fadeOut(
        animationSpec = tween(
            durationMillis = 90,
            easing = FastOutLinearInEasing
        )
    ) + scaleOut(
        targetScale = 1.1f,
        animationSpec = tween(easing = FastOutSlowInEasing)
    )
