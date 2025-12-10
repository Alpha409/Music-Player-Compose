package com.example.musicplayercompose.splash

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController


@Composable
fun SplashScreen (navController: NavController) {
    Button(onClick = {
        navController.navigate("profile")
    }) {
        Text("Go to Profile")
    }
}