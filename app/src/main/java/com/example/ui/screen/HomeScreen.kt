package com.example.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundType
import com.example.haptic.VibrationIntensity
import com.example.manager.PrankManager
import com.example.ui.components.BrokenScreenCanvas
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartFullscreenPrank: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val settings by PrankManager.settings.collectAsState()
    val testCrackState by PrankManager.testCrackState.collectAsState()
    val isOverlayRunning by PrankManager.isOverlayRunning.collectAsState()

    var hasOverlayPermission by remember { mutableStateOf(PrankManager.hasOverlayPermission(context)) }
    var countdownTimer by remember { mutableIntStateOf(0) }
    var isCountingDown by remember { mutableStateOf(false) }

    // Overlay Permission launcher
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasOverlayPermission = PrankManager.hasOverlayPermission(context)
        if (hasOverlayPermission) {
            Toast.makeText(context, "Đã cấp quyền lớp phủ thành công!", Toast.LENGTH_SHORT).show()
        }
    }

    // Check permission on resume
    LaunchedEffect(Unit) {
        hasOverlayPermission = PrankManager.hasOverlayPermission(context)
    }

    // Countdown logic for timer prank
    LaunchedEffect(isCountingDown, countdownTimer) {
        if (isCountingDown && countdownTimer > 0) {
            delay(1000)
            countdownTimer--
            if (countdownTimer == 0) {
                isCountingDown = false
                if (hasOverlayPermission) {
                    PrankManager.startOverlayService(context)
                    Toast.makeText(context, "Màn hình bể đã kích hoạt!", Toast.LENGTH_SHORT).show()
                } else {
                    onStartFullscreenPrank()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 36.dp)
    ) {
        // 1. Hero Header
        HeroHeader()

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Critical Instruction: How to Exit (Double Back)
        ExitTutorialCard()

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Primary Trigger Modes
        Text(
            text = "CHẾ ĐỘ TROLL BẠN BÈ",
            color = NeonCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Mode 1: System Overlay (Over other apps)
        SystemOverlayCard(
            isOverlayRunning = isOverlayRunning,
            hasPermission = hasOverlayPermission,
            onRequestPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    overlayPermissionLauncher.launch(intent)
                }
            },
            onToggleOverlay = {
                if (isOverlayRunning) {
                    PrankManager.stopOverlayService(context)
                } else {
                    if (hasOverlayPermission) {
                        PrankManager.startOverlayService(context)
                        Toast.makeText(
                            context,
                            "Lớp phủ đã bật! Chạm vào bất kỳ đâu trên màn hình để gây nứt.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mode 2: Fullscreen Direct Prank
        FullscreenPrankCard(
            onStartFullscreenPrank = onStartFullscreenPrank
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mode 3: Timer Delay Mode
        TimerPrankCard(
            isCountingDown = isCountingDown,
            remainingSeconds = countdownTimer,
            onStartTimer = { seconds ->
                countdownTimer = seconds
                isCountingDown = true
                Toast.makeText(
                    context,
                    "Sẽ kích hoạt sau $seconds giây! Hãy đưa máy cho bạn bè ngay.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onCancelTimer = {
                isCountingDown = false
                countdownTimer = 0
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Interactive Live Scratchpad & Damage Preview
        Text(
            text = "CHẠM THỬ NGHIỆM VẾT NỨT TẠI CHỖ",
            color = NeonMagenta,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        InteractiveTesterCard(
            crackState = testCrackState,
            onScreenTouched = { x, y, w, h ->
                PrankManager.addTestImpact(x, y, w, h, context)
            },
            onClear = {
                PrankManager.clearTestCracks()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Visual Damage Settings (Spiderweb, AMOLED ink bleed, RGB lines)
        Text(
            text = "TÙY CHỌN HIỆU ỨNG HỎNG MÀN HÌNH",
            color = NeonGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        VisualEffectsSettingsCard(
            settings = settings,
            onToggleSpiderweb = { PrankManager.toggleSpiderweb(it) },
            onToggleInkBleed = { PrankManager.toggleInkBleed(it) },
            onToggleRgbGlitch = { PrankManager.toggleRgbGlitch(it) },
            onToggleFlicker = { PrankManager.toggleFlicker(it) },
            onToggleShake = {
                PrankManager.updateSettings(settings.copy(shakeToCrackEnabled = it))
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Sound & Vibration Configuration
        Text(
            text = "ÂM THANH VỠ KÍNH & RUNG GIẬT MÌNH",
            color = WarningAmber,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        AudioAndHapticCard(
            settings = settings,
            onToggleSound = { PrankManager.toggleSound(it) },
            onSelectSoundType = { PrankManager.updateSoundType(it) },
            onTestSound = {
                PrankManager.getSoundManager(context).playCrackSound(settings.soundType, settings.soundVolume)
            },
            onToggleVibration = { PrankManager.toggleVibration(it) },
            onSelectVibrationIntensity = { PrankManager.updateVibrationIntensity(it) },
            onTestVibration = {
                PrankManager.getVibrationManager(context).triggerVibration(settings.vibrationIntensity)
            }
        )
    }
}

@Composable
private fun HeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF0B0F19)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(NeonCyan, NeonMagenta)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "BỂ MÀN HÌNH TROLL",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Nứt kính tại điểm chạm • Đốm mực • Sọc RGB",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ExitTutorialCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1B4B)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF4338CA))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6366F1).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFA5B4FC),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "CÁCH TRỞ LẠI BÌNH THƯỜNG",
                    color = Color(0xFFC7D2FE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Chỉ cần nhấn nút phím BACK 2 lần liên tục, màn hình sẽ lập tức phục hồi bình thường!",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun SystemOverlayCard(
    isOverlayRunning: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onToggleOverlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            if (isOverlayRunning) NeonGreen else DarkCardBorder
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isOverlayRunning) NeonGreen.copy(alpha = 0.15f) else NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = if (isOverlayRunning) NeonGreen else NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lớp phủ trên các ứng dụng khác",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isOverlayRunning) "Đang hoạt động trên mọi app" else "Xuất hiện trên TikTok, Facebook, Game...",
                        color = if (isOverlayRunning) NeonGreen else TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!hasPermission) {
                // Notice to grant permission
                Surface(
                    color = WarningAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cần quyền 'Hiển thị trên ứng dụng khác' để nứt màn hình ngoài app.",
                            color = WarningAmber,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("grant_overlay_permission_button")
                ) {
                    Text("Cấp Quyền Lớp Phủ Ngay", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onToggleOverlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOverlayRunning) DangerRed else NeonCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("toggle_overlay_prank_button")
                ) {
                    Icon(
                        imageVector = if (isOverlayRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOverlayRunning) "TẮT LỚP PHỦ TROLL" else "BẬT LỚP PHỦ TOÀN HỆ THỐNG",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FullscreenPrankCard(
    onStartFullscreenPrank: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonMagenta.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = null,
                    tint = NeonMagenta,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Troll Toàn Màn Hình Trực Tiếp",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Giả lập màn hình chính chân thực để đưa bạn bè",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onStartFullscreenPrank,
                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("start_fullscreen_prank_button")
            ) {
                Text("Bắt đầu", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TimerPrankCard(
    isCountingDown: Boolean,
    remainingSeconds: Int,
    onStartTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElectricBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hẹn Giờ Kích Hoạt (Timer)",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isCountingDown) "Sắp nổ sau $remainingSeconds giây!" else "Hẹn giờ rồi giả vờ đưa máy nhờ bạn bè bấm",
                        color = if (isCountingDown) WarningAmber else TextSecondary,
                        fontSize = 12.sp
                    )
                }

                if (isCountingDown) {
                    IconButton(onClick = onCancelTimer) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Hủy", tint = DangerRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15, 30).forEach { sec ->
                    OutlinedButton(
                        onClick = { onStartTimer(sec) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isCountingDown && remainingSeconds == sec) WarningAmber else TextPrimary
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isCountingDown && remainingSeconds == sec) WarningAmber else DarkCardBorder
                        )
                    ) {
                        Text("${sec}s", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveTesterCard(
    crackState: com.example.model.CrackState,
    onScreenTouched: (Float, Float, Float, Float) -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bàn Thử Nghiệm Tương Tác",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chạm nhiều lần liên tiếp để xem mực đen & sọc vỡ",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (crackState.totalTapCount > 0) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Xóa vết nứt",
                            tint = NeonCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulated Mini Phone Screen Container
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF070B12))
                    .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .testTag("interactive_test_canvas")
            ) {
                val w = constraints.maxWidth.toFloat()
                val h = constraints.maxHeight.toFloat()

                // Wallpaper preview
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF111827))
                            )
                        )
                )

                // Hint if no touches yet
                if (crackState.totalTapCount == 0) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = NeonCyan.copy(alpha = 0.7f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "CHẠM VÀO ĐÂY ĐỂ LÀM VỠ",
                            color = TextPrimary.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Càng chạm nhiều càng bể nặng và loang mực!",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Broken Screen Canvas
                BrokenScreenCanvas(crackState = crackState)

                // Touch detection layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                onScreenTouched(offset.x, offset.y, w, h)
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Damage Status Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (crackState.totalTapCount > 0) DangerRed else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Số lần va đập: ${crackState.totalTapCount}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                val damageText = when {
                    crackState.totalTapCount == 0 -> "Nguyên vẹn"
                    crackState.totalTapCount < 3 -> "Nứt mạng nhện nhẹ"
                    crackState.totalTapCount < 6 -> "Vỡ kính sâu"
                    crackState.totalTapCount < 10 -> "Loang mực đen AMOLED"
                    else -> "HỎNG TOÀN DIỆN & SỌC RGB"
                }

                val damageColor = when {
                    crackState.totalTapCount == 0 -> NeonGreen
                    crackState.totalTapCount < 3 -> NeonCyan
                    crackState.totalTapCount < 6 -> WarningAmber
                    crackState.totalTapCount < 10 -> NeonMagenta
                    else -> DangerRed
                }

                Text(
                    text = damageText,
                    color = damageColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun VisualEffectsSettingsCard(
    settings: com.example.model.PrankSettings,
    onToggleSpiderweb: (Boolean) -> Unit,
    onToggleInkBleed: (Boolean) -> Unit,
    onToggleRgbGlitch: (Boolean) -> Unit,
    onToggleFlicker: (Boolean) -> Unit,
    onToggleShake: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingSwitchRow(
                icon = Icons.Default.AutoAwesome,
                title = "Vết nứt kính mạng nhện",
                description = "Tia nứt góc nhọn chân thực từ điểm ngón tay chạm",
                checked = settings.spiderwebEnabled,
                onCheckedChange = onToggleSpiderweb,
                accentColor = NeonCyan
            )

            Spacer(modifier = Modifier.height(14.dp))

            SettingSwitchRow(
                icon = Icons.Default.FiberManualRecord,
                title = "Đốm mực đen AMOLED loang",
                description = "Chảy mực chết điểm ảnh đen sì càng chạm càng to",
                checked = settings.inkBleedEnabled,
                onCheckedChange = onToggleInkBleed,
                accentColor = NeonMagenta
            )

            Spacer(modifier = Modifier.height(14.dp))

            SettingSwitchRow(
                icon = Icons.Default.ColorLens,
                title = "Sọc màn hình nhiều màu (RGB)",
                description = "Chết dải pixel neon xanh lá, hồng, trắng rạch ngang dọc",
                checked = settings.rgbGlitchEnabled,
                onCheckedChange = onToggleRgbGlitch,
                accentColor = NeonGreen
            )

            Spacer(modifier = Modifier.height(14.dp))

            SettingSwitchRow(
                icon = Icons.Default.FlashOn,
                title = "Chớp nháy màn hình hỏng",
                description = "Đèn nền chập chờn khi bị nứt quá nặng",
                checked = settings.flickerGlitchEnabled,
                onCheckedChange = onToggleFlicker,
                accentColor = WarningAmber
            )

            Spacer(modifier = Modifier.height(14.dp))

            SettingSwitchRow(
                icon = Icons.Default.Sensors,
                title = "Cảm biến lắc để làm vỡ",
                description = "Lắc mạnh điện thoại sẽ tự động gây nứt vỡ",
                checked = settings.shakeToCrackEnabled,
                onCheckedChange = onToggleShake,
                accentColor = ElectricBlue
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AudioAndHapticCard(
    settings: com.example.model.PrankSettings,
    onToggleSound: (Boolean) -> Unit,
    onSelectSoundType: (SoundType) -> Unit,
    onTestSound: () -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onSelectVibrationIntensity: (VibrationIntensity) -> Unit,
    onTestVibration: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Sound section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarningAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Âm Thanh Vỡ Kính",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (settings.soundEnabled) {
                        IconButton(onClick = onTestSound) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Nghe thử âm thanh",
                                tint = WarningAmber
                            )
                        }
                    }
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = onToggleSound,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = WarningAmber
                        )
                    )
                }
            }

            if (settings.soundEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SoundType.values().forEach { st ->
                        FilterChip(
                            selected = settings.soundType == st,
                            onClick = { onSelectSoundType(st) },
                            label = { Text(st.title, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarningAmber,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vibration section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonMagenta.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = NeonMagenta,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Rung Giật Mình",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (settings.vibrationEnabled) {
                        IconButton(onClick = onTestVibration) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Thử rung",
                                tint = NeonMagenta
                            )
                        }
                    }
                    Switch(
                        checked = settings.vibrationEnabled,
                        onCheckedChange = onToggleVibration,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = NeonMagenta
                        )
                    )
                }
            }

            if (settings.vibrationEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VibrationIntensity.values().forEach { vi ->
                        FilterChip(
                            selected = settings.vibrationIntensity == vi,
                            onClick = { onSelectVibrationIntensity(vi) },
                            label = { Text(vi.title, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonMagenta,
                                selectedLabelColor = Color.White,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}
