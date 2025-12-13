package com.example.musicplayercompose.splash


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.musicplayercompose.R


@Composable
fun SplashScreen (navController: NavController) {


    Column (modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top,){
        Icon(painter = painterResource(R.drawable.musicicon),
            contentDescription = null, modifier = Modifier.fillMaxWidth().padding(top = 22.dp),)
    }


}


@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = NavController(LocalContext.current))
}