package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.CrackImpact
import com.example.model.CrackState
import com.example.model.RgbGlitchLine

@Composable
fun BrokenScreenCanvas(
    crackState: CrackState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 1. Draw each physical crack impact
        for (impact in crackState.impacts) {
            drawImpact(impact)
        }

        // 2. Draw global RGB glitch lines (dead pixel lines stretching across screen)
        for (glitch in crackState.globalGlitchLines) {
            drawGlitchLine(glitch, canvasWidth, canvasHeight)
        }

        // 3. Draw screen flicker / dead backlight artifact if heavily damaged
        if (crackState.isFlickering) {
            drawRect(
                color = Color.Black.copy(alpha = 0.25f),
                size = size
            )
        }
    }
}

private fun DrawScope.drawImpact(impact: CrackImpact) {
    // 1. Draw AMOLED / LCD Ink Bleed Blobs first (so glass crack lines overlay on top!)
    for (blob in impact.inkBlobs) {
        if (blob.contourPoints.size >= 3) {
            val path = Path().apply {
                moveTo(blob.contourPoints[0].x, blob.contourPoints[0].y)
                for (i in 1 until blob.contourPoints.size) {
                    lineTo(blob.contourPoints[i].x, blob.contourPoints[i].y)
                }
                close()
            }

            // A. Colored liquid crystal bleed glow around the edges
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black,
                        blob.haloColor,
                        Color.Transparent
                    ),
                    center = blob.center,
                    radius = blob.radius * 1.35f
                ),
                radius = blob.radius * 1.35f,
                center = blob.center
            )

            // B. Pure pitch-black ink core ("đốm đen cả mực")
            drawPath(path = path, color = Color(0xFF000000), style = Fill)

            // C. Inky edge line with subtle purple
            drawPath(
                path = path,
                color = blob.haloColor.copy(alpha = 0.8f),
                style = Stroke(width = 3f)
            )
        }
    }

    // 2. Draw Glass Shards (specular shards)
    for (shard in impact.glassShards) {
        if (shard.vertices.size >= 3) {
            val path = Path().apply {
                moveTo(shard.vertices[0].x, shard.vertices[0].y)
                for (v in 1 until shard.vertices.size) {
                    lineTo(shard.vertices[v].x, shard.vertices[v].y)
                }
                close()
            }
            drawPath(path = path, color = shard.fillColor, style = Fill)
            drawPath(path = path, color = shard.outlineColor, style = Stroke(width = 1.2f))
        }
    }

    // 3. Draw Radial Rays (3-Layer Realistic Glass Fracture)
    for (ray in impact.radialRays) {
        drawRealisticLinePath(ray.points, ray.strokeWidth)
        for (branch in ray.branchPaths) {
            drawRealisticLinePath(branch, ray.strokeWidth * 0.75f)
        }
    }

    // 4. Draw Spiderweb Concentric Rings
    for (ring in impact.webRings) {
        drawRealisticLinePath(ring.points, ring.strokeWidth)
    }

    // 5. Draw Local RGB Glitch lines near this impact
    for (glitch in impact.glitchLines) {
        drawGlitchLine(glitch, size.width, size.height)
    }

    // 6. Draw central micro-impact crush point (shattered white glass center)
    drawCircle(
        color = Color(0x66FFFFFF),
        radius = 8f + impact.severityLevel * 2f,
        center = Offset(impact.touchX, impact.touchY)
    )
    drawCircle(
        color = Color.White,
        radius = 3.5f,
        center = Offset(impact.touchX, impact.touchY)
    )
}

/**
 * Draws realistic 3-layer glass fracture lines:
 * - 1st layer: Dark glass refraction shadow (depth)
 * - 2nd layer: Ice-blue glass edge reflection
 * - 3rd layer: Brilliant specular white core crack
 */
private fun DrawScope.drawRealisticLinePath(points: List<Offset>, strokeWidth: Float) {
    if (points.size < 2) return

    val shadowPath = Path().apply {
        moveTo(points[0].x + 1.2f, points[0].y + 1.2f)
        for (i in 1 until points.size) {
            lineTo(points[i].x + 1.2f, points[i].y + 1.2f)
        }
    }

    val mainPath = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }

    // Layer 1: Dark refraction shadow (gives glass physical depth)
    drawPath(
        path = shadowPath,
        color = Color(0x99050B14),
        style = Stroke(
            width = strokeWidth * 2.2f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Layer 2: Ice-blue translucent refraction tint
    drawPath(
        path = mainPath,
        color = Color(0xCCBAE6FD), // Light cyan ice blue
        style = Stroke(
            width = strokeWidth * 1.4f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Layer 3: Brilliant specular white fracture highlight
    drawPath(
        path = mainPath,
        color = Color(0xFFFFFFFF),
        style = Stroke(
            width = strokeWidth * 0.7f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Draws dead pixel scanlines and RGB glitch bars.
 */
private fun DrawScope.drawGlitchLine(
    glitch: RgbGlitchLine,
    canvasWidth: Float,
    canvasHeight: Float
) {
    if (glitch.isVertical) {
        val startY = canvasHeight * glitch.offsetRatio
        val endY = startY + canvasHeight * glitch.lengthRatio
        drawLine(
            color = glitch.color.copy(alpha = glitch.alpha),
            start = Offset(glitch.position, startY),
            end = Offset(glitch.position, endY),
            strokeWidth = glitch.strokeWidth,
            cap = StrokeCap.Square
        )
    } else {
        val startX = canvasWidth * glitch.offsetRatio
        val endX = startX + canvasWidth * glitch.lengthRatio
        drawLine(
            color = glitch.color.copy(alpha = glitch.alpha),
            start = Offset(startX, glitch.position),
            end = Offset(endX, glitch.position),
            strokeWidth = glitch.strokeWidth,
            cap = StrokeCap.Square
        )
    }
}
