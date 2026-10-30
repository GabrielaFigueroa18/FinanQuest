package com.finanquest.sv.ui.presupuestos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.viewmodel.PresupuestoViewModel
import java.text.NumberFormat
import java.util.Locale

internal fun dineroPres(valor: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-SV")).format(valor)

@Composable
fun PresupuestosScreen(
    onVolver: () -> Unit = {},
    onCrear: () -> Unit = {},
    onCategorias: () -> Unit = {},
    onDetalle: (String) -> Unit = {},
    onReportes: () -> Unit = {},
    vm: PresupuestoViewModel = viewModel(),
) {
    LaunchedEffect(vm) { vm.observar() }
    PresupuestoEstilo {
        val activos = vm.presupuestos.filter { it.activo }
        val gasto = activos.sumOf { vm.gastoPresupuesto(it) }
        Scaffold(
            containerColor = fondoPres,
            topBar = {
                PresupuestoBarra("Presupuestos", onVolver) {
                    IconButton(onClick = onCategorias) {
                        Icon(Icons.Default.Tune, "Categorías y alertas")
                    }
                }
            },
            bottomBar = {
                Surface(color = fondoPres) {
                    Button(
                        onClick = onCrear,
                        modifier =
                            Modifier.fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .heightIn(min = 54.dp),
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Crear presupuesto", fontWeight = FontWeight.Bold)
                    }
                }
            },
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { PresupuestoResumen(gasto, activos.sumOf { it.limite }) }
                item {
                    Text(
                        "Resumen de períodos activos · semanal y mensual",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                vm.mensaje?.let { mensaje ->
                    item {
                        Text(
                            mensaje,
                            color = if (mensaje.contains("guardad", true)) verdePres else rojoPres,
                        )
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Mis presupuestos", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (activos.size == 1) "1 activo" else "${activos.size} activos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = onCategorias) { Text("Categorías") }
                    }
                }
                if (vm.cargando && activos.isEmpty())
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                else if (activos.isEmpty())
                    item {
                        PresupuestoIntroduccion(
                            "Aún no tienes presupuestos",
                            "Define un límite para controlar tus gastos.",
                        )
                    }
                items(activos, key = { it.id }) { presupuesto ->
                    val usado = vm.gastoPresupuesto(presupuesto)
                    val fraccion = if (presupuesto.limite > 0) usado / presupuesto.limite else 0.0
                    val alerta = vm.alerta(presupuesto)
                    val color = if (alerta) rojoPres else verdePres
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onDetalle(presupuesto.id) },
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                        shape = RoundedCornerShape(24.dp),
                        border =
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .55f)),
                    ) {
                        Column(
                            Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                PresupuestoIcono(presupuesto.categoria)
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        presupuesto.categoria,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                    )
                                    Text(
                                        presupuesto.periodo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Surface(
                                    color = color.copy(alpha = .1f),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Text(
                                        "${(fraccion * 100).toInt()}%",
                                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        color = color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    dineroPres(usado),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "de ${dineroPres(presupuesto.limite)}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.Bottom),
                                )
                            }
                            LinearProgressIndicator(
                                progress = { fraccion.toFloat().coerceIn(0f, 1f) },
                                color = color,
                                trackColor = color.copy(alpha = .1f),
                                modifier = Modifier.fillMaxWidth().height(7.dp),
                            )
                            Text(
                                if (alerta) "Has alcanzado el umbral de alerta"
                                else
                                    "${dineroPres((presupuesto.limite - usado).coerceAtLeast(0.0))} disponibles",
                                color = color,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
                item {
                    OutlinedButton(
                        onClick = onCategorias,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    ) {
                        Icon(Icons.Default.Tune, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Categorías y alertas")
                    }
                }
                item {
                    TextButton(onClick = onReportes, modifier = Modifier.fillMaxWidth()) {
                        Text("Ver reportes")
                    }
                }
            }
        }
    }
}
