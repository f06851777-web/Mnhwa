package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class AppConfig(
    val apiKey: String = "",
    val textModel: String = "gemini-2.5-flash",
    val imageModel: String = "gemini-2.5-flash-image",
    val defaultSceneDuration: Float = 6.0f,
    val resolution: String = "1080p", // "720p", "1080p"
    val fps: Int = 30,
    val isDarkMode: Boolean = true,
    val hasSeenOnboarding: Boolean = false
)

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("manhwavid_settings", Context.MODE_PRIVATE)

    fun getConfig(): AppConfig {
        return AppConfig(
            apiKey = prefs.getString("api_key", "") ?: "",
            textModel = prefs.getString("text_model", "gemini-2.5-flash") ?: "gemini-2.5-flash",
            imageModel = prefs.getString("image_model", "gemini-2.5-flash-image") ?: "gemini-2.5-flash-image",
            defaultSceneDuration = prefs.getFloat("default_scene_duration", 6.0f),
            resolution = prefs.getString("resolution", "1080p") ?: "1080p",
            fps = prefs.getInt("fps", 30),
            isDarkMode = prefs.getBoolean("is_dark_mode", true),
            hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
        )
    }

    fun saveConfig(config: AppConfig) {
        prefs.edit().apply {
            putString("api_key", config.apiKey.trim())
            putString("text_model", config.textModel.trim())
            putString("image_model", config.imageModel.trim())
            putFloat("default_scene_duration", config.defaultSceneDuration)
            putString("resolution", config.resolution)
            putInt("fps", config.fps)
            putBoolean("is_dark_mode", config.isDarkMode)
            putBoolean("has_seen_onboarding", config.hasSeenOnboarding)
            apply()
        }
    }

    fun setHasSeenOnboarding(seen: Boolean) {
        prefs.edit().putBoolean("has_seen_onboarding", seen).apply()
    }
}
