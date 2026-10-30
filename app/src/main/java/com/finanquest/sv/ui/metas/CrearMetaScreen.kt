package com.finanquest.sv.ui.metas

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.R
import com.finanquest.sv.data.model.Cuenta
import com.finanquest.sv.data.repository.CuentaRepository
import com.finanquest.sv.ui.components.*
import com.finanquest.sv.viewmodel.MetaViewModel
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearMetaScreen(
    onVolver: () -> Unit = {},
    onCrear: (String, Double, Double, Long, String) -> Unit = { _, _, _, _, _ -> },
    vm: MetaViewModel = viewModel(),
) {
    QuestVisualTheme {
        var nombre by remember { mutableStateOf("") }
        var objetivo by remember { mutableStateOf("") }
        var inicial by remember { mutableStateOf("0") }
        var fechaLimite by remember { mutableLongStateOf(0L) }
        var icono by remember { mutableStateOf("🎯") }
        var cuentaId by remember { mutableStateOf("") }
        var cuentas by remember { mutableStateOf<List<Cuenta>>(emptyList()) }
        var errorCuentas by remember { mutableStateOf<String?>(null) }
        // Mantener el mismo ID al reintentar una solicitud de creación.
        var solicitudId by remember {
            mutableStateOf(UUID.randomUUID().toString().replace("-", "").take(20))
        }
        val cuentaRepo = remember { CuentaRepository() }
        val context = LocalContext.current
        val moradoOscuro = MaterialTheme.colorScheme.secondary
        val moradoClaro = MaterialTheme.colorScheme.secondaryContainer
        val objetivoNumero = objetivo.replace(",", ".").toDoubleOrNull()
        val inicialNumero = inicial.replace(",", ".").toDoubleOrNull()
        val requiereCuenta = (inicialNumero ?: 0.0) > 0.0
        val cuentaElegida = cuentas.firstOrNull { it.id == cuentaId }

        LaunchedEffect(Unit) { vm.limpiarMensaje() }
        DisposableEffect(cuentaRepo) {
            val registro =
                cuentaRepo.escucharCuentas { lista, error ->
                    cuentas = lista.filter { it.activa }
                    errorCuentas = error
                }
            onDispose { registro?.remove() }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Nueva meta", fontWeight = FontWeight.Bold, color = moradoOscuro)
                    },
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = moradoOscuro,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
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
                    colors = CardDefaults.cardColors(containerColor = moradoClaro),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "¡Una nueva aventura!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = moradoOscuro,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Ahorra paso a paso para cumplir tus sueños.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        QuestArtwork(R.drawable.quest_goal, Modifier.size(76.dp))
                    }
                }

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            "Detalles de tu meta",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = moradoOscuro,
                        )
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it.take(60) },
                            label = { Text("Nombre de la meta") },
                            placeholder = { Text("Ej. Viaje, laptop, graduación") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                        )
                        OutlinedTextField(
                            value = objetivo,
                            onValueChange = {
                                objetivo = it.filter { c -> c.isDigit() || c == '.' || c == ',' }
                            },
                            label = { Text("Monto objetivo") },
                            leadingIcon = { Text("$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                        )
                        OutlinedTextField(
                            value = inicial,
                            onValueChange = {
                                inicial = it.filter { c -> c.isDigit() || c == '.' || c == ',' }
                            },
                            label = { Text("Ahorro inicial (opcional)") },
                            leadingIcon = { Text("$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                        )
                        Text(
                            "Elige una imagen",
                            fontWeight = FontWeight.SemiBold,
                            color = moradoOscuro,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier =
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        ) {
                            opcionesMeta.forEach { (clave, etiqueta) ->
                                FilterChip(
                                    selected = icono == clave,
                                    onClick = { icono = clave },
                                    label = {
                                        Column(
                                            Modifier.padding(vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            QuestArtwork(
                                                imagenMeta(clave),
                                                Modifier.size(44.dp),
                                                etiqueta,
                                            )
                                            Text(etiqueta, fontSize = 11.sp)
                                        }
                                    },
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                val hoy = Calendar.getInstance()
                                DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            fechaLimite =
                                                Calendar.getInstance()
                                                    .apply {
                                                        set(year, month, day, 0, 0, 0)
                                                        set(Calendar.MILLISECOND, 0)
                                                    }
                                                    .timeInMillis
                                        },
                                        hoy.get(Calendar.YEAR),
                                        hoy.get(Calendar.MONTH),
                                        hoy.get(Calendar.DAY_OF_MONTH),
                                    )
                                    .apply {
                                        datePicker.minDate =
                                            Calendar.getInstance()
                                                .apply {
                                                    set(Calendar.HOUR_OF_DAY, 0)
                                                    set(Calendar.MINUTE, 0)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }
                                                .timeInMillis
                                    }
                                    .show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (fechaLimite == 0L) "Elegir fecha límite (opcional)"
                                else fechaMeta(fechaLimite)
                            )
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1EBFF)),
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "Cuenta de origen",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = moradoOscuro,
                        )
                        Text(
                            if (requiereCuenta)
                                "Selecciona de dónde se descontará el ahorro inicial."
                            else "Opcional si comienzas con $0. Puedes aportar después.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                        errorCuentas?.let { Text(it, color = Color(0xFFB42318)) }
                        if (cuentas.isEmpty()) {
                            Text(
                                "No hay cuentas activas disponibles.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        cuentas.forEach { cuenta ->
                            val seleccionada = cuentaId == cuenta.id
                            Card(
                                modifier =
                                    Modifier.fillMaxWidth().clickable { cuentaId = cuenta.id },
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                border =
                                    if (seleccionada)
                                        androidx.compose.foundation.BorderStroke(2.dp, vmor)
                                    else null,
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = seleccionada,
                                        onClick = { cuentaId = cuenta.id },
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(cuenta.nombre, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            cuenta.tipo,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        monedaMeta(cuenta.saldo),
                                        color = moradoOscuro,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                        if (
                            requiereCuenta &&
                                cuentaElegida != null &&
                                inicialNumero != null &&
                                cuentaElegida.saldo + 0.000001 < inicialNumero
                        ) {
                            Text(
                                "Saldo insuficiente en la cuenta seleccionada.",
                                color = Color(0xFFB42318),
                            )
                        }
                    }
                }

                vm.mensaje?.let {
                    Text(
                        it,
                        color =
                            if (it.contains("correctamente", true)) vverde else Color(0xFFB42318),
                    )
                }
                val montoValido =
                    objetivoNumero != null &&
                        objetivoNumero.isFinite() &&
                        objetivoNumero > 0 &&
                        inicialNumero != null &&
                        inicialNumero.isFinite() &&
                        inicialNumero >= 0 &&
                        inicialNumero <= objetivoNumero &&
                        kotlin.math.abs(
                            inicialNumero * 100 - kotlin.math.round(inicialNumero * 100)
                        ) < 0.000001
                val cuentaValida =
                    !requiereCuenta ||
                        (cuentaElegida != null &&
                            cuentaElegida.saldo + 0.000001 >= (inicialNumero ?: 0.0))
                Button(
                    onClick = {
                        if (requiereCuenta) {
                            vm.crearConCuenta(
                                nombre.trim(),
                                objetivoNumero!!,
                                inicialNumero!!,
                                fechaLimite,
                                icono,
                                cuentaId,
                                solicitudId,
                            ) {
                                solicitudId = UUID.randomUUID().toString().replace("-", "").take(20)
                                onVolver()
                            }
                        } else {
                            // Conserva el flujo original de AppNavigation y repo.crear().
                            onCrear(nombre.trim(), objetivoNumero!!, 0.0, fechaLimite, icono)
                        }
                    },
                    enabled = !vm.cargando && nombre.isNotBlank() && montoValido && cuentaValida,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = vmor),
                ) {
                    Text(if (vm.cargando) "Guardando…" else "Guardar meta")
                }
                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
