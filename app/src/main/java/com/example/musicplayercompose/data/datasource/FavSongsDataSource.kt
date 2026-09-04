package com.example.musicplayercompose.data.datasource

import com.example.musicplayercompose.data.datasource.db.FavSongEntity
import com.example.musicplayercompose.data.datasource.db.FavSongsDao
import com.example.musicplayercompose.domain.models.Mp3FilesDataClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavSongsDataSource @Inject constructor(
    private val favDao: FavSongsDao
) {
    fun getAllFavSongs(): Flow<List<Mp3FilesDataClass>> {
        return favDao.getAllFavSongs().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insertFav(favSong: Mp3FilesDataClass) {
        favDao.insertFav(FavSongEntity.fromDomain(favSong))
    }

    suspend fun removeFav(removeSong: Mp3FilesDataClass) {
        favDao.removeFavById(removeSong.id)
    }
}