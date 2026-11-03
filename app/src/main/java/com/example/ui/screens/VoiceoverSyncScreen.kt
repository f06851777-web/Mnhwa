package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSyncManager
import com.example.data.ProjectEntity
import com.example.data.SceneEntity
import com.example.ui.theme.ManhwaAmber
import com.example.ui.theme.ManhwaCyan
import com.example.ui.theme.ManhwaEmerald
import com.example.ui.theme.ManhwaIndigo
import com.example.ui.theme.ManhwaPurple
import com.example.ui.theme.ManhwaRose
import kotlin.math.abs
import kotlin.math.max

@Composable
fun VoiceoverSyncScreen(
    project: ProjectEntity,
    scenes: List<SceneEntity>,
    onAttachAudio: (String, Long) -> Unit,
    onAutoArrange: () -> Unit,
    onFitToAudio: () -> Unit,
    onUpdateSceneDuration: (Long, Float) -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current

    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val durationMs = AudioSyncManager.getAudioDurationMs(context, uri)
            onAttachAudio(uri.toString(), durationMs)
        }
    }

    val totalScenesDurationSec = scenes.sumOf { it.estimatedSeconds.toDouble() }.toFloat()
    val audioDurationSec = project.audioDurationMs / 1000f
    val hasAudio = project.audioDurationMs > 0
    val durationDiff = abs(totalScenesDurationSec - audioDurationSec)
    val isDurationMismatched = hasAudio && durationDiff > 1.5f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("voiceover_sync_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "4. Voiceover Sync",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Align scene durations with your voiceover audio narration",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Voiceover Audio Upload Card & Waveform
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ManhwaIndigo.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = ManhwaPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (hasAudio) "Voiceover Track Attached" else "No Voiceover Track",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (hasAudio) "Duration: ${String.format("%.1f", audioDurationSec)}s" else "Upload MP3, WAV, or M4A audio file",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { audioFilePicker.launch("audio/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = ManhwaPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("upload_audio_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (hasAudio) "Replace" else "Upload Audio", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Audio Waveform Visualization Graphic
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bars = 38
                            for (i in 0 until bars) {
                                val hFactor = if (hasAudio) {
                                    // Simulated audio waveform height curve
                                    val sinVal = kotlin.math.sin(i * 0.45)
                                    val cosVal = kotlin.math.cos(i * 0.25)
                                    val factor = (abs(sinVal + cosVal) / 2.0).coerceIn(0.2, 1.0).toFloat()
                                    (factor * 44f).dp
                                } else {
                                    6.dp
                                }

                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(hFactor)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (hasAudio) ManhwaCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-Arrange Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAutoArrange,
                            colors = ButtonDefaults.buttonColors(containerColor = ManhwaIndigo),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("auto_arrange_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto-Arrange by Script", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (hasAudio) {
                            OutlinedButton(
                                onClick = onFitToAudio,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("fit_to_audio_button")
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Fit to Audio", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Duration Warning Banner
        if (isDurationMismatched) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ManhwaAmber.copy(alpha = 0.15f))
                        .border(1.dp, ManhwaAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ManhwaAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Duration Discrepancy",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ManhwaAmber
                            )
                            Text(
                                text = "Total scenes duration (${String.format("%.1f", totalScenesDurationSec)}s) does not match voiceover audio (${String.format("%.1f", audioDurationSec)}s).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = onFitToAudio,
                            colors = ButtonDefaults.buttonColors(containerColor = ManhwaAmber),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Fix", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Timeline Blocks Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scene Timeline Blocks (${scenes.size} scenes)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total: ${String.format("%.1f", totalScenesDurationSec)}s",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ManhwaPurple
                )
            }
        }

        // Visual Proportion Timeline Bar
        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(4.dp)
                ) {
                    if (scenes.isNotEmpty() && totalScenesDurationSec > 0f) {
                        scenes.forEachIndexed { idx, scene ->
                            val weight = scene.estimatedSeconds / totalScenesDurationSec
                            val barColor = if (idx % 2 == 0) ManhwaPurple else ManhwaIndigo
                            Box(
                                modifier = Modifier
                                    .weight(weight.coerceAtLeast(0.01f))
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(barColor)
                                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(3.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${scene.sceneIndex}",
                                    fontSize = 8.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Individual Scene Duration Fine-Tuning List
        items(scenes, key = { it.id }) { scene ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Scene #${scene.sceneIndex}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ManhwaPurple
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${scene.estimatedSeconds}s",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = scene.narration,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    // Plus / Minus Duration Fine-tuning
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val newSec = max(1.5f, scene.estimatedSeconds - 0.5f)
                                onUpdateSceneDuration(scene.id, String.format("%.1f", newSec).toFloat())
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                            }
                        }

                        IconButton(
                            onClick = {
                                val newSec = scene.estimatedSeconds + 0.5f
                                onUpdateSceneDuration(scene.id, String.format("%.1f", newSec).toFloat())
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        // Proceed to Preview Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = ManhwaPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("next_to_preview_button")
            ) {
                Text("Proceed to Preview & Edit", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
