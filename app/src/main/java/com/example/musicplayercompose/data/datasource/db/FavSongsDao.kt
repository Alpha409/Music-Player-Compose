package com.example.musicplayer.data.datasource.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicplayer.domain.models.Mp3FilesDataClass
import kotlinx.coroutines.flow.Flow


@Dao
interface FavSongsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFav(favSong: Mp3FilesDataClass)

    @Query("SELECT * FROM fav_songs")
     fun getAllFavSongs(): Flow<List<Mp3FilesDataClass>>

    @Delete
    suspend fun removeFav(favSong: Mp3FilesDataClass)


}