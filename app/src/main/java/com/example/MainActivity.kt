package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.manager.PrankManager
import com.example.ui.screen.FullscreenPrankScreen
import com.example.ui.screen.HomeScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    HOME,
    FULLSCREEN_PRANK
}

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Permission result handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        PrankManager.initialize(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(ScreenState.HOME) }

                when (currentScreen) {
                    ScreenState.HOME -> {
                        Scaffold(
                            modifier = Modifier.fillMaxSize()
                        ) { innerPadding ->
                            HomeScreen(
                                onStartFullscreenPrank = {
                                    currentScreen = ScreenState.FULLSCREEN_PRANK
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    ScreenState.FULLSCREEN_PRANK -> {
                        FullscreenPrankScreen(
                            onExitPrank = {
                                currentScreen = ScreenState.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}
