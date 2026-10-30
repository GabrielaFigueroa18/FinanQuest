package com.finanquest.sv.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
internal fun QuestVisualTheme(content: @Composable () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    val scheme =
        if (dark)
            darkColorScheme(
                primary = Color(0xFF70E0B1),
                secondary = Color(0xFFC9B5FF),
                background = Color(0xFF11141F),
                surface = Color(0xFF1C2131),
                onSurface = Color(0xFFF0F2FA),
                outline = Color(0xFF485064),
            )
        else
            lightColorScheme(
                primary = Color(0xFF137B54),
                onPrimary = Color.White,
                secondary = Color(0xFF7044C4),
                background = Color(0xFFF5F6FB),
                surface = Color.White,
                onSurface = Color(0xFF20263D),
                onSurfaceVariant = Color(0xFF626A80),
                outline = Color(0xFFDDE1EB),
                surfaceVariant = Color(0xFFEDE9FA),
            )
    MaterialTheme(
        colorScheme = scheme,
        shapes =
            Shapes(
                extraSmall = RoundedCornerShape(10.dp),
                small = RoundedCornerShape(14.dp),
                medium = RoundedCornerShape(20.dp),
                large = RoundedCornerShape(26.dp),
                extraLarge = RoundedCornerShape(30.dp),
            ),
        content = content,
    )
}
