package com.finanquest.sv.ui.metas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.data.model.Cuenta
import com.finanquest.sv.data.model.MetaAhorro
import com.finanquest.sv.data.repository.CuentaRepository
import com.finanquest.sv.ui.components.*
import com.finanquest.sv.viewmodel.MetaViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMetaScreen(
    meta: MetaAhorro,
    onVolver: () -> Unit = {},
    vm: MetaViewModel = viewModel(),
) {
    QuestVisualTheme {
        var monto by remember { mutableStateOf("") }
        var dialogo by remember { mutableStateOf(false) }
        var cuentas by remember { mutableStateOf<List<Cuenta>>(emptyList()) }
        var errorCuentas by remember { mutableStateOf<String?>(null) }
        var cuentaId by remember { mutableStateOf("") }
        var solicitudId by remember {
            mutableStateOf(UUID.randomUUID().toString().replace("-", "").take(20))
        }
        val cuentaRepo = remember { CuentaRepository() }

        LaunchedEffect(meta.id) {
            vm.observar()
            vm.observarAportes(meta.id)
        }
        DisposableEffect(cuentaRepo) {
            val registro =
                cuentaRepo.escucharCuentas { lista, error ->
                    cuentas = lista.filter { it.activa }
                    errorCuentas = error
                }
            onDispose { registro?.remove() }
        }

        val actual = vm.metas.firstOrNull { it.id == meta.id } ?: meta
        val progreso =
            if (actual.objetivo > 0) (actual.actual / actual.objetivo).coerceIn(0.0, 1.0) else 0.0
        val pendiente = (actual.objetivo - actual.actual).coerceAtLeast(0.0)
        val seleccionada = cuentas.firstOrNull { it.id == cuentaId }
        val montoNumero = monto.replace(",", ".").toDoubleOrNull()
        val montoValido =
            montoNumero != null &&
                montoNumero.isFinite() &&
                montoNumero > 0 &&
                montoNumero <= pendiente + 0.000001 &&
                kotlin.math.abs(montoNumero * 100 - kotlin.math.round(montoNumero * 100)) <
                    0.000001 &&
                seleccionada != null &&
                montoNumero <= seleccionada.saldo + 0.000001

        Scaffold(
            containerColor = Color(0xFFF6F3FF),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Detalle de meta", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver") }
                    },
                    colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color(0xFFF6F3FF)
                        ),
                )
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .imePadding()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        QuestArtwork(imagenMeta(actual.icono, actual.nombre), Modifier.size(132.dp))
                        Text(actual.nombre, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Cada aporte te acerca a tu sueño",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                        Text(
                            monedaMeta(actual.actual),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = vmor,
                        )
                        Text(
                            "de ${monedaMeta(actual.objetivo)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LinearProgressIndicator(
                            progress = { progreso.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = vmor,
                            trackColor = Color(0xFFE9DFFF),
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "${(progreso * 100).toInt()}% completado",
                                color = vmor,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "Faltan ${monedaMeta(pendiente)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                            )
                        }
                        if (actual.fechaLimite > 0)
                            Text(
                                "Fecha límite: ${fechaMeta(actual.fechaLimite)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                    }
                }

                vm.mensaje?.let { mensaje ->
                    Text(
                        mensaje,
                        color =
                            if (mensaje.contains("guardad", ignoreCase = true)) vverde
                            else Color(0xFFB42318),
                        fontSize = 13.sp,
                    )
                }

                Button(
                    onClick = {
                        vm.limpiarMensaje()
                        dialogo = true
                    },
                    enabled = pendiente > 0 && !vm.cargando,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = vmor),
                ) {
                    Text(if (pendiente <= 0) "¡Meta completada!" else "Agregar aporte")
                }

                Text("Actividad reciente", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                if (vm.aportes.isEmpty())
                    Text("Aún no hay aportes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                vm.aportes.forEach { aporte ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.padding(15.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (aporte.nota.isBlank()) "Aporte a la meta" else aporte.nota,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    fechaMeta(aporte.fecha),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                )
                            }
                            Text(
                                "+${monedaMeta(aporte.monto)}",
                                color = vverde,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        if (dialogo) {
            AlertDialog(
                onDismissRequest = { if (!vm.cargando) dialogo = false },
                title = { Text("Agregar aporte", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier =
                            Modifier.heightIn(max = 450.dp).verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            "Elige la cuenta desde donde ahorrarás.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        errorCuentas?.let { Text(it, color = Color(0xFFB42318), fontSize = 12.sp) }
                        if (cuentas.isEmpty())
                            Text(
                                "No hay cuentas activas. Crea una cuenta primero.",
                                color = Color(0xFFB42318),
                            )
                        cuentas.forEach { cuenta ->
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth().clickable(enabled = !vm.cargando) {
                                        if (cuentaId != cuenta.id) {
                                            cuentaId = cuenta.id
                                            solicitudId =
                                                UUID.randomUUID()
                                                    .toString()
                                                    .replace("-", "")
                                                    .take(20)
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = cuentaId == cuenta.id,
                                    enabled = !vm.cargando,
                                    onClick = {
                                        if (cuentaId != cuenta.id) {
                                            cuentaId = cuenta.id
                                            solicitudId =
                                                UUID.randomUUID()
                                                    .toString()
                                                    .replace("-", "")
                                                    .take(20)
                                        }
                                    },
                                )
                                Column {
                                    Text(cuenta.nombre, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Disponible: ${monedaMeta(cuenta.saldo)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                        OutlinedTextField(
                            value = monto,
                            onValueChange = { nuevo ->
                                monto = nuevo.filter { it.isDigit() || it == '.' || it == ',' }
                                solicitudId = UUID.randomUUID().toString().replace("-", "").take(20)
                            },
                            enabled = !vm.cargando,
                            label = { Text("Monto del aporte") },
                            leadingIcon = { Text("$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "Pendiente de la meta: ${monedaMeta(pendiente)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                        if (
                            montoNumero != null &&
                                seleccionada != null &&
                                montoNumero > seleccionada.saldo
                        ) {
                            Text(
                                "La cuenta no tiene saldo suficiente.",
                                color = Color(0xFFB42318),
                                fontSize = 12.sp,
                            )
                        }
                        vm.mensaje?.let { Text(it, color = Color(0xFFB42318), fontSize = 12.sp) }
                        if (vm.cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.aportar(actual, cuentaId, monto, solicitudId) {
                                dialogo = false
                                monto = ""
                                cuentaId = ""
                                solicitudId = UUID.randomUUID().toString().replace("-", "").take(20)
                            }
                        },
                        enabled = montoValido && !vm.cargando,
                        colors = ButtonDefaults.buttonColors(containerColor = vmor),
                    ) {
                        Text(if (vm.cargando) "Guardando…" else "Guardar aporte")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dialogo = false }, enabled = !vm.cargando) {
                        Text("Cancelar")
                    }
                },
            )
        }
    }
}
