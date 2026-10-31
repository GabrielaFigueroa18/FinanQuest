package com.finanquest.sv.ui.niveles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.viewmodel.AuthViewModel

private val verdeNivel = Color(0xFF14A247)
private val moradoNivel = Color(0xFF7C3AED)
private val fondoNivel: Color @Composable get() = MaterialTheme.colorScheme.background
private const val XP_POR_NIVEL = 100 // Mismo valor que server.js

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NivelesScreen(authViewModel: AuthViewModel, onVolver: () -> Unit) {
    LaunchedEffect(Unit) { authViewModel.cargarPerfil() }
    val xp = (authViewModel.usuario?.xp ?: 0).coerceAtLeast(0)
    val nivel = (xp / XP_POR_NIVEL) + 1
    val xpActualNivel = xp % XP_POR_NIVEL
    val xpRestante = XP_POR_NIVEL - xpActualNivel
    val progreso = xpActualNivel.toFloat() / XP_POR_NIVEL
    val nivelesVisibles = (1..maxOf(10, nivel + 2)).toList()

    Scaffold(
        containerColor = fondoNivel,
        topBar = {
            TopAppBar(
                title = { Text("Mis niveles", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = fondoNivel)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0E9FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🐱", fontSize = 58.sp)
                        Text("¡Cada misión te hace crecer!", color = moradoNivel, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Text("Nivel $nivel", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                        Text("$xp XP acumulados", color = moradoNivel)
                        Spacer(Modifier.height(18.dp))
                        LinearProgressIndicator(
                            progress = { progreso },
                            modifier = Modifier.fillMaxWidth().height(12.dp),
                            color = verdeNivel,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("$xpActualNivel / $XP_POR_NIVEL XP · Faltan $xpRestante XP para nivel ${nivel + 1}", fontSize = 12.sp)
                    }
                }
            }
            item {
                Text("Tu camino de niveles", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Completa misiones para seguir avanzando.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            items(nivelesVisibles) { numero ->
                val alcanzado = numero <= nivel
                val actual = numero == nivel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = if (actual) Color(0xFFE8F7EC) else MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (alcanzado) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (alcanzado) verdeNivel else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Nivel $numero", fontWeight = FontWeight.Bold)
                            Text(
                                when {
                                    actual -> "Tu nivel actual"
                                    alcanzado -> "Nivel alcanzado"
                                    else -> "Se alcanza con ${(numero - 1) * XP_POR_NIVEL} XP"
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        if (actual) Icon(Icons.Filled.Star, null, tint = moradoNivel)
                    }
                }
            }
            item {
                Text("Las insignias y accesorios se consultan en Mis recompensas.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}
