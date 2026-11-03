package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ProjectEntity
import com.example.ui.AppStep
import com.example.ui.MainViewModel
import com.example.ui.components.HowToUseDialog
import com.example.ui.components.SettingsModal
import com.example.ui.components.StepNavigationStrip
import com.example.ui.components.TopStudioBar
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.PreviewEditScreen
import com.example.ui.screens.ProjectSetupScreen
import com.example.ui.screens.SceneBoardScreen
import com.example.ui.screens.ScriptInputScreen
import com.example.ui.screens.VoiceoverSyncScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val config by viewModel.config.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = config.isDarkMode) {
                ManhwaVidApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ManhwaVidApp(viewModel: MainViewModel) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val project by viewModel.project.collectAsStateWithLifecycle()
    val scenes by viewModel.scenes.collectAsStateWithLifecycle()
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val isAnalyzing by viewModel.isAnalyzingScript.collectAsStateWithLifecycle()
    val analysisStatusText by viewModel.analysisStatusText.collectAsStateWithLifecycle()

    val isGeneratingAll by viewModel.isGeneratingAll.collectAsStateWithLifecycle()
    val remainingTimeSeconds by viewModel.estimatedTimeRemainingSeconds.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()

    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPlaybackMs by viewModel.currentPlaybackMs.collectAsStateWithLifecycle()
    val activeSceneIndex by viewModel.activeSceneIndex.collectAsStateWithLifecycle()

    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val exportedFilePath by viewModel.exportedFilePath.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSettingsModal by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(!config.hasSeenOnboarding) }

    // User snackbar toast trigger
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Back handling across steps
    BackHandler(enabled = currentStep != AppStep.SETUP) {
        val prevStep = when (currentStep) {
            AppStep.EXPORT -> AppStep.PREVIEW
            AppStep.PREVIEW -> AppStep.VOICEOVER
            AppStep.VOICEOVER -> AppStep.SCENES
            AppStep.SCENES -> AppStep.SCRIPT
            AppStep.SCRIPT -> AppStep.SETUP
            AppStep.SETUP -> AppStep.SETUP
        }
        viewModel.setStep(prevStep)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopStudioBar(
                    projectName = project?.name.orEmpty(),
                    config = config,
                    onOpenSettings = { showSettingsModal = true },
                    onOpenHelp = { showHelpDialog = true },
                    onToggleDarkMode = {
                        viewModel.updateConfig(config.copy(isDarkMode = !config.isDarkMode))
                    }
                )

                StepNavigationStrip(
                    currentStep = currentStep,
                    isSetupComplete = (project?.name.orEmpty()).isNotBlank(),
                    isScriptComplete = (project?.script.orEmpty()).isNotBlank(),
                    isScenesComplete = scenes.isNotEmpty(),
                    isVoiceoverComplete = (project?.audioDurationMs ?: 0L) > 0L,
                    onStepSelected = { viewModel.setStep(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val currentProject = project ?: ProjectEntity(id = 1L)

            when (currentStep) {
                AppStep.SETUP -> {
                    ProjectSetupScreen(
                        project = currentProject,
                        characterRefs = viewModel.getCharacterRefsList(),
                        onUpdateName = { viewModel.updateProjectName(it) },
                        onUpdateAspectRatio = { viewModel.updateAspectRatio(it) },
                        onUpdateArtStyle = { style, custom -> viewModel.updateArtStyle(style, custom) },
                        onAddCharacterRef = { name, role, uri -> viewModel.addCharacterRef(name, role, uri) },
                        onRemoveCharacterRef = { id -> viewModel.removeCharacterRef(id) },
                        onNext = { viewModel.setStep(AppStep.SCRIPT) }
                    )
                }

                AppStep.SCRIPT -> {
                    ScriptInputScreen(
                        script = currentProject.script,
                        isAnalyzing = isAnalyzing,
                        analysisStatus = analysisStatusText,
                        onScriptChange = { viewModel.updateScript(it) },
                        onLoadSampleScript = { viewModel.loadSampleScript() },
                        onAnalyzeScript = { viewModel.analyzeScriptAndCreateScenes() }
                    )
                }

                AppStep.SCENES -> {
                    SceneBoardScreen(
                        scenes = scenes,
                        aspectRatio = currentProject.aspectRatio,
                        isGeneratingAll = isGeneratingAll,
                        remainingTimeSeconds = remainingTimeSeconds,
                        statusFilter = statusFilter,
                        onFilterChange = { viewModel.setStatusFilter(it) },
                        onGenerateAll = { viewModel.generateAllImages() },
                        onStopGeneration = { viewModel.stopGeneration() },
                        onRetryFailed = { viewModel.retryFailedScenes() },
                        onRegenerateScene = { id -> viewModel.regenerateSingleScene(id) },
                        onCustomImageSelected = { id, uri -> viewModel.setCustomSceneImage(id, uri) },
                        onDeleteScene = { id -> viewModel.deleteScene(id) },
                        onMoveScene = { id, up -> viewModel.moveScene(id, up) },
                        onUpdateNarration = { id, text -> viewModel.updateSceneNarration(id, text) },
                        onUpdateVisualPrompt = { id, prompt -> viewModel.updateSceneVisualPrompt(id, prompt) },
                        onUpdateDuration = { id, sec -> viewModel.updateSceneDuration(id, sec) },
                        onNext = { viewModel.setStep(AppStep.VOICEOVER) }
                    )
                }

                AppStep.VOICEOVER -> {
                    VoiceoverSyncScreen(
                        project = currentProject,
                        scenes = scenes,
                        onAttachAudio = { uri, duration -> viewModel.setVoiceoverAudio(uri, duration) },
                        onAutoArrange = { viewModel.autoArrangeScenes() },
                        onFitToAudio = { viewModel.fitScenesToAudio() },
                        onUpdateSceneDuration = { id, sec -> viewModel.updateSceneDuration(id, sec) },
                        onNext = { viewModel.setStep(AppStep.PREVIEW) }
                    )
                }

                AppStep.PREVIEW -> {
                    PreviewEditScreen(
                        project = currentProject,
                        scenes = scenes,
                        isPlaying = isPlaying,
                        currentPlaybackMs = currentPlaybackMs,
                        activeSceneIndex = activeSceneIndex,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSeek = { viewModel.seekPlayback(it) },
                        onUpdateSubtitles = { enabled, size, pos -> viewModel.updateSubtitles(enabled, size, pos) },
                        onUpdateCrossfade = { dur -> viewModel.updateCrossfade(dur) },
                        onUpdateBgMusic = { enabled, vol -> viewModel.updateBgMusic(enabled, vol) },
                        onNext = { viewModel.setStep(AppStep.EXPORT) }
                    )
                }

                AppStep.EXPORT -> {
                    ExportScreen(
                        project = currentProject,
                        scenes = scenes,
                        isExporting = isExporting,
                        exportProgress = exportProgress,
                        exportedFilePath = exportedFilePath,
                        onStartExport = { res, fps -> viewModel.startExport(res, fps) },
                        onCancelExport = { viewModel.cancelExport() }
                    )
                }
            }

            // Settings Modal
            if (showSettingsModal) {
                SettingsModal(
                    config = config,
                    onDismiss = { showSettingsModal = false },
                    onSave = { updated ->
                        viewModel.updateConfig(updated)
                    }
                )
            }

            // How To Use Beginner Guide
            if (showHelpDialog) {
                HowToUseDialog(
                    onDismiss = {
                        showHelpDialog = false
                        viewModel.updateConfig(config.copy(hasSeenOnboarding = true))
                    }
                )
            }
        }
    }
}
