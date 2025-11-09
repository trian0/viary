package com.trian0.viary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trian0.viary.ui.components.Background
import com.trian0.viary.ui.navigation.AppNavigation
import com.trian0.viary.ui.navigation.BottomNavigation
import com.trian0.viary.ui.navigation.NavigationItem

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screensWithoutBottomBar = listOf(
        NavigationItem.Splash.route,
    )

    val showBottomBar = currentRoute !in screensWithoutBottomBar

    Background {
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavigation(
                viewModel = viewModel,
                navController = navController,
                paddingValues = PaddingValues(
                    bottom = if (showBottomBar) 96.dp else 0.dp
                )
            )

            if (showBottomBar) {
                BottomNavigation(
                    navController = navController,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}