package com.example.musicplayercompose.data.repoimpl

import com.example.musicplayercompose.data.datasource.LocalDataSource
import com.example.musicplayercompose.domain.models.Mp3FilesDataClass
import com.example.musicplayercompose.domain.repository.GetMusicLocalRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMusicLocalRepoImpl @Inject constructor(var localSource: LocalDataSource): GetMusicLocalRepo {
    override fun getMp3LocalFiles(): Flow<List<Mp3FilesDataClass>> {
        return localSource.getMp3Files()
    }
}