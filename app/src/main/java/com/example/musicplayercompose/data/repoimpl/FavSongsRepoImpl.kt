package com.example.musicplayer.data.repoimpl

import com.example.musicplayer.data.datasource.FavSongsDataSource
import com.example.musicplayer.domain.models.Mp3FilesDataClass
import com.example.musicplayer.domain.repository.FavSongsRepo
import kotlinx.coroutines.flow.Flow

class FavSongsRepoImpl(val favSource: FavSongsDataSource) : FavSongsRepo {
    override suspend fun getAllFavSongs(): Flow<List<Mp3FilesDataClass>> {
        return favSource.getAllFavSongs()
    }

    override suspend fun insertFav(favSong: Mp3FilesDataClass) {
        favSource.insertFav(favSong)
    }

    override suspend fun removeFav(removeSong: Mp3FilesDataClass) {
        favSource.removeFav(removeSong)
    }
}