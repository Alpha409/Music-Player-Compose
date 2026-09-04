package com.example.musicplayercompose.data.datasource.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavSongsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFav(favSong: FavSongEntity)

    @Query("SELECT * FROM fav_songs ORDER BY title ASC")
    fun getAllFavSongs(): Flow<List<FavSongEntity>>

    @Delete
    suspend fun removeFav(favSong: FavSongEntity)

    @Query("DELETE FROM fav_songs WHERE id = :id")
    suspend fun removeFavById(id: Long)
}