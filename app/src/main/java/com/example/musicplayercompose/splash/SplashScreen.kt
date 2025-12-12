package com.example.musicplayercompose.splash


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.musicplayercompose.R


@Composable
fun SplashScreen (navController: NavController) {


    Box(modifier = Modifier.fillMaxSize().background(Color.Black),){
        Icon(painter = painterResource(R.drawable.musicicon),
            contentDescription = null)
    }

    Button(onClick = {
        navController.navigate("profile")
    }) {
        Text("Precision Granted")
    }
}


@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = NavController(LocalContext.current))
}