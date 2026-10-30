package com.finanquest.sv.ui.presupuestos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.R

internal val verdePres: Color
    @Composable get() = MaterialTheme.colorScheme.primary
internal val fondoPres: Color
    @Composable get() = MaterialTheme.colorScheme.background
internal val rojoPres: Color
    @Composable get() = MaterialTheme.colorScheme.error

@Composable
internal fun PresupuestoEstilo(content: @Composable () -> Unit) {
    com.finanquest.sv.ui.components.QuestVisualTheme(content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PresupuestoBarra(
    titulo: String,
    onVolver: () -> Unit,
    acciones: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = { Text(titulo, fontWeight = FontWeight.Bold, fontSize = 19.sp) },
        navigationIcon = {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
        },
        actions = acciones,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = fondoPres),
    )
}

@Composable
internal fun PresupuestoIcono(nombre: String, modifier: Modifier = Modifier) {
    val recurso =
        when {
            nombre.contains("comida", true) || nombre.contains("alimenta", true) ->
                R.drawable.presupuesto_food
            nombre.contains("transporte", true) -> R.drawable.presupuesto_transport
            nombre.contains("educa", true) -> R.drawable.presupuesto_education
            else -> R.drawable.presupuesto_wallet
        }
    Image(
        painter = painterResource(recurso),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(58.dp),
    )
}

@Composable
internal fun PresupuestoIntroduccion(titulo: String, descripcion: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .6f)),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.presupuesto_wallet),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(68.dp),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun PresupuestoResumen(
    gasto: Double,
    limite: Double,
    titulo: String = "Tu dinero, bajo control",
) {
    val excedido = gasto > limite
    Column(
        Modifier.fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Color(0xFF35215E), Color(0xFF7044BA))),
                RoundedCornerShape(28.dp),
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(titulo, color = Color(0xFFE0D6F4), fontSize = 12.sp)
                Text(
                    dineroPres(
                        if (excedido) gasto - limite else (limite - gasto).coerceAtLeast(0.0)
                    ),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp,
                )
                Text(
                    if (excedido) "Por encima del límite" else "Disponibles",
                    color = Color(0xFFE0D6F4),
                    fontSize = 12.sp,
                )
            }
            Image(
                painter = painterResource(R.drawable.presupuesto_wallet),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(78.dp),
            )
        }
        LinearProgressIndicator(
            progress = { if (limite > 0) (gasto / limite).toFloat().coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth().height(7.dp),
            color = if (excedido) Color(0xFFFFBAA7) else Color(0xFF74E3AE),
            trackColor = Color.White.copy(alpha = .16f),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("UTILIZADO", color = Color(0xFFD9C9F4), fontSize = 10.sp, letterSpacing = 1.sp)
                Text(dineroPres(gasto), color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "LÍMITE TOTAL",
                    color = Color(0xFFD9C9F4),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                )
                Text(dineroPres(limite), color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
