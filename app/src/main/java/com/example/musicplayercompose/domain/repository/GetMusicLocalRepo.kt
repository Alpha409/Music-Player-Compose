package com.example.musicplayer.domain.repository

import com.example.musicplayer.domain.models.Mp3FilesDataClass
import kotlinx.coroutines.flow.Flow

interface GetMusicLocalRepo {
    fun getMp3LocalFiles(): Flow<List<Mp3FilesDataClass>>
}