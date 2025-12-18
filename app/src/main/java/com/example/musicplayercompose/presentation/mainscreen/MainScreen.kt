package com.example.musicplayercompose.presentation.mainscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.musicplayercompose.common.bottomnav.BottomNavItem



val bottomBarRoutes = listOf(
    BottomNavItem.Home.route,
    BottomNavItem.MyMusic.route,
    BottomNavItem.Favorite.route
)

@Composable
fun MainScreen(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomBar(navController)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavGraph(navController)
        }
    }
}

@Composable
fun BottomBar(navController: NavController) {

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.MyMusic,
        BottomNavItem.Favorite
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
