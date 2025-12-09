package com.example.musicplayer.interfaces

import com.example.musicplayer.domain.models.Mp3FilesDataClass

interface BottomMenuClickInterface {
    fun showBottomMenu(Mp3Songs: Mp3FilesDataClass)
}