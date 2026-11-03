package com.example.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.SceneEntity
import com.example.data.SceneStatus
import com.example.ui.components.SceneCard
import com.example.ui.theme.ManhwaAmber
import com.example.ui.theme.ManhwaCyan
import com.example.ui.theme.ManhwaEmerald
import com.example.ui.theme.ManhwaIndigo
import com.example.ui.theme.ManhwaPurple
import com.example.ui.theme.ManhwaRose
import com.example.util.ImageStorageUtil

@Composable
fun SceneBoardScreen(
    scenes: List<SceneEntity>,
    aspectRatio: String,
    isGeneratingAll: Boolean,
    remainingTimeSeconds: Int,
    statusFilter: String,
    onFilterChange: (String) -> Unit,
    onGenerateAll: () -> Unit,
    onStopGeneration: () -> Unit,
    onRetryFailed: () -> Unit,
    onRegenerateScene: (Long) -> Unit,
    onCustomImageSelected: (Long, String) -> Unit,
    onDeleteScene: (Long) -> Unit,
    onMoveScene: (Long, Boolean) -> Unit,
    onUpdateNarration: (Long, String) -> Unit,
    onUpdateVisualPrompt: (Long, String) -> Unit,
    onUpdateDuration: (Long, Float) -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val totalCount = scenes.size
    val doneCount = scenes.count { it.status == SceneStatus.DONE.name }
    val failedCount = scenes.count { it.status == SceneStatus.FAILED.name }
    val pendingCount = scenes.count { it.status == SceneStatus.PENDING.name }

    val filteredScenes = when (statusFilter) {
        "DONE" -> scenes.filter { it.status == SceneStatus.DONE.name }
        "PENDING" -> scenes.filter { it.status == SceneStatus.PENDING.name }
        "FAILED" -> scenes.filter { it.status == SceneStatus.FAILED.name }
        else -> scenes
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("scene_board_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Progress Summary
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "3. Scene Board",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Review and generate image visuals for each scene",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Done counter badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ManhwaEmerald.copy(alpha = 0.15f))
                            .border(1.dp, ManhwaEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$doneCount / $totalCount Done",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ManhwaEmerald
                        )
                    }
                }
            }
        }

        // Action Toolbar Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Main action buttons: Generate All, Stop, Retry Failed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isGeneratingAll) {
                            Button(
                                onClick = onStopGeneration,
                                colors = ButtonDefaults.buttonColors(containerColor = ManhwaRose),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("stop_generation_button")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop Queue", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = onGenerateAll,
                                enabled = pendingCount > 0 || failedCount > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = ManhwaPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("generate_all_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate All Images", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (failedCount > 0 && !isGeneratingAll) {
                            OutlinedButton(
                                onClick = onRetryFailed,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("retry_failed_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry Failed ($failedCount)", fontSize = 11.sp)
                            }
                        }
                    }

                    // Time estimate indicator during queue
                    if (isGeneratingAll && remainingTimeSeconds > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Estimated time remaining: ~${remainingTimeSeconds}s (Processing 2 concurrent scenes)",
                            fontSize = 11.sp,
                            color = ManhwaCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )

                        listOf(
                            "ALL" to "All ($totalCount)",
                            "PENDING" to "Pending ($pendingCount)",
                            "DONE" to "Done ($doneCount)",
                            "FAILED" to "Failed ($failedCount)"
                        ).forEach { (key, label) ->
                            val isSel = statusFilter == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSel) ManhwaIndigo else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable { onFilterChange(key) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
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
                }
            }
        }

        // Empty state
        if (filteredScenes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (scenes.isEmpty()) "No scenes created yet. Paste and analyze a script first!" else "No scenes match filter '$statusFilter'",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Scene Cards List
        items(filteredScenes, key = { it.id }) { scene ->
            SceneCard(
                scene = scene,
                aspectRatio = aspectRatio,
                onRegenerate = { onRegenerateScene(scene.id) },
                onCustomImageSelected = { uri ->
                    val path = ImageStorageUtil.copyUriToInternalStorage(
                        context,
                        uri,
                        "custom_scene_${scene.sceneIndex}_${System.currentTimeMillis()}"
                    )
                    if (path != null) {
                        onCustomImageSelected(scene.id, path)
                    }
                },
                onDelete = { onDeleteScene(scene.id) },
                onMoveUp = { onMoveScene(scene.id, true) },
                onMoveDown = { onMoveScene(scene.id, false) },
                onUpdateNarration = { newText -> onUpdateNarration(scene.id, newText) },
                onUpdateVisualPrompt = { newPrompt -> onUpdateVisualPrompt(scene.id, newPrompt) },
                onUpdateDuration = { newSec -> onUpdateDuration(scene.id, newSec) }
            )
        }

        // Proceed to Voiceover button
        if (scenes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = ManhwaPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("next_to_voiceover_button")
                ) {
                    Text("Proceed to Voiceover Sync", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
