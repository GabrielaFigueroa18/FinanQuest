package com.finanquest.sv.ui.metas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.R
import com.finanquest.sv.data.model.MetaAhorro
import com.finanquest.sv.ui.components.*
import com.finanquest.sv.viewmodel.MetaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen(
    onVolver: () -> Unit = {},
    onNuevaMeta: () -> Unit = {},
    onDetalle: (MetaAhorro) -> Unit = {},
    vm: MetaViewModel = viewModel(),
) {
    LaunchedEffect(vm) { vm.observar() }
    QuestVisualTheme {
        var filtro by rememberSaveable { mutableStateOf("Todas") }
        val metas = vm.metas
        val completada: (MetaAhorro) -> Boolean = {
            it.completada || (it.objetivo > 0 && it.actual >= it.objetivo)
        }
        val visibles =
            metas.filter {
                when (filtro) {
                    "Activas" -> !completada(it)
                    "Completadas" -> completada(it)
                    else -> true
                }
            }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Mis metas", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver") }
                    },
                    colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                )
            },
            bottomBar = {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Button(
                        onClick = onNuevaMeta,
                        modifier =
                            Modifier.fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .heightIn(min = 54.dp),
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Crear una nueva meta", fontWeight = FontWeight.Bold)
                    }
                }
            },
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).testTag("lista-metas"),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Column(
                        Modifier.fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF35215E), Color(0xFF7044BA))),
                                RoundedCornerShape(28.dp),
                            )
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Text(
                                    "CADA AHORRO CUENTA",
                                    color = Color(0xFFE4DAF9),
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                )
                                Text(
                                    monedaMeta(metas.sumOf { it.actual }),
                                    color = Color.White,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                                Text(
                                    "Total ahorrado en tus metas",
                                    color = Color(0xFFE4DAF9),
                                    fontSize = 12.sp,
                                )
                            }
                            QuestArtwork(R.drawable.quest_goal, Modifier.size(86.dp))
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = .2f))
                        Text(
                            "Activas: ${metas.count { !completada(it) }}   ·   Completadas: ${metas.count { completada(it) }}",
                            color = Color.White,
                            fontSize = 13.sp,
                        )
                    }
                }
                item {
                    Text(
                        "Ahorra paso a paso para lo que te importa.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                }
                item {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Todas", "Activas", "Completadas").forEach { opcion ->
                            FilterChip(
                                selected = filtro == opcion,
                                onClick = { filtro = opcion },
                                label = { Text(opcion) },
                            )
                        }
                    }
                }
                vm.mensaje?.let { mensaje ->
                    item {
                        Text(
                            mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                if (vm.cargando && metas.isEmpty())
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                else if (visibles.isEmpty())
                    item {
                        QuestFeatureBanner(
                            if (metas.isEmpty()) "Tu primer sueño empieza aquí"
                            else "No hay metas en esta sección",
                            "Crea una meta y empieza a ahorrar a tu ritmo.",
                            R.drawable.quest_goal,
                        )
                    }
                items(visibles, key = { it.id }) { meta ->
                    val progreso =
                        if (meta.objetivo > 0) (meta.actual / meta.objetivo).coerceIn(0.0, 1.0)
                        else 0.0
                    Card(
                        Modifier.fillMaxWidth().clickable { onDetalle(meta) },
                        shape = RoundedCornerShape(24.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                        border =
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .5f)),
                    ) {
                        Column(
                            Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                QuestArtwork(
                                    imagenMeta(meta.icono, meta.nombre),
                                    Modifier.size(64.dp),
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        meta.nombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        if (completada(meta)) "Objetivo cumplido"
                                        else "Tu sueño está en marcha",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                    )
                                }
                                Text(
                                    "${(progreso * 100).toInt()}%",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Text(
                                "${monedaMeta(meta.actual)} / ${monedaMeta(meta.objetivo)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            )
                            LinearProgressIndicator(
                                progress = { progreso.toFloat() },
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f),
                                modifier = Modifier.fillMaxWidth().height(7.dp),
                            )
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    if (completada(meta)) "Meta alcanzada"
                                    else
                                        "Faltan ${monedaMeta((meta.objetivo - meta.actual).coerceAtLeast(0.0))}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                                Icon(
                                    Icons.Default.ChevronRight,
                                    "Ver detalle y aportar",
                                    tint = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
