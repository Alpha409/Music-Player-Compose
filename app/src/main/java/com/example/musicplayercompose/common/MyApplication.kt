package com.example.musicplayercompose.common

import android.app.Application

/*@HiltAndroidApp*/
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MyApplication
            private set
    }
}