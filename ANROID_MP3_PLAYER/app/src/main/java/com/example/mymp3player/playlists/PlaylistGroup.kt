package com.example.mymp3player.playlists

data class PlaylistGroup(
    val name: String,
    val playlistNames: List<String> = emptyList()
)