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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CharacterRef
import com.example.data.ProjectEntity
import com.example.ui.theme.ManhwaCyan
import com.example.ui.theme.ManhwaIndigo
import com.example.ui.theme.ManhwaPurple
import com.example.util.ImageStorageUtil
import java.io.File

@Composable
fun ProjectSetupScreen(
    project: ProjectEntity,
    characterRefs: List<CharacterRef>,
    onUpdateName: (String) -> Unit,
    onUpdateAspectRatio: (String) -> Unit,
    onUpdateArtStyle: (String, String) -> Unit,
    onAddCharacterRef: (String, String, String?) -> Unit,
    onRemoveCharacterRef: (String) -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var isAddingCharacter by remember { mutableStateOf(false) }
    var charName by remember { mutableStateOf("") }
    var charRole by remember { mutableStateOf("") }
    var selectedCharImageUri by remember { mutableStateOf<String?>(null) }
    var customStyleText by remember { mutableStateOf(project.customArtStyle) }

    val charImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val saved = ImageStorageUtil.copyUriToInternalStorage(
                context,
                uri,
                "char_ref_${System.currentTimeMillis()}"
            )
            selectedCharImageUri = saved
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("project_setup_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Text(
                text = "1. Project Setup",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Configure aspect ratio, manhwa art style, and recurring characters",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Project Name Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Project Title",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = project.name,
                        onValueChange = onUpdateName,
                        placeholder = { Text("e.g. Solo Leveling: Shadow Monarch") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_name_input"),
                        singleLine = true
                    )
                }
            }
        }

        // Aspect Ratio Selector Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Aspect Ratio",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Optimized for your target publishing platform",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AspectRatioOption(
                            title = "16:9",
                            subtitle = "YouTube Landscape",
                            icon = Icons.Default.Tv,
                            isSelected = project.aspectRatio == "16:9",
                            onClick = { onUpdateAspectRatio("16:9") },
                            modifier = Modifier.weight(1f)
                        )
                        AspectRatioOption(
                            title = "9:16",
                            subtitle = "Shorts / Reels",
                            icon = Icons.Default.Smartphone,
                            isSelected = project.aspectRatio == "9:16",
                            onClick = { onUpdateAspectRatio("9:16") },
                            modifier = Modifier.weight(1f)
                        )
                        AspectRatioOption(
                            title = "1:1",
                            subtitle = "Square Feed",
                            icon = Icons.Default.Palette,
                            isSelected = project.aspectRatio == "1:1",
                            onClick = { onUpdateAspectRatio("1:1") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Art Style Presets
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Art Style Preset",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sets the visual aesthetic passed to Gemini Image Generation",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val presets = listOf(
                        "Manhwa / Korean webtoon",
                        "Anime",
                        "Realistic",
                        "Watercolor",
                        "Flat illustration",
                        "Custom"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.chunked(2).forEach { rowPresets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPresets.forEach { preset ->
                                    val isSelected = project.artStyle == preset
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) ManhwaPurple
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) ManhwaPurple
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                onUpdateArtStyle(preset, customStyleText)
                                            }
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = preset,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                if (rowPresets.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    if (project.artStyle == "Custom") {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customStyleText,
                            onValueChange = {
                                customStyleText = it
                                onUpdateArtStyle("Custom", it)
                            },
                            placeholder = { Text("e.g. Cyberpunk Noir 90s Cel Shading") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Character References (up to 4)
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
                        Column {
                            Text(
                                text = "Character References (${characterRefs.size}/4)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sent with every image request for visual character consistency",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (characterRefs.size < 4 && !isAddingCharacter) {
                            Button(
                                onClick = { isAddingCharacter = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ManhwaPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("add_character_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Character references row
                    if (characterRefs.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(characterRefs) { charRef ->
                                CharacterRefCard(
                                    ref = charRef,
                                    onDelete = { onRemoveCharacterRef(charRef.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else if (!isAddingCharacter) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No character references added yet. Add main protagonist or recurring objects for continuous visual style.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Add Character inline form
                    if (isAddingCharacter) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Add Character or Recurring Asset",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = charName,
                                        onValueChange = { charName = it },
                                        placeholder = { Text("Name (e.g. Jin-Woo)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = charRole,
                                        onValueChange = { charRole = it },
                                        placeholder = { Text("Role (Protagonist, villain)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            charImagePicker.launch(
                                                androidx.activity.result.PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ManhwaIndigo),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            if (selectedCharImageUri != null) "Photo Selected" else "Pick Photo",
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row {
                                        Button(
                                            onClick = {
                                                isAddingCharacter = false
                                                charName = ""
                                                charRole = ""
                                                selectedCharImageUri = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Cancel", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Button(
                                            onClick = {
                                                if (charName.isNotBlank()) {
                                                    onAddCharacterRef(
                                                        charName.trim(),
                                                        charRole.ifBlank { "Character" },
                                                        selectedCharImageUri
                                                    )
                                                    isAddingCharacter = false
                                                    charName = ""
                                                    charRole = ""
                                                    selectedCharImageUri = null
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ManhwaPurple),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Save", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Next Button
        item {
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = ManhwaPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("next_to_script_button")
            ) {
                Text("Proceed to Script Input", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AspectRatioOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ManhwaPurple else MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (isSelected) ManhwaPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CharacterRefCard(
    ref: CharacterRef,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .width(110.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    ) {
        Box {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ManhwaIndigo.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!ref.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = File(ref.imageUri),
                            contentDescription = ref.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = ManhwaPurple,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = ref.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = ref.role,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
