package com.tany.lecta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tany.lecta.ui.theme.LectaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()

        AppSettings.load(this)

        setContent {
            LectaTheme {
                LectaAppTheme {
                    AppRoot()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            if (insets.isVisible(WindowInsetsCompat.Type.statusBars()) ||
                insets.isVisible(WindowInsetsCompat.Type.navigationBars())
            ) {
                view.post { hideSystemBars() }
            }
            ViewCompat.onApplyWindowInsets(view, insets)
        }
    }

    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }
}