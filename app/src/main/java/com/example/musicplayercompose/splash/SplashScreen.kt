package com.example.musicplayercompose.splash


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.musicplayercompose.R
import ir.kaaveh.sdpcompose.sdp
import ir.kaaveh.sdpcompose.ssp


@Composable
fun SplashScreen(navController: NavController) {


    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
        Icon(
            painter = painterResource(R.drawable.musicicon),
            contentDescription = null, modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.sdp)
        )
        Icon(
            painter = painterResource(R.drawable.echo),
            contentDescription = null, modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.sdp)
        )
        AnimatedPreloader(modifier = Modifier.fillMaxWidth())

        Text(
            text = "Music Player",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.ssp
        )
    }


}

@Composable
fun AnimatedPreloader(modifier: Modifier = Modifier) {
    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.loading
        )
    )

    val preloaderProgress by animateLottieCompositionAsState(
        preloaderLottieComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )


    LottieAnimation(
        composition = preloaderLottieComposition,
        progress = preloaderProgress,
        modifier = modifier
    )


}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = NavController(LocalContext.current))
}