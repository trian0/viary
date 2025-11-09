package com.trian0.viary.ui.navigation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.trian0.viary.ui.theme.ViaryOutline
import com.trian0.viary.ui.theme.ViaryPrimary
import com.trian0.viary.ui.theme.ViaryPrimaryContainer

@Composable
fun BottomNavigation(navController: NavController, modifier: Modifier = Modifier) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Create,
        NavigationItem.Historical,
    )

    NavigationBar(
        modifier = modifier
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp)),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelLarge
                    )},
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ViaryPrimary,
                    selectedTextColor = ViaryPrimary,
                    indicatorColor = ViaryPrimaryContainer,
                    unselectedIconColor = ViaryOutline,
                    unselectedTextColor = ViaryOutline
                ),
                alwaysShowLabel = false
            )
        }
    }
}