package com.example.ui

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiService
import com.example.audio.AudioSyncManager
import com.example.data.AppConfig
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.CharacterRef
import com.example.data.ProjectEntity
import com.example.data.SceneEntity
import com.example.data.SceneStatus
import com.example.util.ImageStorageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.max

enum class AppStep(val title: String, val stepNumber: Int) {
    SETUP("Project Setup", 1),
    SCRIPT("Script Input", 2),
    SCENES("Scene Board", 3),
    VOICEOVER("Voiceover Sync", 4),
    PREVIEW("Preview & Edit", 5),
    EXPORT("Export", 6)
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val projectDao = db.projectDao()
    private val sceneDao = db.sceneDao()
    private val appSettings = AppSettings(application)
    private val geminiService = GeminiService(application)

    // Config state
    private val _config = MutableStateFlow(appSettings.getConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    // Active navigation step
    private val _currentStep = MutableStateFlow(AppStep.SETUP)
    val currentStep: StateFlow<AppStep> = _currentStep.asStateFlow()

    // Project data
    val project: StateFlow<ProjectEntity?> = projectDao.getProject(1L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Scenes list
    val scenes: StateFlow<List<SceneEntity>> = sceneDao.getScenesForProject(1L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback banner / toast
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Script analysis state
    private val _isAnalyzingScript = MutableStateFlow(false)
    val isAnalyzingScript: StateFlow<Boolean> = _isAnalyzingScript.asStateFlow()

    private val _analysisStatusText = MutableStateFlow("")
    val analysisStatusText: StateFlow<String> = _analysisStatusText.asStateFlow()

    // Generation Queue State
    private val _isGeneratingAll = MutableStateFlow(false)
    val isGeneratingAll: StateFlow<Boolean> = _isGeneratingAll.asStateFlow()

    private val _estimatedTimeRemainingSeconds = MutableStateFlow(0)
    val estimatedTimeRemainingSeconds: StateFlow<Int> = _estimatedTimeRemainingSeconds.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL") // ALL, PENDING, DONE, FAILED
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private var queueJob: Job? = null

    // Audio & Playback state
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlaybackMs = MutableStateFlow(0L)
    val currentPlaybackMs: StateFlow<Long> = _currentPlaybackMs.asStateFlow()

    private val _activeSceneIndex = MutableStateFlow(0)
    val activeSceneIndex: StateFlow<Int> = _activeSceneIndex.asStateFlow()

    private var playbackJob: Job? = null

    // Export State
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    private val _exportedFilePath = MutableStateFlow<String?>(null)
    val exportedFilePath: StateFlow<String?> = _exportedFilePath.asStateFlow()

    private var exportJob: Job? = null

    init {
        // Initialize default project if database is empty
        viewModelScope.launch {
            val existing = projectDao.getProjectSync(1L)
            if (existing == null) {
                projectDao.insertProject(
                    ProjectEntity(
                        id = 1L,
                        name = "Shadow Monarch: The Awakening",
                        script = SAMPLE_MANHWA_SCRIPT,
                        aspectRatio = "16:9",
                        artStyle = "Manhwa / Korean webtoon"
                    )
                )
            }
        }
    }

    fun setStep(step: AppStep) {
        _currentStep.value = step
    }

    fun updateConfig(newConfig: AppConfig) {
        appSettings.saveConfig(newConfig)
        _config.value = newConfig
        showMessage("Settings updated successfully")
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun updateProjectName(name: String) {
        viewModelScope.launch {
            val current = projectDao.getProjectSync(1L) ?: ProjectEntity(id = 1L)
            projectDao.updateProject(current.copy(name = name))
        }
    }

    fun updateAspectRatio(ratio: String) {
        viewModelScope.launch {
            val current = projectDao.getProjectSync(1L) ?: ProjectEntity(id = 1L)
            projectDao.updateProject(current.copy(aspectRatio = ratio))
        }
    }

    fun updateArtStyle(style: String, customStyle: String = "") {
        viewModelScope.launch {
            val current = projectDao.getProjectSync(1L) ?: ProjectEntity(id = 1L)
            projectDao.updateProject(current.copy(artStyle = style, customArtStyle = customStyle))
        }
    }

    fun updateScript(script: String) {
        viewModelScope.launch {
            val current = projectDao.getProjectSync(1L) ?: ProjectEntity(id = 1L)
            projectDao.updateProject(current.copy(script = script))
        }
    }

    fun loadSampleScript() {
        updateScript(SAMPLE_MANHWA_SCRIPT)
        showMessage("Loaded sample Korean Manhwa explainer script")
    }

    // Character Reference Management (up to 4)
    fun addCharacterRef(name: String, role: String, imageUri: String?) {
        viewModelScope.launch {
            val current = projectDao.getProjectSync(1L) ?: ProjectEntity(id = 1L)
            val list = parseCharacterRefs(current.characterRefsJson).toMutableList()
            if (list.size < 4) {
                list.add(CharacterRef(id = System.currentTimeMillis().toString(), name = name, role = role, imageUri = imageUri))
                projectDao.updateProject(current.copy(characterRefsJson = serializeCharacterRefs(list)))
                showMessage("Added reference for $name")
            } else {
                showMessage("Maximum 4 character references allowed")
            }
        }
    }

    fun removeCharacterRef(id: String) {
        viewModelScope.launch {
            val current = projectDao.getProjectSync(1L) ?: return@launch
            val list = parseCharacterRefs(current.characterRefsJson).filterNot { it.id == id }
            projectDao.updateProject(current.copy(characterRefsJson = serializeCharacterRefs(list)))
        }
    }

    fun getCharacterRefsList(): List<CharacterRef> {
        val json = project.value?.characterRefsJson ?: "[]"
        return parseCharacterRefs(json)
    }

    // Script Analysis & Scene Creation
    fun analyzeScriptAndCreateScenes() {
        val currentScript = project.value?.script.orEmpty()
        if (currentScript.isBlank()) {
            showMessage("Please enter or paste a narration script first")
            return
        }

        viewModelScope.launch {
            _isAnalyzingScript.value = true
            _analysisStatusText.value = "Analyzing narrative pacing and sentence boundaries…"

            val apiKey = _config.value.apiKey
            val model = _config.value.textModel

            val result = geminiService.analyzeScript(apiKey, model, currentScript)

            result.onSuccess { parsedList ->
                _analysisStatusText.value = "Creating ${parsedList.size} cinematic scenes…"
                delay(400)

                val sceneEntities = parsedList.mapIndexed { index, item ->
                    SceneEntity(
                        projectId = 1L,
                        sceneIndex = index + 1,
                        narration = item.narration,
                        visualPrompt = item.visualPrompt,
                        estimatedSeconds = item.estimatedSeconds,
                        mood = item.mood,
                        status = SceneStatus.PENDING.name
                    )
                }

                sceneDao.deleteAllScenesForProject(1L)
                sceneDao.insertScenes(sceneEntities)

                _isAnalyzingScript.value = false
                _analysisStatusText.value = ""
                _currentStep.value = AppStep.SCENES
                showMessage("Successfully created ${sceneEntities.size} scenes from your script!")
            }.onFailure { err ->
                _isAnalyzingScript.value = false
                _analysisStatusText.value = ""
                showMessage("Analysis error: ${err.localizedMessage ?: "Unknown error"}. Retrying with local sentence parser.")
                val fallbackList = geminiService.splitScriptLocally(currentScript)
                val sceneEntities = fallbackList.mapIndexed { index, item ->
                    SceneEntity(
                        projectId = 1L,
                        sceneIndex = index + 1,
                        narration = item.narration,
                        visualPrompt = item.visualPrompt,
                        estimatedSeconds = item.estimatedSeconds,
                        mood = item.mood,
                        status = SceneStatus.PENDING.name
                    )
                }
                sceneDao.deleteAllScenesForProject(1L)
                sceneDao.insertScenes(sceneEntities)
                _currentStep.value = AppStep.SCENES
            }
        }
    }

    // Scene Board Actions
    fun updateSceneNarration(sceneId: Long, newNarration: String) {
        viewModelScope.launch {
            val scene = sceneDao.getSceneById(sceneId) ?: return@launch
            sceneDao.updateScene(scene.copy(narration = newNarration))
        }
    }

    fun updateSceneVisualPrompt(sceneId: Long, newPrompt: String) {
        viewModelScope.launch {
            val scene = sceneDao.getSceneById(sceneId) ?: return@launch
            sceneDao.updateScene(scene.copy(visualPrompt = newPrompt))
        }
    }

    fun updateSceneDuration(sceneId: Long, newDuration: Float) {
        viewModelScope.launch {
            val scene = sceneDao.getSceneById(sceneId) ?: return@launch
            sceneDao.updateScene(scene.copy(estimatedSeconds = max(1.0f, newDuration)))
        }
    }

    fun setCustomSceneImage(sceneId: Long, imageUri: String) {
        viewModelScope.launch {
            val scene = sceneDao.getSceneById(sceneId) ?: return@launch
            sceneDao.updateScene(scene.copy(imageUri = imageUri, status = SceneStatus.DONE.name))
            showMessage("Scene #${scene.sceneIndex} image updated")
        }
    }

    fun deleteScene(sceneId: Long) {
        viewModelScope.launch {
            sceneDao.deleteSceneById(sceneId)
            // Re-index remaining
            val remaining = sceneDao.getScenesSync(1L)
            remaining.forEachIndexed { idx, s ->
                if (s.sceneIndex != idx + 1) {
                    sceneDao.updateScene(s.copy(sceneIndex = idx + 1))
                }
            }
            showMessage("Scene removed")
        }
    }

    fun moveScene(sceneId: Long, up: Boolean) {
        viewModelScope.launch {
            val all = sceneDao.getScenesSync(1L).toMutableList()
            val index = all.indexOfFirst { it.id == sceneId }
            if (index == -1) return@launch

            val targetIndex = if (up) index - 1 else index + 1
            if (targetIndex in all.indices) {
                val temp = all[index]
                all[index] = all[targetIndex]
                all[targetIndex] = temp

                all.forEachIndexed { i, s ->
                    sceneDao.updateScene(s.copy(sceneIndex = i + 1))
                }
            }
        }
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    // Image Generation Queue with Max 2 Concurrency
    fun generateAllImages() {
        if (_isGeneratingAll.value) return
        queueJob = viewModelScope.launch {
            _isGeneratingAll.value = true
            val pendingScenes = sceneDao.getScenesSync(1L).filter { it.status != SceneStatus.DONE.name }

            if (pendingScenes.isEmpty()) {
                showMessage("All scenes already have images!")
                _isGeneratingAll.value = false
                return@launch
            }

            val characterRefs = getCharacterRefsList()
            val projectData = project.value ?: ProjectEntity()
            val artStyle = if (projectData.artStyle == "Custom") projectData.customArtStyle else projectData.artStyle
            val aspectRatio = projectData.aspectRatio

            val semaphore = Semaphore(2) // Max 2 concurrent requests
            var completed = 0
            val total = pendingScenes.size
            _estimatedTimeRemainingSeconds.value = total * 3

            val jobs = pendingScenes.map { scene ->
                launch {
                    semaphore.withPermit {
                        if (!_isGeneratingAll.value) return@withPermit
                        sceneDao.updateScene(scene.copy(status = SceneStatus.GENERATING.name))

                        val res = geminiService.generateImage(
                            apiKey = _config.value.apiKey,
                            imageModel = _config.value.imageModel,
                            artStyle = artStyle,
                            aspectRatio = aspectRatio,
                            visualPrompt = scene.visualPrompt,
                            sceneIndex = scene.sceneIndex,
                            mood = scene.mood,
                            characterRefs = characterRefs
                        )

                        res.onSuccess { path ->
                            sceneDao.updateScene(
                                scene.copy(
                                    status = SceneStatus.DONE.name,
                                    imageUri = path,
                                    errorMessage = null
                                )
                            )
                        }.onFailure { err ->
                            sceneDao.updateScene(
                                scene.copy(
                                    status = SceneStatus.FAILED.name,
                                    errorMessage = err.localizedMessage ?: "Failed"
                                )
                            )
                        }

                        completed++
                        _estimatedTimeRemainingSeconds.value = max(0, (total - completed) * 3)
                    }
                }
            }

            jobs.forEach { it.join() }
            _isGeneratingAll.value = false
            _estimatedTimeRemainingSeconds.value = 0
            showMessage("Image generation complete! $completed / $total processed")
        }
    }

    fun regenerateSingleScene(sceneId: Long) {
        viewModelScope.launch {
            val scene = sceneDao.getSceneById(sceneId) ?: return@launch
            sceneDao.updateScene(scene.copy(status = SceneStatus.GENERATING.name))

            val characterRefs = getCharacterRefsList()
            val projectData = project.value ?: ProjectEntity()
            val artStyle = if (projectData.artStyle == "Custom") projectData.customArtStyle else projectData.artStyle
            val aspectRatio = projectData.aspectRatio

            val res = geminiService.generateImage(
                apiKey = _config.value.apiKey,
                imageModel = _config.value.imageModel,
                artStyle = artStyle,
                aspectRatio = aspectRatio,
                visualPrompt = scene.visualPrompt,
                sceneIndex = scene.sceneIndex,
                mood = scene.mood,
                characterRefs = characterRefs
            )

            res.onSuccess { path ->
                sceneDao.updateScene(
                    scene.copy(
                        status = SceneStatus.DONE.name,
                        imageUri = path,
                        errorMessage = null
                    )
                )
                showMessage("Generated image for Scene #${scene.sceneIndex}")
            }.onFailure { err ->
                sceneDao.updateScene(
                    scene.copy(
                        status = SceneStatus.FAILED.name,
                        errorMessage = err.localizedMessage
                    )
                )
                showMessage("Failed to generate scene: ${err.localizedMessage}")
            }
        }
    }

    fun stopGeneration() {
        _isGeneratingAll.value = false
        queueJob?.cancel()
        viewModelScope.launch {
            // Reset any currently generating scene back to pending
            val scenesList = sceneDao.getScenesSync(1L)
            scenesList.filter { it.status == SceneStatus.GENERATING.name }.forEach {
                sceneDao.updateScene(it.copy(status = SceneStatus.PENDING.name))
            }
            showMessage("Generation stopped")
        }
    }

    fun retryFailedScenes() {
        viewModelScope.launch {
            val failed = sceneDao.getScenesSync(1L).filter { it.status == SceneStatus.FAILED.name }
            failed.forEach {
                sceneDao.updateScene(it.copy(status = SceneStatus.PENDING.name))
            }
            generateAllImages()
        }
    }

    // Voiceover Sync
    fun setVoiceoverAudio(uriString: String, durationMs: Long) {
        viewModelScope.launch {
            val current = projectDao.getProjectSync(1L) ?: return@launch
            projectDao.updateProject(current.copy(audioUri = uriString, audioDurationMs = durationMs))
            showMessage("Voiceover audio attached (${durationMs / 1000}s)")
        }
    }

    fun autoArrangeScenes() {
        viewModelScope.launch {
            val currentProject = project.value ?: return@launch
            val audioMs = currentProject.audioDurationMs
            val currentScenes = sceneDao.getScenesSync(1L)

            if (currentScenes.isEmpty()) {
                showMessage("No scenes to arrange!")
                return@launch
            }

            val arranged = if (audioMs > 0) {
                AudioSyncManager.autoArrangeScenesByWordCount(currentScenes, audioMs)
            } else {
                // Word count baseline: 2.5 words per sec
                currentScenes.map { scene ->
                    val words = scene.narration.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                    val sec = max(3.5f, words / 2.5f)
                    scene.copy(estimatedSeconds = String.format("%.1f", sec).toFloat())
                }
            }

            arranged.forEach { sceneDao.updateScene(it) }
            showMessage("Auto-arranged ${arranged.size} scenes by narration word count pacing!")
        }
    }

    fun fitScenesToAudio() {
        viewModelScope.launch {
            val currentProject = project.value ?: return@launch
            val audioMs = currentProject.audioDurationMs
            if (audioMs <= 0) {
                showMessage("Please upload voiceover audio first to fit duration")
                return@launch
            }
            val currentScenes = sceneDao.getScenesSync(1L)
            val adjusted = AudioSyncManager.fitToAudio(currentScenes, audioMs)
            adjusted.forEach { sceneDao.updateScene(it) }
            showMessage("Scenes timing adjusted to match ${audioMs / 1000}s audio exactly")
        }
    }

    // Preview Player Controller
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        val sceneList = scenes.value
        if (sceneList.isEmpty()) {
            showMessage("No scenes to preview")
            return
        }

        val totalMs = AudioSyncManager.calculateTotalVideoDurationMs(sceneList)
        if (totalMs <= 0) return

        _isPlaying.value = true

        // Play audio if available
        val audioUriStr = project.value?.audioUri
        if (!audioUriStr.isNullOrBlank()) {
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(getApplication(), Uri.parse(audioUriStr))
                        prepare()
                        seekTo(_currentPlaybackMs.value.toInt())
                        start()
                    }
                } else {
                    mediaPlayer?.seekTo(_currentPlaybackMs.value.toInt())
                    mediaPlayer?.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        playbackJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis() - _currentPlaybackMs.value
            while (_isPlaying.value && _currentPlaybackMs.value < totalMs) {
                val now = System.currentTimeMillis()
                val currentMs = now - startTime
                _currentPlaybackMs.value = currentMs
                _activeSceneIndex.value = AudioSyncManager.getCurrentSceneIndex(scenes.value, currentMs)
                delay(33) // ~30 FPS UI ticker
            }

            if (_currentPlaybackMs.value >= totalMs) {
                pausePlayback()
                _currentPlaybackMs.value = 0L
                _activeSceneIndex.value = 0
            }
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
    }

    fun seekPlayback(targetMs: Long) {
        val totalMs = AudioSyncManager.calculateTotalVideoDurationMs(scenes.value)
        val clamped = targetMs.coerceIn(0L, max(1L, totalMs))
        _currentPlaybackMs.value = clamped
        _activeSceneIndex.value = AudioSyncManager.getCurrentSceneIndex(scenes.value, clamped)
        mediaPlayer?.seekTo(clamped.toInt())
    }

    fun updateSubtitles(enabled: Boolean, size: Int, pos: String) {
        viewModelScope.launch {
            val current = projectDao.getProjectSync(1L) ?: return@launch
            projectDao.updateProject(
                current.copy(
                    subtitlesEnabled = enabled,
                    subtitleFontSize = size,
                    subtitlePosition = pos
                )
            )
        }
    }

    fun updateCrossfade(duration: Float) {
        viewModelScope.launch {
            val current = projectDao.getProjectSync(1L) ?: return@launch
            projectDao.updateProject(current.copy(crossfadeDuration = duration))
        }
    }

    fun updateBgMusic(enabled: Boolean, volume: Float) {
        viewModelScope.launch {
            val current = projectDao.getProjectSync(1L) ?: return@launch
            projectDao.updateProject(current.copy(bgMusicEnabled = enabled, bgMusicVolume = volume))
        }
    }

    // Export Rendering Simulation
    fun startExport(resolution: String, fps: Int) {
        val sceneList = scenes.value
        if (sceneList.isEmpty()) {
            showMessage("No scenes to export!")
            return
        }

        exportJob?.cancel()
        exportJob = viewModelScope.launch {
            _isExporting.value = true
            _exportProgress.value = 0f
            _exportedFilePath.value = null

            val totalDurationMs = AudioSyncManager.calculateTotalVideoDurationMs(sceneList)
            val totalSeconds = (totalDurationMs / 1000f).coerceAtLeast(5f)
            val totalFrames = (totalSeconds * fps).toInt().coerceAtMost(3600) // Safe frame count for demo

            var frame = 0
            while (frame < totalFrames && _isExporting.value) {
                frame += 12 // Fast rendering step
                val prog = (frame.toFloat() / totalFrames).coerceAtMost(0.99f)
                _exportProgress.value = prog
                delay(60)
            }

            if (_isExporting.value) {
                _exportProgress.value = 1.0f
                // Save exported summary file / dummy MP4 container
                val dir = File(getApplication<Application>().filesDir, "exports").apply { if (!exists()) mkdirs() }
                val exportFile = File(dir, "ManhwaVid_${System.currentTimeMillis()}.mp4")
                if (!exportFile.exists()) {
                    exportFile.writeBytes(ByteArray(1024 * 512)) // Mock mp4 payload
                }
                _exportedFilePath.value = exportFile.absolutePath
                _isExporting.value = false
                showMessage("Export complete! Saved to ${exportFile.name}")
            }
        }
    }

    fun cancelExport() {
        _isExporting.value = false
        _exportProgress.value = 0f
        exportJob?.cancel()
        showMessage("Export cancelled")
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun parseCharacterRefs(json: String): List<CharacterRef> {
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<CharacterRef>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CharacterRef(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        role = obj.getString("role"),
                        imageUri = obj.optString("imageUri").takeIf { it.isNotBlank() }
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeCharacterRefs(list: List<CharacterRef>): String {
        val array = JSONArray()
        list.forEach { ref ->
            array.put(JSONObject().apply {
                put("id", ref.id)
                put("name", ref.name)
                put("role", ref.role)
                put("imageUri", ref.imageUri ?: "")
            })
        }
        return array.toString()
    }

    companion object {
        const val SAMPLE_MANHWA_SCRIPT =
            "In a world where mysterious dimensional gates shattered civilization, monsters poured into the human realm without mercy. " +
            "Jin-Woo, universally mocked as the weakest hunter of all mankind, stood before the towering obsidian gates of an S-Rank double dungeon. " +
            "The ancient stone statues turned their glowing crimson eyes upon the raid party as the sanctuary gates slammed shut. " +
            "Sacrificing himself to protect his fallen comrades, Jin-Woo watched his health reach zero beneath the merciless god statue. " +
            "Suddenly, an electric blue holographic screen illuminated the darkness before his fading vision: [Congratulations, you have qualified to become a Player]. " +
            "A dark shadowy energy erupted from his chest, shattering the chains of weakness forever as the true Shadow Monarch awakens!"
    }
}
