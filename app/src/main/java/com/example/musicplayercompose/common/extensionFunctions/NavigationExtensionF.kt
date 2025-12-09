package com.example.musicplayercompose.common.extensionFunctions

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

/**
 * Prevents multiple rapid clicks from crashing navigation
 */
fun NavHostController.navigateSafely(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    val currentRoute = currentBackStackEntry?.destination?.route
    if (currentRoute != route) {
        navigate(route, builder)
    }
}

/**
 * Navigate and clear whole back stack (like logout → login)
 */
fun NavHostController.navigateAndClearStack(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

/**
 * Safe back navigation
 */
fun NavHostController.safePopBackStack() {
    if (!popBackStack()) {
        // optional: handle when no back stack exists
    }
}

/**
 * Pop back to a specific route
 */
fun NavHostController.popBackTo(
    route: String,
    inclusive: Boolean = false
) {
    popBackStack(route, inclusive)
}

/**
 * Check current destination
 */
fun NavHostController.isAtDestination(route: String): Boolean {
    return currentBackStackEntry?.destination?.route == route
}

/**
 * Navigate only if not already on that screen
 */
fun NavHostController.navigateIfNotAt(route: String) {
    if (!isAtDestination(route)) {
        navigate(route)
    }
}

/**
 * Navigate with full animation control (Compose-style)
 */
fun NavHostController.navigateWithAnim(
    route: String,
    popUpToRoute: String? = null,
    inclusive: Boolean = false
) {
    navigate(route) {
        launchSingleTop = true

        popUpToRoute?.let {
            popUpTo(it) {
                this.inclusive = inclusive
            }
        }
    }
}
