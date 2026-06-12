package com.trian0.viary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trian0.viary.ui.components.RequestLocationPermission
import com.trian0.viary.ui.navigation.AppNavigation
import com.trian0.viary.ui.navigation.BottomNavigation
import com.trian0.viary.ui.navigation.NavigationItem
import com.trian0.viary.ui.theme.Tertiary90

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screensWithoutBottomBar = listOf(
        NavigationItem.Splash.route,
        NavigationItem.ViaryDetails.route,
    )

    RequestLocationPermission()

    val showBottomBar = currentRoute !in screensWithoutBottomBar
    Scaffold(contentWindowInsets = WindowInsets.ime) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Tertiary90.copy(alpha = 0.3f))
                .graphicsLayer { clip = false }
        ) {
            AppNavigation(
                viewModel = viewModel,
                navController = navController,
                paddingValues = PaddingValues(
                    bottom = if (showBottomBar) 96.dp else 0.dp
                )
            )

            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 0.dp)
                        .graphicsLayer { clip = false }
                ) {
                    BottomNavigation(
                        navController = navController,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}