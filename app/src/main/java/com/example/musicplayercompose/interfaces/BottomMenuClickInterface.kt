package com.example.musicplayercompose.interfaces

import com.example.musicplayercompose.domain.models.Mp3FilesDataClass

interface BottomMenuClickInterface {
    fun showBottomMenu(Mp3Songs: Mp3FilesDataClass)
}