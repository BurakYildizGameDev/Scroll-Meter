package com.scrollmeter.app.presentation.components

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Global LRU memory cache for application icons to avoid repetitive IPC Binder queries.
 */
object AppIconCache {
    private val iconCache = LruCache<String, ImageBitmap>(150)

    fun get(packageName: String): ImageBitmap? = iconCache.get(packageName)

    fun put(packageName: String, bitmap: ImageBitmap) {
        iconCache.put(packageName, bitmap)
    }
}

/**
 * High-performance App Icon component that fetches real Android app icons with asynchronous IO.
 */
@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    contentDescription: String? = null
) {
    val context = LocalContext.current

    val imageBitmapState = produceState<ImageBitmap?>(
        initialValue = AppIconCache.get(packageName),
        key1 = packageName
    ) {
        if (value == null && packageName.isNotBlank()) {
            val bitmap = withContext(Dispatchers.IO) {
                loadAppIcon(context, packageName)
            }
            if (bitmap != null) {
                AppIconCache.put(packageName, bitmap)
                value = bitmap
            }
        }
    }

    val bitmap = imageBitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
        )
    } else {
        val fallbackInfo = remember(packageName) { getFallbackBrandInfo(packageName) }
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .background(fallbackInfo.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fallbackInfo.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp
                ),
                color = fallbackInfo.textColor
            )
        }
    }
}

private data class FallbackBrand(
    val label: String,
    val backgroundColor: Color,
    val textColor: Color = Color.White
)

private fun getFallbackBrandInfo(packageName: String): FallbackBrand {
    return when {
        packageName.contains("instagram", ignoreCase = true) ->
            FallbackBrand("IG", Color(0xFFE1306C))
        packageName.contains("whatsapp", ignoreCase = true) ->
            FallbackBrand("WA", Color(0xFF25D366))
        packageName.contains("twitter", ignoreCase = true) || packageName.contains(".x", ignoreCase = true) ->
            FallbackBrand("𝕏", Color(0xFF0F1419))
        packageName.contains("chrome", ignoreCase = true) ->
            FallbackBrand("CH", Color(0xFF0F9D58))
        packageName.contains("youtube", ignoreCase = true) ->
            FallbackBrand("YT", Color(0xFFFF0000))
        packageName.contains("tiktok", ignoreCase = true) || packageName.contains("musically", ignoreCase = true) ->
            FallbackBrand("TT", Color(0xFF010101))
        packageName.contains("github", ignoreCase = true) ->
            FallbackBrand("GH", Color(0xFF24292E))
        packageName.contains("reddit", ignoreCase = true) ->
            FallbackBrand("RD", Color(0xFFFF4500))
        packageName.contains("spotify", ignoreCase = true) ->
            FallbackBrand("SP", Color(0xFF1DB954))
        packageName.contains("okey", ignoreCase = true) || packageName.contains("yuzbir", ignoreCase = true) ->
            FallbackBrand("101", Color(0xFF0284C7))
        else -> {
            val char = packageName.substringAfterLast('.').firstOrNull()?.uppercaseChar()?.toString() ?: "•"
            val hue = (Math.abs(packageName.hashCode()) % 360).toFloat()
            FallbackBrand(char, Color.hsl(hue, 0.45f, 0.35f))
        }
    }
}

private fun loadAppIcon(context: Context, packageName: String): ImageBitmap? {
    if (packageName.isBlank()) return null
    return try {
        val pm = context.packageManager
        val drawable = pm.getApplicationIcon(packageName)
        drawableToBitmap(drawable)?.asImageBitmap()
    } catch (_: PackageManager.NameNotFoundException) {
        null
    } catch (_: Exception) {
        null
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap? {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
