package com.finanquest.sv.ui.metas

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.data.model.MetaAhorro
import com.finanquest.sv.data.model.Cuenta
import com.finanquest.sv.data.repository.CuentaRepository
import java.util.UUID
import com.finanquest.sv.viewmodel.MetaViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val vmor = Color(0xFF7C3AED)
private val vverde = Color(0xFF14A247)
private val vfondo: Color @Composable get() = MaterialTheme.colorScheme.background
private val verdePrincipal = Color(0xFF14A247)
private val verdeOscuro = Color(0xFF0D873B)
private val verdeClaro = Color(0xFFE5F6EB)
private val moradoSuave = Color(0xFFF2EBFF)

private fun monedaMeta(v: Double): String =
 NumberFormat.getCurrencyInstance(Locale("es", "SV")).format(v)

private fun fechaMeta(v: Long): String =
 SimpleDateFormat("dd/MM/yyyy", Locale("es", "SV")).format(Date(v))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen(
 onVolver: () -> Unit = {},
 onNuevaMeta: () -> Unit = {},
 onDetalle: (MetaAhorro) -> Unit = {},
 vm: MetaViewModel = viewModel()
) {
 LaunchedEffect(Unit) { vm.observar() }
 val metas = vm.metas
 val totalAhorrado = metas.sumOf { it.actual.coerceAtLeast(0.0) }
 val completadas = metas.count { it.completada || (it.objetivo > 0 && it.actual >= it.objetivo) }
 val activas = metas.size - completadas
 val porcentajeGlobal = if (metas.sumOf { it.objetivo } > 0) {
  (totalAhorrado / metas.sumOf { it.objetivo }).coerceIn(0.0, 1.0)
 } else 0.0

 Scaffold(
  containerColor = vfondo,
  topBar = {
   TopAppBar(
    title = { Text("Mis metas", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface) },
    navigationIcon = {
     IconButton(onClick = onVolver) {
      Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = verdeOscuro)
     }
    },
    actions = {
     IconButton(onClick = onNuevaMeta) {
      Icon(Icons.Default.Add, contentDescription = "Crear nueva meta", tint = verdePrincipal)
     }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = vfondo)
   )
  }
 ) { padding ->
  LazyColumn(
   modifier = Modifier.fillMaxSize().padding(padding),
   contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 32.dp),
   verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
   item {
    Card(
     modifier = Modifier.fillMaxWidth(),
     shape = RoundedCornerShape(26.dp),
     colors = CardDefaults.cardColors(containerColor = Color(0xFFF0E9FF))
    ) {
     Column(
      modifier = Modifier.background(
       Brush.linearGradient(listOf(Color(0xFFF0E9FF), Color(0xFFEDE4FF)))
      ).padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(15.dp)
     ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
       Column(Modifier.weight(1f)) {
        Text("FINANQUEST  •  TUS SUEÑOS", color = Color(0xFF7C3AED), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(7.dp))
        Text("Cada ahorro cuenta", color = Color(0xFF4C1D95), fontWeight = FontWeight.ExtraBold, fontSize = 23.sp)
        Spacer(Modifier.height(4.dp))
        Text("¡Vas más cerca de lograrlo!", color = Color(0xFF6B5A85), fontSize = 13.sp)
       }
       Box(
        modifier = Modifier.size(57.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.70f), CircleShape),
        contentAlignment = Alignment.Center
       ) { Text("🐱💜", fontSize = 25.sp) }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
       Column(
        Modifier.weight(1f).background(Color(0xFF7C3AED), RoundedCornerShape(18.dp)).padding(13.dp)
       ) {
        Text("TOTAL AHORRADO", color = Color(0xFFF1E7FF), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(5.dp))
        Text(monedaMeta(totalAhorrado), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
       }
       Column(
        Modifier.weight(1f).background(Color(0xFF7C3AED), RoundedCornerShape(18.dp)).padding(13.dp)
       ) {
        Text("METAS ACTIVAS", color = Color(0xFFF1E7FF), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(5.dp))
        Text("$activas", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
       }
      }
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
       LinearProgressIndicator(
        progress = { porcentajeGlobal.toFloat() },
        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(8.dp)),
        color = Color(0xFF14A247), trackColor = Color(0xFFD9CBF5)
       )
       Text("${(porcentajeGlobal * 100).toInt()}%", color = Color(0xFF4C1D95), fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
     }
    }
   }
   item {
    Button(
     onClick = onNuevaMeta,
     modifier = Modifier.fillMaxWidth().height(54.dp),
     shape = RoundedCornerShape(18.dp),
     colors = ButtonDefaults.buttonColors(containerColor = vmor)
    ) {
     Icon(Icons.Default.Add, contentDescription = null)
     Spacer(Modifier.width(8.dp))
     Text("Crear una nueva meta", fontWeight = FontWeight.Bold)
    }
   }
   vm.mensaje?.let { mensaje ->
    item {
     val esExito = listOf("guardad", "cread", "descontad", "correctamente", "completad").any { mensaje.contains(it, ignoreCase = true) }
     val color = if (esExito) verdeOscuro else Color(0xFFB42318)
     val fondo = if (esExito) verdeClaro else Color(0xFFFFEDEA)
     Card(colors = CardDefaults.cardColors(containerColor = fondo), shape = RoundedCornerShape(15.dp)) {
      Text(
       text = (if (esExito) "✓  " else "!  ") + mensaje,
       color = color,
       fontSize = 13.sp,
       fontWeight = FontWeight.Medium,
       modifier = Modifier.fillMaxWidth().padding(14.dp)
      )
     }
    }
   }
   item {
    Row(verticalAlignment = Alignment.CenterVertically) {
     Column(Modifier.weight(1f)) {
      Text("Tus objetivos", fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, color = MaterialTheme.colorScheme.onSurface)
      Text("Pequeños pasos, grandes logros", fontSize = 12.sp, color = Color(0xFF728478))
     }
     Box(Modifier.background(moradoSuave, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 7.dp)) {
      Text("${metas.size} metas", color = vmor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
     }
    }
   }
   if (vm.cargando && metas.isEmpty()) {
    item { Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = verdePrincipal) } }
   } else if (metas.isEmpty()) {
    item {
     Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
      Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
       Text("🌱", fontSize = 42.sp)
       Text("Tu primer sueño empieza aquí", fontWeight = FontWeight.Bold, color = verdeOscuro, fontSize = 17.sp)
       Text("Crea una meta y empieza a ahorrar a tu ritmo.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
       OutlinedButton(onClick = onNuevaMeta) { Text("Empezar a ahorrar") }
      }
     }
    }
   } else {
    items(metas, key = { it.id }) { meta ->
     val progreso = if (meta.objetivo > 0) (meta.actual / meta.objetivo).coerceIn(0.0, 1.0) else 0.0
     val pendiente = (meta.objetivo - meta.actual).coerceAtLeast(0.0)
     val completa = meta.completada || progreso >= 1.0
     Card(
      modifier = Modifier.fillMaxWidth().shadow(5.dp, RoundedCornerShape(23.dp), ambientColor = Color(0xFFB7D8C3).copy(alpha = 0.25f)).clickable { onDetalle(meta) },
      shape = RoundedCornerShape(23.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
     ) {
      Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
       Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(55.dp).background(moradoSuave, RoundedCornerShape(17.dp)), contentAlignment = Alignment.Center) {
         Text(meta.icono, fontSize = 27.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
         Text(meta.nombre, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
         Spacer(Modifier.height(3.dp))
         Text(if (completa) "¡Objetivo cumplido! 🎉" else "Tu sueño está en marcha", color = if (completa) verdePrincipal else Color(0xFF7A8B80), fontSize = 12.sp)
        }
        Box(Modifier.background(if (completa) verdeClaro else moradoSuave, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 7.dp)) {
         Text("${(progreso * 100).toInt()}%", color = if (completa) verdeOscuro else vmor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
       }
       Row(verticalAlignment = Alignment.Bottom) {
        Text(monedaMeta(meta.actual), color = verdePrincipal, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(7.dp))
        Text("/ ${monedaMeta(meta.objetivo)}", color = Color(0xFF87978C), fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
       }
       LinearProgressIndicator(
        progress = { progreso.toFloat() },
        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp)),
        color = verdePrincipal,
        trackColor = verdeClaro
       )
       Row(verticalAlignment = Alignment.CenterVertically) {
        Text(if (completa) "Meta alcanzada" else "Faltan ${monedaMeta(pendiente)}", color = Color(0xFF74877A), fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text("Ver detalle y aportar", color = vmor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.width(3.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = vmor, modifier = Modifier.size(18.dp))
       }
      }
     }
    }
   }
  }
 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearMetaScreen(
 onVolver: () -> Unit = {},
 onCrear: (String, Double, Double, Long, String) -> Unit = { _, _, _, _, _ -> },
 vm: MetaViewModel = viewModel()
) {
 var nombre by remember { mutableStateOf("") }
 var objetivo by remember { mutableStateOf("") }
 var inicial by remember { mutableStateOf("0") }
 var fechaLimite by remember { mutableLongStateOf(0L) }
 var icono by remember { mutableStateOf("🎯") }
 var cuentaId by remember { mutableStateOf("") }
 var cuentas by remember { mutableStateOf<List<Cuenta>>(emptyList()) }
 var errorCuentas by remember { mutableStateOf<String?>(null) }
 // Mantener el mismo ID al reintentar una solicitud de creación.
 var solicitudId by remember { mutableStateOf(UUID.randomUUID().toString().replace("-", "").take(20)) }
 val cuentaRepo = remember { CuentaRepository() }
 val context = LocalContext.current
 val moradoOscuro = Color(0xFF4C1D95)
 val moradoClaro = Color(0xFFF0E9FF)
 val objetivoNumero = objetivo.replace(",", ".").toDoubleOrNull()
 val inicialNumero = inicial.replace(",", ".").toDoubleOrNull()
 val requiereCuenta = (inicialNumero ?: 0.0) > 0.0
 val cuentaElegida = cuentas.firstOrNull { it.id == cuentaId }

 LaunchedEffect(Unit) { vm.limpiarMensaje() }
 DisposableEffect(cuentaRepo) {
  val registro = cuentaRepo.escucharCuentas { lista, error ->
   cuentas = lista.filter { it.activa }
   errorCuentas = error
  }
  onDispose { registro?.remove() }
 }

 Scaffold(
  containerColor = Color(0xFFF9F7FE),
  topBar = {
   CenterAlignedTopAppBar(
    title = { Text("Nueva meta", fontWeight = FontWeight.Bold, color = moradoOscuro) },
    navigationIcon = {
     IconButton(onClick = onVolver) {
      Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = moradoOscuro)
     }
    },
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF4EEFF))
   )
  }
 ) { padding ->
  Column(
   modifier = Modifier.fillMaxSize().padding(padding)
    .verticalScroll(rememberScrollState()).padding(18.dp),
   verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
   Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = moradoClaro)
   ) {
    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
     Column(Modifier.weight(1f)) {
      Text("¡Una nueva aventura!", fontWeight = FontWeight.Bold,
       fontSize = 20.sp, color = moradoOscuro)
      Spacer(Modifier.height(6.dp))
      Text("Ahorra paso a paso para cumplir tus sueños.", color = Color(0xFF6B5A85))
     }
     Text("🐱💜", fontSize = 32.sp)
    }
   }

   Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
     Text("Detalles de tu meta", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = moradoOscuro)
     OutlinedTextField(
      value = nombre, onValueChange = { nombre = it.take(60) },
      label = { Text("Nombre de la meta") },
      placeholder = { Text("Ej. Viaje, laptop, graduación") },
      singleLine = true, modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp)
     )
     OutlinedTextField(
      value = objetivo,
      onValueChange = { objetivo = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
      label = { Text("Monto objetivo") }, leadingIcon = { Text("$") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
      singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
     )
     OutlinedTextField(
      value = inicial,
      onValueChange = { inicial = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
      label = { Text("Ahorro inicial (opcional)") }, leadingIcon = { Text("$") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
      singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
     )
     Text("Elige un icono", fontWeight = FontWeight.SemiBold, color = moradoOscuro)
     Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
      listOf("🎯", "💻", "✈️", "🚘", "🏠", "🎓", "🛡️").forEach { opcion ->
       Box(Modifier.weight(1f)) {
        FilterChip(
         selected = icono == opcion,
         onClick = { icono = opcion },
         label = { Text(opcion, fontSize = 15.sp) },
         colors = FilterChipDefaults.filterChipColors(selectedContainerColor = moradoClaro)
        )
       }
      }
     }
     OutlinedButton(
      onClick = {
       val hoy = Calendar.getInstance()
       DatePickerDialog(context, { _, year, month, day ->
        fechaLimite = Calendar.getInstance().apply {
         set(year, month, day, 0, 0, 0)
         set(Calendar.MILLISECOND, 0)
        }.timeInMillis
       }, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH)).apply {
        datePicker.minDate = Calendar.getInstance().apply {
         set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
         set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
       }.show()
      },
      modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
     ) {
      Icon(Icons.Default.CalendarMonth, contentDescription = null)
      Spacer(Modifier.width(8.dp))
      Text(if (fechaLimite == 0L) "Elegir fecha límite (opcional)" else fechaMeta(fechaLimite))
     }
    }
   }

   Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1EBFF))) {
    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
     Text("💳 Cuenta de origen", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = moradoOscuro)
     Text(
      if (requiereCuenta) "Selecciona de dónde se descontará el ahorro inicial."
      else "Opcional si comienzas con $0. Puedes aportar después.",
      color = Color(0xFF6B5A85), fontSize = 13.sp
     )
     errorCuentas?.let { Text(it, color = Color(0xFFB42318)) }
     if (cuentas.isEmpty()) {
      Text("No hay cuentas activas disponibles.", color = Color(0xFF6B5A85))
     }
     cuentas.forEach { cuenta ->
      val seleccionada = cuentaId == cuenta.id
      Card(
       modifier = Modifier.fillMaxWidth().clickable { cuentaId = cuenta.id },
       shape = RoundedCornerShape(16.dp),
       colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
       border = if (seleccionada) androidx.compose.foundation.BorderStroke(2.dp, vmor) else null
      ) {
       Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = seleccionada, onClick = { cuentaId = cuenta.id })
        Column(Modifier.weight(1f)) {
         Text(cuenta.nombre, fontWeight = FontWeight.SemiBold)
         Text(cuenta.tipo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(monedaMeta(cuenta.saldo), color = moradoOscuro, fontWeight = FontWeight.Bold)
       }
      }
     }
     if (requiereCuenta && cuentaElegida != null && inicialNumero != null &&
      cuentaElegida.saldo + 0.000001 < inicialNumero) {
      Text("Saldo insuficiente en la cuenta seleccionada.", color = Color(0xFFB42318))
     }
    }
   }

   vm.mensaje?.let { Text(it, color = if (it.contains("correctamente", true)) vverde else Color(0xFFB42318)) }
   val montoValido = objetivoNumero != null && objetivoNumero.isFinite() && objetivoNumero > 0 &&
           inicialNumero != null && inicialNumero.isFinite() && inicialNumero >= 0 &&
           inicialNumero <= objetivoNumero &&
           kotlin.math.abs(inicialNumero * 100 - kotlin.math.round(inicialNumero * 100)) < 0.000001
   val cuentaValida = !requiereCuenta || (cuentaElegida != null && cuentaElegida.saldo + 0.000001 >= (inicialNumero ?: 0.0))
   Button(
    onClick = {
     if (requiereCuenta) {
      vm.crearConCuenta(
       nombre.trim(), objetivoNumero!!, inicialNumero!!, fechaLimite,
       icono, cuentaId, solicitudId
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
    colors = ButtonDefaults.buttonColors(containerColor = vmor)
   ) { Text(if (vm.cargando) "Guardando…" else "💜 Guardar meta") }
   OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
    Text("Cancelar")
   }
  }
 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMetaScreen(
 meta: MetaAhorro,
 onVolver: () -> Unit = {},
 vm: MetaViewModel = viewModel()
) {
 var monto by remember { mutableStateOf("") }
 var dialogo by remember { mutableStateOf(false) }
 var cuentas by remember { mutableStateOf<List<Cuenta>>(emptyList()) }
 var errorCuentas by remember { mutableStateOf<String?>(null) }
 var cuentaId by remember { mutableStateOf("") }
 var solicitudId by remember { mutableStateOf(UUID.randomUUID().toString().replace("-", "").take(20)) }
 val cuentaRepo = remember { CuentaRepository() }

 LaunchedEffect(meta.id) {
  vm.observar()
  vm.observarAportes(meta.id)
 }
 DisposableEffect(cuentaRepo) {
  val registro = cuentaRepo.escucharCuentas { lista, error ->
   cuentas = lista.filter { it.activa }
   errorCuentas = error
  }
  onDispose { registro?.remove() }
 }

 val actual = vm.metas.firstOrNull { it.id == meta.id } ?: meta
 val progreso = if (actual.objetivo > 0) (actual.actual / actual.objetivo).coerceIn(0.0, 1.0) else 0.0
 val pendiente = (actual.objetivo - actual.actual).coerceAtLeast(0.0)
 val seleccionada = cuentas.firstOrNull { it.id == cuentaId }
 val montoNumero = monto.replace(",", ".").toDoubleOrNull()
 val montoValido = montoNumero != null && montoNumero.isFinite() && montoNumero > 0 &&
         montoNumero <= pendiente + 0.000001 &&
         kotlin.math.abs(montoNumero * 100 - kotlin.math.round(montoNumero * 100)) < 0.000001 &&
         seleccionada != null && montoNumero <= seleccionada.saldo + 0.000001

 Scaffold(
  containerColor = Color(0xFFF6F3FF),
  topBar = {
   CenterAlignedTopAppBar(
    title = { Text("Detalle de meta", fontWeight = FontWeight.Bold) },
    navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver") } },
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF6F3FF))
   )
  }
 ) { padding ->
  Column(
   modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(18.dp),
   verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
   Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth()
   ) {
    Column(
     Modifier.fillMaxWidth().padding(22.dp),
     horizontalAlignment = Alignment.CenterHorizontally,
     verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
     Text("🐱💜", fontSize = 48.sp)
     Text(actual.icono + "  " + actual.nombre, fontSize = 21.sp, fontWeight = FontWeight.Bold)
     Text("Cada aporte te acerca a tu sueño", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
     Text(monedaMeta(actual.actual), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = vmor)
     Text("de ${monedaMeta(actual.objetivo)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
     LinearProgressIndicator(
      progress = { progreso.toFloat() },
      modifier = Modifier.fillMaxWidth().height(10.dp),
      color = vmor,
      trackColor = Color(0xFFE9DFFF)
     )
     Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text("${(progreso * 100).toInt()}% completado", color = vmor, fontWeight = FontWeight.SemiBold)
      Text("Faltan ${monedaMeta(pendiente)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
     }
     if (actual.fechaLimite > 0) Text("Fecha límite: ${fechaMeta(actual.fechaLimite)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
   }

   vm.mensaje?.let { mensaje ->
    Text(
     mensaje,
     color = if (mensaje.contains("guardad", ignoreCase = true)) vverde else Color(0xFFB42318),
     fontSize = 13.sp
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
    colors = ButtonDefaults.buttonColors(containerColor = vmor)
   ) { Text(if (pendiente <= 0) "¡Meta completada! 🎉" else "＋ Agregar aporte") }

   Text("Actividad reciente", fontWeight = FontWeight.Bold, fontSize = 19.sp)
   if (vm.aportes.isEmpty()) Text("Aún no hay aportes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
   vm.aportes.forEach { aporte ->
    Card(
     shape = RoundedCornerShape(16.dp),
     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
     modifier = Modifier.fillMaxWidth()
    ) {
     Row(
      Modifier.padding(15.dp).fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
     ) {
      Column(Modifier.weight(1f)) {
       Text(if (aporte.nota.isBlank()) "Aporte a la meta" else aporte.nota, fontWeight = FontWeight.Medium)
       Text(fechaMeta(aporte.fecha), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
      }
      Text("+${monedaMeta(aporte.monto)}", color = vverde, fontWeight = FontWeight.Bold)
     }
    }
   }
  }
 }

 if (dialogo) {
  AlertDialog(
   onDismissRequest = { if (!vm.cargando) dialogo = false },
   title = { Text("🐱 Agregar aporte", fontWeight = FontWeight.Bold) },
   text = {
    Column(
     verticalArrangement = Arrangement.spacedBy(10.dp),
     modifier = Modifier.heightIn(max = 450.dp).verticalScroll(rememberScrollState())
    ) {
     Text("Elige la cuenta desde donde ahorrarás.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
     errorCuentas?.let { Text(it, color = Color(0xFFB42318), fontSize = 12.sp) }
     if (cuentas.isEmpty()) Text("No hay cuentas activas. Crea una cuenta primero.", color = Color(0xFFB42318))
     cuentas.forEach { cuenta ->
      Row(
       modifier = Modifier.fillMaxWidth().clickable(enabled = !vm.cargando) {
        if (cuentaId != cuenta.id) {
         cuentaId = cuenta.id
         solicitudId = UUID.randomUUID().toString().replace("-", "").take(20)
        }
       },
       verticalAlignment = Alignment.CenterVertically
      ) {
       RadioButton(
        selected = cuentaId == cuenta.id,
        enabled = !vm.cargando,
        onClick = {
         if (cuentaId != cuenta.id) {
          cuentaId = cuenta.id
          solicitudId = UUID.randomUUID().toString().replace("-", "").take(20)
         }
        }
       )
       Column {
        Text(cuenta.nombre, fontWeight = FontWeight.SemiBold)
        Text("Disponible: ${monedaMeta(cuenta.saldo)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
      modifier = Modifier.fillMaxWidth()
     )
     Text("Pendiente de la meta: ${monedaMeta(pendiente)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
     if (montoNumero != null && seleccionada != null && montoNumero > seleccionada.saldo) {
      Text("La cuenta no tiene saldo suficiente.", color = Color(0xFFB42318), fontSize = 12.sp)
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
     colors = ButtonDefaults.buttonColors(containerColor = vmor)
    ) { Text(if (vm.cargando) "Guardando…" else "Guardar aporte") }
   },
   dismissButton = {
    TextButton(onClick = { dialogo = false }, enabled = !vm.cargando) { Text("Cancelar") }
   }
  )
 }
}

