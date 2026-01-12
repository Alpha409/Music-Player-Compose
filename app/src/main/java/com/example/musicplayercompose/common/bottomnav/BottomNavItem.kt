package com.example.musicplayercompose.common.bottomnav

import com.example.musicplayercompose.R

sealed class BottomNavItem(
    val route: String,
    val icon: Int,
    val label: String
) {
    object MyMusic : BottomNavItem("myMusic", R.drawable.music, "MyMusic")
    object Home : BottomNavItem("home", R.drawable.home, "Home")
    object Favorite : BottomNavItem("fav", R.drawable.fav, "Favorite")
}