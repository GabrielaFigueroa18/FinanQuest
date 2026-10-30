package com.finanquest.sv.ui.metas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.finanquest.sv.ui.components.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal val vmor = Color(0xFF7C3AED)
internal val vverde = Color(0xFF14A247)
internal val vfondo: Color
    @Composable get() = MaterialTheme.colorScheme.background
internal val verdePrincipal = Color(0xFF14A247)
internal val verdeOscuro = Color(0xFF0D873B)
internal val verdeClaro = Color(0xFFE5F6EB)
internal val moradoSuave = Color(0xFFF2EBFF)

internal fun monedaMeta(v: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es", "SV")).format(v)

internal fun fechaMeta(v: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("es", "SV")).format(Date(v))
