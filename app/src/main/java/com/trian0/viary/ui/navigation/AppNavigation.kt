package com.trian0.viary.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.trian0.viary.MainViewModel
import com.trian0.viary.ui.checkpoint.CheckpointScreen
import com.trian0.viary.ui.create.CreateScreen
import com.trian0.viary.ui.historical.HistoricalScreen
import com.trian0.viary.ui.home.HomeScreen
import com.trian0.viary.ui.splash.CustomSplashScreen
import com.trian0.viary.ui.viarydetails.ViaryDetailsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(
    viewModel: MainViewModel = koinViewModel(),
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
            HomeScreen(
                onNavigateCheckpoint = {
                    navController.navigate(NavigationItem.Checkpoint.route) {
                        popUpTo(NavigationItem.Home.route) { inclusive = true }
                    }
                },
                onNavigateCreate = {
                    navController.navigate(NavigationItem.Create.route) {
                        popUpTo(NavigationItem.Home.route) { inclusive = true }
                    }
                },
                onNavigateHistorical = {
                    navController.navigate(NavigationItem.Historical.route)
                }
            )
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
            HistoricalScreen(
                onViaryClick = { viaryId ->
                    navController.navigate(NavigationItem.ViaryDetails.route(viaryId))
                }
            )
        }
        composable(
            route = NavigationItem.ViaryDetails.route,
            arguments = listOf(navArgument("viaryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val viaryId = backStackEntry.arguments?.getString("viaryId") ?: return@composable
            ViaryDetailsScreen(
                viaryId = viaryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavigationItem.Checkpoint.route) {
            CheckpointScreen(
                onNavigateBack = {
                    navController.navigate(NavigationItem.Home.route) {
                        popUpTo(NavigationItem.Checkpoint.route) { inclusive = true }
                    }
                }
            )
        }
    }
}