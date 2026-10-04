package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.audio.SoundType
import com.example.haptic.VibrationIntensity

/**
 * A single jagged ray path radiating out from an impact point.
 */
data class RadialRay(
    val points: List<Offset>,
    val branchPaths: List<List<Offset>> = emptyList(),
    val strokeWidth: Float = 2.5f
)

/**
 * A spiderweb polygon segment connecting adjacent rays.
 */
data class SpiderwebRing(
    val points: List<Offset>,
    val strokeWidth: Float = 1.8f
)

/**
 * Little specular glass shard polygon near the point of impact.
 */
data class GlassShard(
    val vertices: List<Offset>,
    val fillColor: Color,
    val outlineColor: Color
)

/**
 * AMOLED / LCD Ink Bleed blob ("đốm đen cả mực").
 * Organic amoeba-shaped black blob with purple/cyan bleeding rim.
 */
data class InkBleedBlob(
    val center: Offset,
    val radius: Float,
    val contourPoints: List<Offset>,
    val haloColor: Color = Color(0x994A0E4E) // Deep purple/violet liquid crystal glow
)

/**
 * Colorful broken screen glitch lines ("sọc nhiều màu").
 */
data class RgbGlitchLine(
    val isVertical: Boolean,
    val position: Float,
    val lengthRatio: Float = 1.0f,
    val offsetRatio: Float = 0.0f,
    val strokeWidth: Float,
    val color: Color,
    val alpha: Float = 0.9f
)

/**
 * Represents a single physical impact at screen coordinates (x, y).
 */
data class CrackImpact(
    val id: Long = System.currentTimeMillis(),
    val touchX: Float,
    val touchY: Float,
    val severityLevel: Int, // 1 to 5+
    val radialRays: List<RadialRay>,
    val webRings: List<SpiderwebRing>,
    val glassShards: List<GlassShard>,
    val inkBlobs: List<InkBleedBlob> = emptyList(),
    val glitchLines: List<RgbGlitchLine> = emptyList()
)

/**
 * Overall Broken Screen state across the device.
 */
data class CrackState(
    val impacts: List<CrackImpact> = emptyList(),
    val totalTapCount: Int = 0,
    val globalGlitchLines: List<RgbGlitchLine> = emptyList(),
    val isFlickering: Boolean = false
)

/**
 * App / Prank configuration settings.
 */
data class PrankSettings(
    val soundEnabled: Boolean = true,
    val soundType: SoundType = SoundType.HEAVY_SHATTER,
    val soundVolume: Float = 1.0f,
    val vibrationEnabled: Boolean = true,
    val vibrationIntensity: VibrationIntensity = VibrationIntensity.TERRIFYING,
    val spiderwebEnabled: Boolean = true,
    val inkBleedEnabled: Boolean = true,
    val rgbGlitchEnabled: Boolean = true,
    val flickerGlitchEnabled: Boolean = true,
    val delaySeconds: Int = 0, // 0 = instant, 5, 10, 15, 30
    val shakeToCrackEnabled: Boolean = false
)
