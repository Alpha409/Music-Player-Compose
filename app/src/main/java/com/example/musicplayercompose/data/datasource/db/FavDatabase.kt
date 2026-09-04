package com.example.musicplayercompose.data.datasource.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavSongEntity::class], version = 4, exportSchema = false)
abstract class FavDatabase : RoomDatabase() {
    abstract fun favDao(): FavSongsDao
}