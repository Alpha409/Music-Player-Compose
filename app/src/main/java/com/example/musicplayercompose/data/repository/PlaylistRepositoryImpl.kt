package com.example.musicplayercompose.data.repository

import com.example.musicplayercompose.data.local.dao.PlaylistDao
import com.example.musicplayercompose.data.local.entity.PlaylistEntity
import com.example.musicplayercompose.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(private val dao: PlaylistDao) : PlaylistRepository {
    override fun observePlaylists() = dao.observePlaylists().map { r -> r.map { it.toDomain() } }
    override fun observePlaylist(id: Long) = dao.observePlaylist(id).map { it?.toDomain() }
    override fun observePlaylistSongs(id: Long) = dao.observeSongs(id).map { r -> r.map { it.toDomain() } }

    override suspend fun createPlaylist(name: String, description: String?): Long {
        val now = System.currentTimeMillis()
        return dao.insert(PlaylistEntity(name = name.trim(), description = description, createdAt = now, updatedAt = now))
    }

    override suspend fun renamePlaylist(id: Long, name: String) =
        dao.rename(id, name.trim(), System.currentTimeMillis())

    override suspend fun deletePlaylist(id: Long) = dao.delete(id)

    override suspend fun addSongs(playlistId: Long, songIds: List<Long>) =
        dao.addSongs(playlistId, songIds, System.currentTimeMillis())

    override suspend fun removeSong(playlistId: Long, songId: Long) =
        dao.removeSong(playlistId, songId, System.currentTimeMillis())

    override suspend fun reorder(playlistId: Long, orderedSongIds: List<Long>) =
        dao.replaceOrder(playlistId, orderedSongIds, System.currentTimeMillis())
}
