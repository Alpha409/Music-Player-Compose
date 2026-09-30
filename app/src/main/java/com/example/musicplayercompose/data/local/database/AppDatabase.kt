package com.example.musicplayercompose.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.musicplayercompose.data.local.dao.PlaylistDao
import com.example.musicplayercompose.data.local.dao.SongDao
import com.example.musicplayercompose.data.local.dao.StatsDao
import com.example.musicplayercompose.data.local.entity.PlaylistEntity
import com.example.musicplayercompose.data.local.entity.PlaylistSongEntity
import com.example.musicplayercompose.data.local.entity.SongEntity
import com.example.musicplayercompose.data.local.entity.SongStatsEntity

@Database(
    entities = [SongEntity::class, SongStatsEntity::class, PlaylistEntity::class, PlaylistSongEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun statsDao(): StatsDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        const val NAME = "music_player.db"
    }
}
