package com.barath.drivexhmi

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.barath.drivexhmi.ui.screens.DashboardScreen
import com.barath.drivexhmi.ui.theme.CarbonDark
import com.barath.drivexhmi.ui.theme.DriveXHMITheme

/**
 * Main Activity hosting the landscape HMI digital instrument cluster.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock landscape orientation for automotive cluster display
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // Prevent screen dimming while dashboard is running (critical for EV clusters)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enable edge-to-edge immersive experience
        enableEdgeToEdge()
        hideSystemBars()

        setContent {
            DriveXHMITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CarbonDark
                ) {
                    DashboardScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
    }
}
