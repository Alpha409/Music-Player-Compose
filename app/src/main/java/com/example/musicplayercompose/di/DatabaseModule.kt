package com.example.musicplayercompose.di

import android.content.Context
import androidx.room.Room
import com.example.musicplayercompose.data.datasource.db.FavDatabase
import com.example.musicplayercompose.data.datasource.db.FavSongsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideFavDatabase(
        @ApplicationContext context: Context
    ): FavDatabase {
        return Room.databaseBuilder(
            context.applicationContext, FavDatabase::class.java, "my_fav_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideFavDao(favDb: FavDatabase): FavSongsDao = favDb.favDao()
}