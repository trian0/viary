package com.trian0.viary.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    object Splash : NavigationItem("splash", Icons.Outlined.Navigation, "Splash")
    object Home : NavigationItem("home", Icons.Outlined.Home, "Início")
    object Create : NavigationItem("create", Icons.Outlined.Add, "Criar")
    object Historical : NavigationItem("historical", Icons.Outlined.History, "Histórico")
}