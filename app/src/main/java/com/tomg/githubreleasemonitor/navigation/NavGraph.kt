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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.systemuicontroller.SystemUiController
import com.tomg.githubreleasemonitor.login.ui.LoginScreen
import com.tomg.githubreleasemonitor.main.ui.MainScreen
import com.tomg.githubreleasemonitor.settings.ui.SettingsScreen

private const val TRANSITION_DURATION = 450

@Composable
fun NavGraph(
    systemUiController: SystemUiController,
    navController: NavHostController,
    startDestination: String = NavDestinations.ROUTE_LOGIN
) {
    NavHost(
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
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                )
            }
        ) {
            MainScreen(
                systemUiController = systemUiController,
                mainViewModel = hiltViewModel(),
                addGitHubRepositoryViewModel = hiltViewModel(),
                onNavigateToSettings = {
                    navController.navigate(route = NavDestinations.ROUTE_SETTINGS)
                }
            )
        }
        composable(
            route = NavDestinations.ROUTE_SETTINGS,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                )
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
