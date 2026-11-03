package com.example

import com.example.audio.AudioSyncManager
import com.example.data.SceneEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testTotalVideoDurationCalculation() {
        val scenes = listOf(
            SceneEntity(id = 1L, estimatedSeconds = 5.0f),
            SceneEntity(id = 2L, estimatedSeconds = 7.5f),
            SceneEntity(id = 3L, estimatedSeconds = 6.0f)
        )
        val totalMs = AudioSyncManager.calculateTotalVideoDurationMs(scenes)
        assertEquals(18500L, totalMs)
    }

    @Test
    fun testCurrentSceneIndexLookup() {
        val scenes = listOf(
            SceneEntity(id = 1L, estimatedSeconds = 5.0f),  // 0ms - 5000ms
            SceneEntity(id = 2L, estimatedSeconds = 7.0f),  // 5000ms - 12000ms
            SceneEntity(id = 3L, estimatedSeconds = 6.0f)   // 12000ms - 18000ms
        )

        assertEquals(0, AudioSyncManager.getCurrentSceneIndex(scenes, 2000L))
        assertEquals(1, AudioSyncManager.getCurrentSceneIndex(scenes, 6000L))
        assertEquals(2, AudioSyncManager.getCurrentSceneIndex(scenes, 15000L))
        assertEquals(2, AudioSyncManager.getCurrentSceneIndex(scenes, 25000L)) // Out of bounds clamped to last
    }

    @Test
    fun testAutoArrangeScenesByWordCount() {
        val scenes = listOf(
            SceneEntity(id = 1L, narration = "Short sentence."), // ~2 words
            SceneEntity(id = 2L, narration = "This is a much longer narrative sentence with several words describing the battle.") // ~13 words
        )

        val totalAudioMs = 15000L // 15 seconds
        val arranged = AudioSyncManager.autoArrangeScenesByWordCount(scenes, totalAudioMs)

        assertEquals(2, arranged.size)
        // Longer sentence gets proportionally more seconds
        assertTrue(arranged[1].estimatedSeconds > arranged[0].estimatedSeconds)
    }
}
