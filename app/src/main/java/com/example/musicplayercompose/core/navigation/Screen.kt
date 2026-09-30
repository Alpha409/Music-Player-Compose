package com.example.musicplayercompose.core.navigation

import com.example.musicplayercompose.core.utils.encodeRouteArg

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object Search : Screen("search")
    data object Playlists : Screen("playlists")
    data object Settings : Screen("settings")

    data object AlbumDetail : Screen("album/{id}") {
        fun create(id: Long) = "album/$id"
    }

    data object ArtistDetail : Screen("artist/{name}") {
        fun create(name: String) = "artist/${encodeRouteArg(name)}"
    }

    data object GenreDetail : Screen("genre/{name}") {
        fun create(name: String) = "genre/${encodeRouteArg(name)}"
    }

    data object PlaylistDetail : Screen("playlist/{id}") {
        fun create(id: Long) = "playlist/$id"
    }
}
