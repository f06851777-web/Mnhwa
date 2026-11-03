package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 1L,
    val name: String = "Untitled Manhwa Project",
    val script: String = "",
    val aspectRatio: String = "16:9", // "16:9", "9:16", "1:1"
    val artStyle: String = "Manhwa / Korean webtoon",
    val customArtStyle: String = "",
    val characterRefsJson: String = "[]", // List of CharacterRef JSON
    val audioUri: String? = null,
    val audioDurationMs: Long = 0L,
    val bgMusicEnabled: Boolean = false,
    val bgMusicVolume: Float = 0.3f,
    val subtitlesEnabled: Boolean = true,
    val subtitleFontSize: Int = 16,
    val subtitlePosition: String = "bottom", // "bottom", "center", "top"
    val crossfadeDuration: Float = 0.5f,
    val updatedAt: Long = System.currentTimeMillis()
)

data class CharacterRef(
    val id: String,
    val name: String,
    val role: String,
    val imageUri: String? = null
)
