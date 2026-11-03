package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.audio.AudioSyncManager
import com.example.data.ProjectEntity
import com.example.data.SceneEntity
import com.example.ui.theme.ManhwaCyan
import com.example.ui.theme.ManhwaIndigo
import com.example.ui.theme.ManhwaPurple
import java.io.File
import kotlin.math.max

@Composable
fun PreviewEditScreen(
    project: ProjectEntity,
    scenes: List<SceneEntity>,
    isPlaying: Boolean,
    currentPlaybackMs: Long,
    activeSceneIndex: Int,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onUpdateSubtitles: (Boolean, Int, String) -> Unit,
    onUpdateCrossfade: (Float) -> Unit,
    onUpdateBgMusic: (Boolean, Float) -> Unit,
    onNext: () -> Unit
) {
    val totalMs = AudioSyncManager.calculateTotalVideoDurationMs(scenes)
    val activeScene = scenes.getOrNull(activeSceneIndex)

    // Ken Burns effect: alternating zoom-in and zoom-out with pan
    val isEvenScene = activeSceneIndex % 2 == 0
    val targetScale = if (isEvenScene) 1.15f else 1.02f
    val targetTranslationX = if (isEvenScene) 15f else -15f

    val animatedScale by animateFloatAsState(
        targetValue = if (isPlaying) targetScale else 1.0f,
        animationSpec = tween(
            durationMillis = ((activeScene?.estimatedSeconds ?: 6f) * 1000).toInt(),
            easing = FastOutSlowInEasing
        ),
        label = "KenBurnsScale"
    )

    val animatedPanX by animateFloatAsState(
        targetValue = if (isPlaying) targetTranslationX else 0f,
        animationSpec = tween(
            durationMillis = ((activeScene?.estimatedSeconds ?: 6f) * 1000).toInt(),
            easing = FastOutSlowInEasing
        ),
        label = "KenBurnsPan"
    )

    val playerAspectRatio = when (project.aspectRatio) {
        "9:16" -> 9f / 16f
        "1:1" -> 1f
        else -> 16f / 9f
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("preview_edit_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "5. Preview & Edit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Cinematic video playback with Ken Burns motion & subtitles",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Cinematic Video Player Canvas Box
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .testTag("ken_burns_player_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(playerAspectRatio)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // Scene Image with Ken Burns animation
                    if (activeScene != null && !activeScene.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = File(activeScene.imageUri),
                            contentDescription = "Scene #${activeScene.sceneIndex}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                    translationX = animatedPanX
                                }
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Scene #${(activeScene?.sceneIndex ?: 1)}",
                                color = ManhwaPurple,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeScene?.mood?.uppercase() ?: "PREVIEW",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Burned-in Subtitles Overlay
                    if (project.subtitlesEnabled && activeScene != null && activeScene.narration.isNotBlank()) {
                        val subAlignment = when (project.subtitlePosition) {
                            "top" -> Alignment.TopCenter
                            "center" -> Alignment.Center
                            else -> Alignment.BottomCenter
                        }

                        Box(
                            modifier = Modifier
                                .align(subAlignment)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = activeScene.narration,
                                color = Color.White,
                                fontSize = project.subtitleFontSize.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = (project.subtitleFontSize * 1.3).sp
                            )
                        }
                    }

                    // Scene Tag Badge (Top Left)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Scene #${(activeScene?.sceneIndex ?: 1)} / ${scenes.size}",
                            color = ManhwaCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Playback Transport Controls
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Timecode display & Scrub bar
                    val currentSec = currentPlaybackMs / 1000
                    val totalSec = totalMs / 1000
                    val timeStr = String.format("%02d:%02d / %02d:%02d", currentSec / 60, currentSec % 60, totalSec / 60, totalSec % 60)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeStr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ken Burns Motion Active",
                            fontSize = 10.sp,
                            color = ManhwaCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Slider
                    Slider(
                        value = if (totalMs > 0) currentPlaybackMs.toFloat() else 0f,
                        onValueChange = { onSeek(it.toLong()) },
                        valueRange = 0f..max(1f, totalMs.toFloat()),
                        colors = SliderDefaults.colors(
                            thumbColor = ManhwaPurple,
                            activeTrackColor = ManhwaPurple
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("playback_seekbar")
                    )

                    // Buttons: Play/Pause, Replay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onSeek(0L) },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Restart",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(ManhwaPurple)
                                .clickable { onTogglePlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Subtitles & Transition Settings Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Subtitles & Video Transitions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Subtitle Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ClosedCaption,
                                contentDescription = null,
                                tint = ManhwaPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Burned-in Subtitles",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = project.subtitlesEnabled,
                            onCheckedChange = {
                                onUpdateSubtitles(it, project.subtitleFontSize, project.subtitlePosition)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = ManhwaPurple)
                        )
                    }

                    if (project.subtitlesEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Subtitle Size options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Small (14sp)" to 14,
                                "Medium (16sp)" to 16,
                                "Large (18sp)" to 18
                            ).forEach { (label, size) ->
                                val isSel = project.subtitleFontSize == size
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) ManhwaPurple else MaterialTheme.colorScheme.surface)
                                        .border(
                                            1.dp,
                                            if (isSel) ManhwaPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            onUpdateSubtitles(true, size, project.subtitlePosition)
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subtitle Position
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("bottom", "center", "top").forEach { pos ->
                                val isSel = project.subtitlePosition == pos
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) ManhwaPurple else MaterialTheme.colorScheme.surface)
                                        .border(
                                            1.dp,
                                            if (isSel) ManhwaPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            onUpdateSubtitles(true, project.subtitleFontSize, pos)
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pos.replaceFirstChar { it.uppercase() },
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Crossfade Transition
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Crossfade Duration",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${project.crossfadeDuration}s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ManhwaPurple
                        )
                    }
                    Slider(
                        value = project.crossfadeDuration,
                        onValueChange = onUpdateCrossfade,
                        valueRange = 0.2f..1.5f,
                        steps = 5,
                        colors = SliderDefaults.colors(thumbColor = ManhwaPurple, activeTrackColor = ManhwaPurple)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Background Music & Auto-Ducking
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = ManhwaCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ambient Background Music (Ducked)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = project.bgMusicEnabled,
                            onCheckedChange = { onUpdateBgMusic(it, project.bgMusicVolume) },
                            colors = SwitchDefaults.colors(checkedThumbColor = ManhwaCyan)
                        )
                    }
                }
            }
        }

        // Proceed to Export Button
        item {
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = ManhwaPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("next_to_export_button")
            ) {
                Text("Proceed to Export Video", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
