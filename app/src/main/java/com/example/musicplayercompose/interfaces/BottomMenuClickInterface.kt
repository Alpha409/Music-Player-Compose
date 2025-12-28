package com.example.musicplayercompose.interfaces

import com.example.musicplayercompose.domain.models.Mp3FilesDataClass

interface BottomMenuClickInterface {
    fun showBottomMenu(mp3Songs: Mp3FilesDataClass)
}