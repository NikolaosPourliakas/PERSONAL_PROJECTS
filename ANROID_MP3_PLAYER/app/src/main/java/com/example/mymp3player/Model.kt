package com.example.mymp3player

data class Song(
    val title: String,
    val assetPath: String,
    val folderName: String? = null
)

data class Playlist(
    val name: String,
    val songs: List<Song>
)

