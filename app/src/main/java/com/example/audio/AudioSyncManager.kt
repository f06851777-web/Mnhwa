package com.example.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.SceneEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

object AudioSyncManager {

    /**
     * Inspects audio file duration in milliseconds using MediaMetadataRetriever
     */
    fun getAudioDurationMs(context: Context, uri: Uri): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    /**
     * Automatically distributes scene durations proportionally based on the word count
     * of each scene's narration text to match the total voiceover duration.
     */
    fun autoArrangeScenesByWordCount(
        scenes: List<SceneEntity>,
        totalAudioDurationMs: Long
    ): List<SceneEntity> {
        if (scenes.isEmpty() || totalAudioDurationMs <= 0) return scenes

        val audioDurationSeconds = totalAudioDurationMs / 1000f

        // Calculate word count for each scene
        val wordCounts = scenes.map { scene ->
            val words = scene.narration.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
            max(4, words) // At least 4 words weight
        }
        val totalWords = wordCounts.sum().toFloat()

        return scenes.mapIndexed { index, scene ->
            val proportion = wordCounts[index] / totalWords
            val calculatedSeconds = max(2.5f, proportion * audioDurationSeconds)
            scene.copy(estimatedSeconds = String.format("%.1f", calculatedSeconds).toFloat())
        }
    }

    /**
     * Adjusts all scenes so their combined duration strictly equals the audio duration.
     */
    fun fitToAudio(
        scenes: List<SceneEntity>,
        totalAudioDurationMs: Long
    ): List<SceneEntity> {
        if (scenes.isEmpty() || totalAudioDurationMs <= 0) return scenes
        val targetTotalSec = totalAudioDurationMs / 1000f
        val currentTotalSec = scenes.sumOf { it.estimatedSeconds.toDouble() }.toFloat()

        if (currentTotalSec <= 0f) return scenes
        val scale = targetTotalSec / currentTotalSec

        return scenes.map { scene ->
            val adjusted = max(2.0f, scene.estimatedSeconds * scale)
            scene.copy(estimatedSeconds = String.format("%.1f", adjusted).toFloat())
        }
    }

    /**
     * Finds which scene is currently active at playback timestamp (in milliseconds).
     */
    fun getCurrentSceneIndex(scenes: List<SceneEntity>, positionMs: Long): Int {
        if (scenes.isEmpty()) return 0
        var accumulatedMs = 0L

        for (i in scenes.indices) {
            val sceneDurationMs = (scenes[i].estimatedSeconds * 1000).toLong()
            if (positionMs >= accumulatedMs && positionMs < accumulatedMs + sceneDurationMs) {
                return i
            }
            accumulatedMs += sceneDurationMs
        }
        return scenes.size - 1
    }

    /**
     * Total video duration in milliseconds calculated from all scene durations.
     */
    fun calculateTotalVideoDurationMs(scenes: List<SceneEntity>): Long {
        return (scenes.sumOf { it.estimatedSeconds.toDouble() } * 1000).toLong()
    }
}
