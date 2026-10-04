package com.example.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.model.CrackImpact
import com.example.model.GlassShard
import com.example.model.InkBleedBlob
import com.example.model.PrankSettings
import com.example.model.RadialRay
import com.example.model.RgbGlitchLine
import com.example.model.SpiderwebRing
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object CrackGenerator {
    private val random = Random()

    private val GLITCH_COLORS = listOf(
        Color(0xFF00FF66), // Neon Green
        Color(0xFFFF007F), // Vivid Magenta
        Color(0xFF00F0FF), // Electric Cyan
        Color(0xFFFFFFFF), // Pure White dead line
        Color(0xFFFFE600), // Bright Yellow
        Color(0xFF8A2BE2), // Blue Violet
        Color(0xFFFF3300)  // Danger Red
    )

    fun createImpact(
        touchX: Float,
        touchY: Float,
        screenWidth: Float,
        screenHeight: Float,
        tapCount: Int,
        existingNearbyCount: Int,
        settings: PrankSettings
    ): CrackImpact {
        val severity = (tapCount / 2).coerceIn(1, 8) + existingNearbyCount

        // 1. Generate radial rays
        val rayCount = if (settings.spiderwebEnabled) (10 + random.nextInt(6) + severity) else 4
        val maxReach = (maxOf(screenWidth, screenHeight) * (0.35f + severity * 0.08f)).coerceAtMost(maxOf(screenWidth, screenHeight) * 1.2f)

        val rays = mutableListOf<RadialRay>()
        val rayInnerPoints = mutableListOf<MutableList<Offset>>()
        val angleStep = (2.0 * PI / rayCount)

        for (i in 0 until rayCount) {
            val baseAngle = i * angleStep + (random.nextDouble() - 0.5) * angleStep * 0.7
            val rayLength = maxReach * (0.45f + random.nextFloat() * 0.75f)
            val stepSize = 25f + random.nextFloat() * 20f
            val steps = (rayLength / stepSize).toInt().coerceAtLeast(3)

            val points = mutableListOf<Offset>()
            points.add(Offset(touchX, touchY))

            var currX = touchX
            var currY = touchY
            var currentAngle = baseAngle

            val branches = mutableListOf<List<Offset>>()
            val ringPointsForRay = mutableListOf<Offset>()

            for (step in 1..steps) {
                currentAngle += (random.nextDouble() - 0.5) * 0.35
                currX += (cos(currentAngle) * stepSize).toFloat()
                currY += (sin(currentAngle) * stepSize).toFloat()
                val pt = Offset(currX, currY)
                points.add(pt)

                if (step in 1..6) {
                    ringPointsForRay.add(pt)
                }

                // Random branch generation
                if (step == steps / 3 || step == (steps * 2) / 3) {
                    if (random.nextBoolean()) {
                        val branchPoints = mutableListOf<Offset>()
                        branchPoints.add(pt)
                        var bX = currX
                        var bY = currY
                        var bAngle = currentAngle + if (random.nextBoolean()) 0.55 else -0.55
                        val bSteps = (steps - step) / 2 + 2
                        for (bs in 1..bSteps) {
                            bAngle += (random.nextDouble() - 0.5) * 0.4
                            bX += (cos(bAngle) * (stepSize * 0.85f)).toFloat()
                            bY += (sin(bAngle) * (stepSize * 0.85f)).toFloat()
                            branchPoints.add(Offset(bX, bY))
                        }
                        branches.add(branchPoints)
                    }
                }
            }
            rays.add(RadialRay(points, branches, strokeWidth = (2.2f + random.nextFloat() * 1.5f)))
            rayInnerPoints.add(ringPointsForRay)
        }

        // 2. Spiderweb concentric rings connecting adjacent rays
        val rings = mutableListOf<SpiderwebRing>()
        if (settings.spiderwebEnabled) {
            val maxRings = (3 + severity).coerceAtMost(7)
            for (r in 0 until maxRings) {
                val ringPts = mutableListOf<Offset>()
                for (rayIdx in 0 until rayCount) {
                    val pts = rayInnerPoints[rayIdx]
                    if (r < pts.size) {
                        val original = pts[r]
                        // Add jitter to make ring look fractured
                        val jx = original.x + (random.nextFloat() - 0.5f) * 12f
                        val jy = original.y + (random.nextFloat() - 0.5f) * 12f
                        ringPts.add(Offset(jx, jy))
                    }
                }
                if (ringPts.size >= 3) {
                    // close the loop
                    ringPts.add(ringPts[0])
                    rings.add(SpiderwebRing(ringPts, strokeWidth = (1.5f + random.nextFloat() * 0.8f)))
                }
            }
        }

        // 3. Realistic glass shards around epicenter
        val shards = mutableListOf<GlassShard>()
        if (settings.spiderwebEnabled) {
            val shardCount = (6 + severity * 2).coerceAtMost(16)
            for (s in 0 until shardCount) {
                val a1 = random.nextDouble() * 2 * PI
                val a2 = a1 + (0.2 + random.nextDouble() * 0.35)
                val dist1 = 15f + random.nextFloat() * (40f + severity * 10f)
                val dist2 = 25f + random.nextFloat() * (50f + severity * 12f)

                val v0 = Offset(touchX, touchY)
                val v1 = Offset((touchX + cos(a1) * dist1).toFloat(), (touchY + sin(a1) * dist1).toFloat())
                val v2 = Offset((touchX + cos(a2) * dist2).toFloat(), (touchY + sin(a2) * dist2).toFloat())

                val alpha = (0.25f + random.nextFloat() * 0.35f)
                shards.add(
                    GlassShard(
                        vertices = listOf(v0, v1, v2),
                        fillColor = Color(0xFFE0F2FE).copy(alpha = alpha),
                        outlineColor = Color(0xFFFFFFFF).copy(alpha = 0.85f)
                    )
                )
            }
        }

        // 4. AMOLED / LCD Ink Bleed ("đốm đen cả mực")
        val inkBlobs = mutableListOf<InkBleedBlob>()
        if (settings.inkBleedEnabled && tapCount >= 2) {
            val blobCount = (1 + (tapCount - 2) / 2).coerceAtMost(4)
            for (b in 0 until blobCount) {
                val offsetX = (random.nextFloat() - 0.5f) * (30f + severity * 8f)
                val offsetY = (random.nextFloat() - 0.5f) * (30f + severity * 8f)
                val center = Offset(touchX + offsetX, touchY + offsetY)

                val baseRadius = (35f + (tapCount * 12f) + random.nextFloat() * 25f).coerceAtMost(220f)
                val points = mutableListOf<Offset>()
                val numVertices = 16

                for (v in 0 until numVertices) {
                    val angle = v * (2 * PI / numVertices)
                    // Perlin-like organic amoeba jitter
                    val radiusVariation = baseRadius * (0.65f + 0.55f * sin(angle * 3.0 + b).toFloat() + (random.nextFloat() - 0.5f) * 0.3f)
                    val px = center.x + (cos(angle) * radiusVariation).toFloat()
                    val py = center.y + (sin(angle) * radiusVariation).toFloat()
                    points.add(Offset(px, py))
                }

                inkBlobs.add(
                    InkBleedBlob(
                        center = center,
                        radius = baseRadius,
                        contourPoints = points,
                        haloColor = if (random.nextBoolean()) Color(0xAA4A0E4E) else Color(0xAA0D47A1)
                    )
                )
            }
        }

        // 5. Broken Screen RGB Glitch Lines ("sọc nhiều màu")
        val glitchLines = mutableListOf<RgbGlitchLine>()
        if (settings.rgbGlitchEnabled && tapCount >= 3) {
            val lineCount = (2 + (tapCount - 3) * 2).coerceAtMost(10)
            for (gl in 0 until lineCount) {
                val isVertical = random.nextFloat() > 0.35f
                val color = GLITCH_COLORS[random.nextInt(GLITCH_COLORS.size)]
                val strokeWidth = (1.5f + random.nextFloat() * 5.0f + (if (random.nextFloat() < 0.2f) 8f else 0f))

                if (isVertical) {
                    // Vertical lines passing through or near touch point
                    val x = touchX + (random.nextFloat() - 0.5f) * (80f + severity * 20f)
                    glitchLines.add(
                        RgbGlitchLine(
                            isVertical = true,
                            position = x.coerceIn(0f, screenWidth),
                            lengthRatio = 1.0f,
                            offsetRatio = 0.0f,
                            strokeWidth = strokeWidth,
                            color = color,
                            alpha = 0.85f + random.nextFloat() * 0.15f
                        )
                    )
                } else {
                    // Horizontal dead scanline
                    val y = touchY + (random.nextFloat() - 0.5f) * (80f + severity * 20f)
                    glitchLines.add(
                        RgbGlitchLine(
                            isVertical = false,
                            position = y.coerceIn(0f, screenHeight),
                            lengthRatio = 0.5f + random.nextFloat() * 0.5f,
                            offsetRatio = random.nextFloat() * 0.5f,
                            strokeWidth = strokeWidth * 0.8f,
                            color = color,
                            alpha = 0.8f + random.nextFloat() * 0.2f
                        )
                    )
                }
            }
        }

        return CrackImpact(
            touchX = touchX,
            touchY = touchY,
            severityLevel = severity,
            radialRays = rays,
            webRings = rings,
            glassShards = shards,
            inkBlobs = inkBlobs,
            glitchLines = glitchLines
        )
    }

    /**
     * Generates persistent global screen glitch artifacts when screen damage is heavy.
     */
    fun generateGlobalGlitchLines(screenWidth: Float, screenHeight: Float, totalTaps: Int): List<RgbGlitchLine> {
        if (totalTaps < 4) return emptyList()
        val count = ((totalTaps - 3) * 2).coerceAtMost(14)
        val list = mutableListOf<RgbGlitchLine>()

        for (i in 0 until count) {
            val isVertical = random.nextFloat() > 0.25f
            val color = GLITCH_COLORS[random.nextInt(GLITCH_COLORS.size)]
            val strokeWidth = 1f + random.nextFloat() * 4f

            if (isVertical) {
                list.add(
                    RgbGlitchLine(
                        isVertical = true,
                        position = random.nextFloat() * screenWidth,
                        lengthRatio = 1f,
                        offsetRatio = 0f,
                        strokeWidth = strokeWidth,
                        color = color,
                        alpha = 0.75f + random.nextFloat() * 0.25f
                    )
                )
            } else {
                list.add(
                    RgbGlitchLine(
                        isVertical = false,
                        position = random.nextFloat() * screenHeight,
                        lengthRatio = 0.4f + random.nextFloat() * 0.6f,
                        offsetRatio = random.nextFloat() * 0.5f,
                        strokeWidth = strokeWidth,
                        color = color,
                        alpha = 0.7f + random.nextFloat() * 0.3f
                    )
                )
            }
        }
        return list
    }
}
