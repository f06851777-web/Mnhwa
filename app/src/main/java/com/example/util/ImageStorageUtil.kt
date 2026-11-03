package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.random.Random

object ImageStorageUtil {

    fun saveBase64Image(context: Context, base64Data: String, fileName: String): String? {
        return try {
            val cleanBase64 = if (base64Data.contains(",")) {
                base64Data.substringAfter(",")
            } else {
                base64Data
            }
            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            val dir = File(context.filesDir, "manhwa_images").apply { if (!exists()) mkdirs() }
            val file = File(dir, "$fileName.png")
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String): String? {
        return try {
            val dir = File(context.filesDir, "manhwa_images").apply { if (!exists()) mkdirs() }
            val file = File(dir, "$fileName.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
        return try {
            val dir = File(context.filesDir, "manhwa_images").apply { if (!exists()) mkdirs() }
            val file = File(dir, "$fileName.png")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bytes = input.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun filePathToBase64(filePath: String): String? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a high-quality stylized Manhwa/Anime graphic canvas bitmap
     * with dramatic perspective, speed lines, glowing aura, and comic framing.
     */
    fun createStylizedManhwaBitmap(
        sceneIndex: Int,
        mood: String,
        artStyle: String,
        prompt: String,
        aspectRatio: String = "16:9"
    ): Bitmap {
        val (width, height) = when (aspectRatio) {
            "9:16" -> 720 to 1280
            "1:1" -> 900 to 900
            else -> 1280 to 720
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Mood-based color schemes
        val (cTop, cMid, cBottom, auraColor) = when (mood.lowercase()) {
            "epic", "action", "climax" -> listOf(
                0xFF1A0B2E.toInt(), // Deep Void Purple
                0xFF3B0764.toInt(), // Intense Violet
                0xFF0F172A.toInt(), // Dark Obsidian
                0xFF9333EA.toInt()  // Electric Purple
            )
            "mystery", "dark", "suspense" -> listOf(
                0xFF020617.toInt(), // Midnight Navy
                0xFF0F172A.toInt(), // Deep Slate
                0xFF1E293B.toInt(), // Twilight Cyan
                0xFF06B6D4.toInt()  // Cyan Pulse
            )
            "emotional", "sad", "dramatic" -> listOf(
                0xFF18181B.toInt(),
                0xFF27272A.toInt(),
                0xFF3F3F46.toInt(),
                0xFFF43F5E.toInt()  // Crimson Glow
            )
            "bright", "triumph", "hope" -> listOf(
                0xFF1E1B4B.toInt(),
                0xFF312E81.toInt(),
                0xFF4338CA.toInt(),
                0xFFF59E0B.toInt()  // Golden Amber
            )
            else -> listOf(
                0xFF0F172A.toInt(),
                0xFF1E1B4B.toInt(),
                0xFF2E1065.toInt(),
                0xFF8B5CF6.toInt()
            )
        }

        // Draw atmospheric gradient background
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(cTop, cMid, cBottom),
                floatArrayOf(0.0f, 0.55f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw Manhwa dynamic action rays / speed lines
        val linePaint = Paint().apply {
            color = auraColor
            strokeWidth = 2.5f
            alpha = 45
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val centerX = width * 0.5f
        val centerY = height * 0.45f
        val random = Random(sceneIndex * 31 + mood.hashCode())

        for (i in 0 until 40) {
            val angle = (i * (360.0 / 40.0) + random.nextInt(6)).toDouble()
            val rad = Math.toRadians(angle)
            val distInner = random.nextInt(60, 160).toFloat()
            val distOuter = width.toFloat() * 1.2f
            val startX = (centerX + Math.cos(rad) * distInner).toFloat()
            val startY = (centerY + Math.sin(rad) * distInner).toFloat()
            val endX = (centerX + Math.cos(rad) * distOuter).toFloat()
            val endY = (centerY + Math.sin(rad) * distOuter).toFloat()
            canvas.drawLine(startX, startY, endX, endY, linePaint)
        }

        // Glowing center portal / focal aura
        val auraPaint = Paint().apply {
            isAntiAlias = true
            color = auraColor
            alpha = 75
        }
        canvas.drawCircle(centerX, centerY, width * 0.22f, auraPaint)
        auraPaint.alpha = 110
        canvas.drawCircle(centerX, centerY, width * 0.12f, auraPaint)

        // Comic silhouette / hero character figure in manhwa style
        val heroPaint = Paint().apply {
            color = 0xFF050814.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val heroPath = Path().apply {
            // Stylized anime/manhwa character silhouette
            val hx = centerX
            val hy = centerY + (height * 0.08f)
            val scale = height * 0.0016f

            moveTo(hx - 25 * scale, hy + 220 * scale)
            lineTo(hx - 40 * scale, hy + 110 * scale)
            lineTo(hx - 60 * scale, hy + 50 * scale)
            lineTo(hx - 20 * scale, hy - 40 * scale)
            // Head & hair spikes
            lineTo(hx - 30 * scale, hy - 90 * scale)
            lineTo(hx - 15 * scale, hy - 110 * scale)
            lineTo(hx, hy - 130 * scale)
            lineTo(hx + 15 * scale, hy - 110 * scale)
            lineTo(hx + 30 * scale, hy - 90 * scale)
            lineTo(hx + 20 * scale, hy - 40 * scale)
            lineTo(hx + 60 * scale, hy + 50 * scale)
            lineTo(hx + 40 * scale, hy + 110 * scale)
            lineTo(hx + 25 * scale, hy + 220 * scale)
            close()
        }
        canvas.drawPath(heroPath, heroPaint)

        // Glowing eyes / energy particles
        val eyePaint = Paint().apply {
            color = 0xFF00F5FF.toInt() // Cyan glow
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val scale = height * 0.0016f
        canvas.drawCircle(centerX - 8 * scale, centerY + (height * 0.08f) - 75 * scale, 3.5f * scale, eyePaint)
        canvas.drawCircle(centerX + 8 * scale, centerY + (height * 0.08f) - 75 * scale, 3.5f * scale, eyePaint)

        // Manhwa panel overlay border
        val borderPaint = Paint().apply {
            color = 0xFF2E384D.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 8f
            isAntiAlias = true
        }
        val rect = RectF(16f, 16f, width - 16f, height - 16f)
        canvas.drawRoundRect(rect, 20f, 20f, borderPaint)

        // Corner accents
        val accentPaint = Paint().apply {
            color = auraColor
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawLine(16f, 50f, 16f, 16f, accentPaint)
        canvas.drawLine(16f, 16f, 50f, 16f, accentPaint)
        canvas.drawLine(width - 50f, 16f, width - 16f, 16f, accentPaint)
        canvas.drawLine(width - 16f, 16f, width - 16f, 50f, accentPaint)
        canvas.drawLine(16f, height - 50f, 16f, height - 16f, accentPaint)
        canvas.drawLine(16f, height - 16f, 50f, height - 16f, accentPaint)
        canvas.drawLine(width - 50f, height - 16f, width - 16f, height - 16f, accentPaint)
        canvas.drawLine(width - 16f, height - 16f, width - 16f, height - 50f, accentPaint)

        // Watermark / Badge: Scene and Style
        val textPaint = Paint().apply {
            color = 0xFFE2E8F0.toInt()
            textSize = 28f
            isAntiAlias = true
            isFakeBoldText = true
        }
        canvas.drawText("SCENE #$sceneIndex • $mood.uppercase()", 36f, 56f, textPaint)

        val stylePaint = Paint().apply {
            color = 0xFF94A3B8.toInt()
            textSize = 22f
            isAntiAlias = true
        }
        canvas.drawText("$artStyle", 36f, 88f, stylePaint)

        return bitmap
    }
}
