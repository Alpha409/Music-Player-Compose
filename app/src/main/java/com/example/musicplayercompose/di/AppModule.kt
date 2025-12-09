package com.example.musicplayer.di

import android.content.Context
import com.example.musicplayer.data.datasource.FavSongsDataSource
import com.example.musicplayer.data.datasource.LocalDataSource
import com.example.musicplayer.data.repoimpl.FavSongsRepoImpl
import com.example.musicplayer.data.repoimpl.GetMusicLocalRepoImpl
import com.example.musicplayer.domain.repository.FavSongsRepo
import com.example.musicplayer.domain.repository.GetMusicLocalRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context = context

    @Provides
    @Singleton
    fun provideLocalDataSource(
        @ApplicationContext context: Context
    ): LocalDataSource {
        return LocalDataSource(context)
    }

    @Provides
    @Singleton
    fun providesGetMusicRepo(dataSource: LocalDataSource): GetMusicLocalRepo {
        return GetMusicLocalRepoImpl(dataSource)
    }
    @Provides
    @Singleton
    fun providesFavRepo(favSource: FavSongsDataSource): FavSongsRepo {
        return FavSongsRepoImpl(favSource)
    }
}