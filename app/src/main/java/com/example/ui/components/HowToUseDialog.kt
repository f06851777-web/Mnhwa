package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ManhwaCyan
import com.example.ui.theme.ManhwaEmerald
import com.example.ui.theme.ManhwaIndigo
import com.example.ui.theme.ManhwaPurple

@Composable
fun HowToUseDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("how_to_use_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome to ManhwaVid",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "How to create photo-explainer videos in 4 steps",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GuideStepItem(
                    stepNumber = "1",
                    title = "Project Setup & Characters",
                    desc = "Choose aspect ratio (16:9 for YouTube, 9:16 for Shorts), set an art style (Manhwa, Anime, etc.), and upload up to 4 character reference photos for visual consistency.",
                    icon = Icons.Default.AutoAwesome,
                    color = ManhwaPurple
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideStepItem(
                    stepNumber = "2",
                    title = "Paste & Analyze Script",
                    desc = "Paste your narrative script (supports English, Hindi, Hinglish). Gemini splits it into 5–8 second scenes at natural sentence boundaries with visual prompts.",
                    icon = Icons.Default.EditNote,
                    color = ManhwaIndigo
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideStepItem(
                    stepNumber = "3",
                    title = "Generate Scene Boards",
                    desc = "Click 'Generate All Images' to queue image creation with Gemini (Nano Banana). Re-generate, edit narration prompts, or upload custom images per scene.",
                    icon = Icons.Default.MovieCreation,
                    color = ManhwaCyan
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideStepItem(
                    stepNumber = "4",
                    title = "Sync Voiceover & Export",
                    desc = "Attach a voiceover file and click 'Auto-Arrange' to sync scene durations by word count. Preview with Ken Burns camera zoom and subtitles, then export your video!",
                    icon = Icons.Default.GraphicEq,
                    color = ManhwaEmerald
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ManhwaPurple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_guide_button")
                ) {
                    Text("Got it, let's create!")
                }
            }
        }
    }
}

@Composable
private fun GuideStepItem(
    stepNumber: String,
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = "$stepNumber. $title",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
