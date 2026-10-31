
package com.finanquest.sv

import android.os.Bundle
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.finanquest.sv.navigation.AppNavigation
import com.finanquest.sv.ui.theme.FinanQuestSVTheme
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val preferences = remember {
                getSharedPreferences("finanquest_preferences", MODE_PRIVATE)
            }
            var darkMode by remember {
                mutableStateOf(preferences.getBoolean("dark_mode", false))
            }

            SideEffect {
                window.statusBarColor = AndroidColor.TRANSPARENT
                window.navigationBarColor = AndroidColor.TRANSPARENT
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkMode
                    isAppearanceLightNavigationBars = !darkMode
                }
            }

            FinanQuestSVTheme(darkTheme = darkMode) {
                AppNavigation(
                    modoOscuro = darkMode,
                    onCambiarModoOscuro = { enabled ->
                        darkMode = enabled
                        preferences.edit().putBoolean("dark_mode", enabled).apply()
                    }
                )
            }
        }
    }
}
