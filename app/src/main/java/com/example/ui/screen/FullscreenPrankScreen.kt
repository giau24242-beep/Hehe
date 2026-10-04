package com.example.ui.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manager.PrankManager
import com.example.model.CrackImpact
import com.example.model.CrackState
import com.example.model.RgbGlitchLine
import com.example.sensor.ShakeDetector
import com.example.ui.components.BrokenScreenCanvas
import com.example.util.CrackGenerator

@Composable
fun FullscreenPrankScreen(
    onExitPrank: () -> Unit
) {
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    var crackState by remember { mutableStateOf(CrackState()) }
    var showExitHint by remember { mutableStateOf(false) }

    // Intercept Back Press - double press to exit as requested by user
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            PrankManager.getVibrationManager(context).triggerDoubleBackHaptic()
            onExitPrank()
        } else {
            lastBackPressTime = currentTime
            showExitHint = true
            Toast.makeText(context, "Nhấn nút Back thêm một lần nữa để trở lại bình thường", Toast.LENGTH_SHORT).show()
        }
    }

    // Optional shake sensor
    val settings = PrankManager.settings.value
    LaunchedEffect(settings.shakeToCrackEnabled) {
        if (settings.shakeToCrackEnabled) {
            val detector = ShakeDetector(context) {
                val config = PrankManager.settings.value
                val newTaps = crackState.totalTapCount + 1
                if (config.soundEnabled) {
                    PrankManager.getSoundManager(context).playCrackSound(config.soundType, config.soundVolume)
                }
                if (config.vibrationEnabled) {
                    PrankManager.getVibrationManager(context).triggerVibration(config.vibrationIntensity)
                }
                val impact = CrackGenerator.createImpact(
                    touchX = 540f,
                    touchY = 1100f,
                    screenWidth = 1080f,
                    screenHeight = 2400f,
                    tapCount = newTaps,
                    existingNearbyCount = 0,
                    settings = config
                )
                val glitches = if (config.rgbGlitchEnabled) {
                    CrackGenerator.generateGlobalGlitchLines(1080f, 2400f, newTaps)
                } else emptyList()
                crackState = CrackState(
                    impacts = crackState.impacts + impact,
                    totalTapCount = newTaps,
                    globalGlitchLines = glitches,
                    isFlickering = config.flickerGlitchEnabled && (newTaps >= 5 && newTaps % 2 == 0)
                )
            }
            detector.start()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("fullscreen_prank_container")
    ) {
        val screenW = constraints.maxWidth.toFloat()
        val screenH = constraints.maxHeight.toFloat()

        // 1. Realistic Simulated Smartphone Home Screen Background
        SimulatedHomeScreenBackground()

        // 2. Touch detection layer: taps register exact (x, y) coordinates
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newTapCount = crackState.totalTapCount + 1
                        val config = PrankManager.settings.value

                        // Play sound
                        if (config.soundEnabled) {
                            PrankManager.getSoundManager(context).playCrackSound(
                                config.soundType,
                                config.soundVolume
                            )
                        }

                        // Play violent vibration
                        if (config.vibrationEnabled) {
                            PrankManager.getVibrationManager(context).triggerVibration(config.vibrationIntensity)
                        }

                        val nearbyCount = crackState.impacts.count {
                            val dx = it.touchX - offset.x
                            val dy = it.touchY - offset.y
                            (dx * dx + dy * dy) < 180f * 180f
                        }

                        val impact = CrackGenerator.createImpact(
                            touchX = offset.x,
                            touchY = offset.y,
                            screenWidth = screenW,
                            screenHeight = screenH,
                            tapCount = newTapCount,
                            existingNearbyCount = nearbyCount,
                            settings = config
                        )

                        val glitches = if (config.rgbGlitchEnabled) {
                            CrackGenerator.generateGlobalGlitchLines(screenW, screenH, newTapCount)
                        } else emptyList()

                        val isFlicker = config.flickerGlitchEnabled && (newTapCount >= 6 && newTapCount % 3 == 0)

                        crackState = CrackState(
                            impacts = crackState.impacts + impact,
                            totalTapCount = newTapCount,
                            globalGlitchLines = glitches,
                            isFlickering = isFlicker
                        )
                    }
                }
        )

        // 3. Realistic Broken Screen Canvas overlay on top
        BrokenScreenCanvas(crackState = crackState)

        // 4. Subtle emergency exit banner at the top
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nhấn Back 2 lần để thoát",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    IconButton(
                        onClick = onExitPrank,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Thoát troll",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showExitHint,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Bấm Back thêm 1 lần nữa để trở lại bình thường",
                    color = Color.Yellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Renders a convincing modern Android Home Screen wallpaper with widgets & app icons
 * so when friend touches it expecting it to launch an app, the screen shatters right under their finger!
 */
@Composable
private fun SimulatedHomeScreenBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Clock & Date Widget
            Text(
                text = "10:24",
                fontSize = 68.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.95f),
                letterSpacing = (-1).sp
            )
            Text(
                text = "Thứ Bảy, 4 tháng 10 • 29°C Nắng nhẹ",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.75f),
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Search Bar Widget
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "Tìm kiếm ứng dụng, danh bạ...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Grid of Fake App Icons (Friend tries to tap one of them!)
            val appsRow1 = listOf(
                FakeApp("Điện thoại", Icons.Default.Call, Color(0xFF4CAF50)),
                FakeApp("Tin nhắn", Icons.Default.Chat, Color(0xFF2196F3)),
                FakeApp("Ảnh", Icons.Default.PhotoLibrary, Color(0xFFFF9800)),
                FakeApp("Cài đặt", Icons.Default.Settings, Color(0xFF607D8B))
            )
            val appsRow2 = listOf(
                FakeApp("Camera", Icons.Default.CameraAlt, Color(0xFFE91E63)),
                FakeApp("Trình duyệt", Icons.Default.Language, Color(0xFF00BCD4)),
                FakeApp("Nhạc", Icons.Default.MusicNote, Color(0xFF9C27B0)),
                FakeApp("Tập tin", Icons.Default.Search, Color(0xFFFF5722))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                appsRow1.forEach { app ->
                    FakeAppIconItem(app)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                appsRow2.forEach { app ->
                    FakeAppIconItem(app)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

private data class FakeApp(val name: String, val icon: ImageVector, val color: Color)

@Composable
private fun FakeAppIconItem(app: FakeApp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(app.color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = app.icon,
                contentDescription = app.name,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = app.name,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
