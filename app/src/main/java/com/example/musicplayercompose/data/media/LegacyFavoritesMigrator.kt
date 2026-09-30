package com.example.musicplayercompose.data.media

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.musicplayercompose.data.local.dao.StatsDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** One-time import of favourites from the pre-rewrite `my_fav_db` database, then deletes it. */
@Singleton
class LegacyFavoritesMigrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stats: StatsDao
) {
    suspend fun migrateIfNeeded() {
        val file = context.getDatabasePath(LEGACY_DB)
        if (!file.exists()) return
        runCatching {
            val ids = mutableListOf<Long>()
            SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                db.rawQuery("SELECT id FROM fav_songs", null).use { c ->
                    while (c.moveToNext()) ids += c.getLong(0)
                }
            }
            ids.forEach { stats.favorite(it) }
        }
        context.deleteDatabase(LEGACY_DB)
    }

    private companion object {
        const val LEGACY_DB = "my_fav_db"
    }
}
