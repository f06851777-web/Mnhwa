package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.ui.AppStep
import com.example.ui.theme.ManhwaEmerald
import com.example.ui.theme.ManhwaPurple

@Composable
fun StepNavigationStrip(
    currentStep: AppStep,
    isSetupComplete: Boolean,
    isScriptComplete: Boolean,
    isScenesComplete: Boolean,
    isVoiceoverComplete: Boolean,
    onStepSelected: (AppStep) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("step_nav_strip")
    ) {
        val scrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepPill(
                step = AppStep.SETUP,
                currentStep = currentStep,
                isCompleted = isSetupComplete,
                icon = Icons.Default.Tune,
                onClick = { onStepSelected(AppStep.SETUP) }
            )
            StepPill(
                step = AppStep.SCRIPT,
                currentStep = currentStep,
                isCompleted = isScriptComplete,
                icon = Icons.Default.EditNote,
                onClick = { onStepSelected(AppStep.SCRIPT) }
            )
            StepPill(
                step = AppStep.SCENES,
                currentStep = currentStep,
                isCompleted = isScenesComplete,
                icon = Icons.Default.GridView,
                onClick = { onStepSelected(AppStep.SCENES) }
            )
            StepPill(
                step = AppStep.VOICEOVER,
                currentStep = currentStep,
                isCompleted = isVoiceoverComplete,
                icon = Icons.Default.GraphicEq,
                onClick = { onStepSelected(AppStep.VOICEOVER) }
            )
            StepPill(
                step = AppStep.PREVIEW,
                currentStep = currentStep,
                isCompleted = isScenesComplete,
                icon = Icons.Default.PlayCircle,
                onClick = { onStepSelected(AppStep.PREVIEW) }
            )
            StepPill(
                step = AppStep.EXPORT,
                currentStep = currentStep,
                isCompleted = false,
                icon = Icons.Default.Download,
                onClick = { onStepSelected(AppStep.EXPORT) }
            )
        }
    }
}

@Composable
private fun StepPill(
    step: AppStep,
    currentStep: AppStep,
    isCompleted: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val isSelected = step == currentStep

    val bg = when {
        isSelected -> ManhwaPurple
        isCompleted -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }

    val contentColor = when {
        isSelected -> Color.White
        isCompleted -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(
                width = 1.dp,
                color = if (isSelected) ManhwaPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step status badge
        if (isCompleted && !isSelected) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(ManhwaEmerald),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = step.title,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "${step.stepNumber}. ${step.title}",
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}
