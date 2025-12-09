package com.example.musicplayercompose.domain.repository

import com.example.musicplayercompose.domain.models.Mp3FilesDataClass
import kotlinx.coroutines.flow.Flow

interface GetMusicLocalRepo {
    fun getMp3LocalFiles(): Flow<List<Mp3FilesDataClass>>
}