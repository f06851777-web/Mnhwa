package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SceneStatus {
    PENDING,
    GENERATING,
    DONE,
    FAILED
}

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val projectId: Long = 1L,
    val sceneIndex: Int = 0,
    val narration: String = "",
    val visualPrompt: String = "",
    val estimatedSeconds: Float = 6.0f,
    val mood: String = "Epic",
    val status: String = SceneStatus.PENDING.name,
    val imageUri: String? = null,
    val errorMessage: String? = null
)
