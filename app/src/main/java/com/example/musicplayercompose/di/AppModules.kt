package com.example.musicplayercompose.di

import android.content.Context
import androidx.room.Room
import com.example.musicplayercompose.data.local.dao.PlaylistDao
import com.example.musicplayercompose.data.local.dao.SongDao
import com.example.musicplayercompose.data.local.dao.StatsDao
import com.example.musicplayercompose.data.local.database.AppDatabase
import com.example.musicplayercompose.data.repository.MusicRepositoryImpl
import com.example.musicplayercompose.data.repository.PlaybackStateRepositoryImpl
import com.example.musicplayercompose.data.repository.PlaylistRepositoryImpl
import com.example.musicplayercompose.data.repository.SettingsRepositoryImpl
import com.example.musicplayercompose.domain.repository.MusicRepository
import com.example.musicplayercompose.domain.repository.PlaybackStateRepository
import com.example.musicplayercompose.domain.repository.PlaylistRepository
import com.example.musicplayercompose.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        // No destructive fallback: user data (playlists, history) must survive upgrades,
        // so every schema change needs an explicit Migration.
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()

    @Provides fun provideSongDao(db: AppDatabase): SongDao = db.songDao()
    @Provides fun provideStatsDao(db: AppDatabase): StatsDao = db.statsDao()
    @Provides fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun music(impl: MusicRepositoryImpl): MusicRepository
    @Binds abstract fun playlists(impl: PlaylistRepositoryImpl): PlaylistRepository
    @Binds abstract fun settings(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds abstract fun playbackState(impl: PlaybackStateRepositoryImpl): PlaybackStateRepository
}
