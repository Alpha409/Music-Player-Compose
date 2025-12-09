package com.example.musicplayer.domain.repository

import com.example.musicplayer.domain.models.Mp3FilesDataClass
import kotlinx.coroutines.flow.Flow

interface FavSongsRepo {

    suspend fun getAllFavSongs(): Flow<List<Mp3FilesDataClass>>

    suspend fun insertFav(favSong: Mp3FilesDataClass)

    suspend fun removeFav(removeSong: Mp3FilesDataClass)
}