package com.example.musicplayer.data.repoimpl

import com.example.musicplayer.data.datasource.LocalDataSource
import com.example.musicplayer.domain.models.Mp3FilesDataClass
import com.example.musicplayer.domain.repository.GetMusicLocalRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMusicLocalRepoImpl @Inject constructor(var localSource: LocalDataSource): GetMusicLocalRepo {
    override fun getMp3LocalFiles(): Flow<List<Mp3FilesDataClass>> {
        return localSource.getMp3Files()
    }
}