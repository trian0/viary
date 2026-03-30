package com.trian0.viary.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.trian0.viary.MainViewModel
import com.trian0.viary.ui.create.CreateScreen
import com.trian0.viary.ui.historical.HistoricalScreen
import com.trian0.viary.ui.home.HomeScreen
import com.trian0.viary.ui.splash.CustomSplashScreen

@Composable
fun AppNavigation(
    viewModel: MainViewModel = viewModel(),
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = NavigationItem.Splash.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(NavigationItem.Splash.route) {
            CustomSplashScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate(NavigationItem.Home.route) {
                        popUpTo(NavigationItem.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavigationItem.Home.route) {
            HomeScreen()
        }
        composable(NavigationItem.Create.route) {
            CreateScreen(
                onNavigateBack = {
                    navController.navigate(NavigationItem.Home.route) {
                        popUpTo(NavigationItem.Create.route) { inclusive = true }
                    }
                }
            )
        }
        composable(NavigationItem.Historical.route) {
            HistoricalScreen()
        }
    }
}