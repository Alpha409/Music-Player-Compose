package com.example.musicplayercompose.presentation.splash


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.musicplayercompose.R
import ir.kaaveh.sdpcompose.sdp
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(10_000) // 10 seconds
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(R.color.black)),
        verticalArrangement = Arrangement.Top
    ) {
        Icon(
            painter = painterResource(R.drawable.musicicon),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.sdp),
            tint = Color.Unspecified
        )
        Icon(
            painter = painterResource(R.drawable.echo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.sdp),
            tint = Color.Unspecified
        )
        AnimatedPreLoader(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
        )

        Text(
            text = "Music Player",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            color = Color.White
        )
    }
}

@Composable
fun AnimatedPreLoader(modifier: Modifier = Modifier) {
    val preLoaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.loading
        )
    )
    val preLoaderProgress by animateLottieCompositionAsState(
        preLoaderLottieComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )
    LottieAnimation(
        composition = preLoaderLottieComposition,
        progress = preLoaderProgress,
        modifier = modifier,
        alignment = Alignment.Center
    )
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = NavController(LocalContext.current))
}