package com.example.musicplayercompose.presentation.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController


@Composable
fun HomeScreen(navController: NavController) {

    Text(modifier = Modifier.fillMaxSize(),
        text = "Home Screen" )
}