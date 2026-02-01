package com.example.musicplayercompose.data.datasource.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.musicplayercompose.common.utils.BitmapConverters
import com.example.musicplayercompose.domain.models.Mp3FilesDataClass

@Database(entities = [Mp3FilesDataClass::class], version = 3, exportSchema = false)
@TypeConverters(BitmapConverters::class)
abstract class FavDatabase : RoomDatabase() {
    abstract fun favDao(): FavSongsDao
}