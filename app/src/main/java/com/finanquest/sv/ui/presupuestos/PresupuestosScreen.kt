package com.finanquest.sv.ui.presupuestos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
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
import com.finanquest.sv.viewmodel.PresupuestoViewModel
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private val verdePres = Color(0xFF16A34A)
private val fondoPres = Color(0xFFF6F7F9)
private val rojoPres = Color(0xFFDC2626)
private fun dineroPres(valor: Double): String =
 NumberFormat.getCurrencyInstance(Locale("es", "SV")).format(valor)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestosScreen(
 onVolver: () -> Unit = {},
 onCrear: () -> Unit = {},
 onCategorias: () -> Unit = {},
 onDetalle: (String) -> Unit = {},
 onReportes: () -> Unit = {},
 vm: PresupuestoViewModel = viewModel()
) {
 LaunchedEffect(vm) { vm.observar() }

 val inicioMes = remember {
  Calendar.getInstance().apply {
   set(Calendar.DAY_OF_MONTH, 1)
   set(Calendar.HOUR_OF_DAY, 0)
   set(Calendar.MINUTE, 0)
   set(Calendar.SECOND, 0)
   set(Calendar.MILLISECOND, 0)
  }.timeInMillis
 }
 val gastoMes = vm.movimientos
  .filter { it.tipo.equals("Gasto", ignoreCase = true) && it.fecha >= inicioMes }
  .sumOf { it.monto }
 val activos = vm.presupuestos.filter { it.activo }
 val limiteTotal = activos.sumOf { it.limite }
 val porcentajeTotal = if (limiteTotal > 0) (gastoMes / limiteTotal).toFloat().coerceIn(0f, 1f) else 0f

 Scaffold(
  containerColor = fondoPres,
  topBar = {
   CenterAlignedTopAppBar(
    title = { Text("Presupuestos", fontWeight = FontWeight.Bold) },
    navigationIcon = {
     IconButton(onClick = onVolver) {
      Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
     }
    },
    actions = {
     IconButton(onClick = onCategorias) {
      Icon(Icons.Default.Tune, contentDescription = "Categorías y alertas")
     }
    }
   )
  }
 ) { padding ->
  Column(
   modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)
  ) {
   Card(
    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF8EF)),
    shape = RoundedCornerShape(16.dp)
   ) {
    Column(Modifier.padding(18.dp)) {
     Text("Resumen del mes", fontWeight = FontWeight.Bold, fontSize = 17.sp)
     Spacer(Modifier.height(5.dp))
     Text("${dineroPres(gastoMes)} de ${dineroPres(limiteTotal)} utilizados", color = Color.DarkGray)
     LinearProgressIndicator(
      progress = { porcentajeTotal },
      modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
      color = if (limiteTotal > 0 && gastoMes >= limiteTotal) rojoPres else verdePres
     )
     Text(
      "${dineroPres((limiteTotal - gastoMes).coerceAtLeast(0.0))} disponibles",
      modifier = Modifier.padding(top = 8.dp),
      color = verdePres,
      fontWeight = FontWeight.SemiBold
     )
    }
   }

   vm.mensaje?.let { mensaje ->
    Text(
     mensaje,
     color = if (mensaje.contains("guardad", ignoreCase = true)) verdePres else rojoPres,
     modifier = Modifier.padding(bottom = 8.dp)
    )
   }

   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
    Text("Mis presupuestos", fontSize = 19.sp, fontWeight = FontWeight.Bold)
    TextButton(onClick = onCategorias) { Text("Categorías y alertas") }
   }

   when {
    vm.cargando && activos.isEmpty() -> {
     Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
     }
    }
    activos.isEmpty() -> {
     Column(
      modifier = Modifier.fillMaxWidth().weight(1f),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
     ) {
      Text("Aún no tienes presupuestos", fontWeight = FontWeight.Bold)
      Text("Define un límite para controlar tus gastos.", color = Color.Gray)
      Spacer(Modifier.height(12.dp))
      Button(onClick = onCrear) { Text("Crear presupuesto") }
     }
    }
    else -> {
     LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(vertical = 8.dp)
     ) {
      items(activos, key = { it.id }) { presupuesto ->
       val usado = vm.gasto(presupuesto.categoria, presupuesto.inicioPeriodo)
       val proporcion = if (presupuesto.limite > 0) {
        (usado / presupuesto.limite).coerceIn(0.0, 1.0)
       } else 0.0
       val alerta = proporcion >= presupuesto.alertaUmbral
       Card(
        modifier = Modifier.fillMaxWidth().clickable { onDetalle(presupuesto.categoria) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
       ) {
        Column(Modifier.padding(16.dp)) {
         Row(verticalAlignment = Alignment.CenterVertically) {
          Column(Modifier.weight(1f)) {
           Text(presupuesto.categoria, fontWeight = FontWeight.Bold)
           Text(
            "${dineroPres(usado)} de ${dineroPres(presupuesto.limite)} · ${presupuesto.periodo}",
            color = Color.Gray,
            fontSize = 13.sp
           )
          }
          Text(
           "${(proporcion * 100).toInt()}%",
           color = if (alerta) rojoPres else verdePres,
           fontWeight = FontWeight.Bold
          )
         }
         LinearProgressIndicator(
          progress = { proporcion.toFloat() },
          modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
          color = if (alerta) rojoPres else verdePres
         )
         if (alerta) {
          Text("Has alcanzado el umbral de alerta", color = rojoPres, fontSize = 12.sp)
         }
        }
       }
      }
     }
    }
   }

   Button(onClick = onCrear, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
    Icon(Icons.Default.Add, contentDescription = null)
    Text(" Crear presupuesto")
   }
   OutlinedButton(onClick = onCategorias, modifier = Modifier.fillMaxWidth()) {
    Text("Categorías y alertas")
   }
   TextButton(onClick = onReportes, modifier = Modifier.align(Alignment.CenterHorizontally)) {
    Text("Ver reportes")
   }
  }
 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPresupuestoScreen(
 onVolver: () -> Unit = {},
 onGuardar: (String, String, String) -> Unit = { _, _, _ -> },
 vm: PresupuestoViewModel = viewModel()
) {
 LaunchedEffect(vm) { vm.observar() }
 var categoria by remember { mutableStateOf("") }
 var limite by remember { mutableStateOf("") }
 var periodo by remember { mutableStateOf("Mensual") }
 var expandido by remember { mutableStateOf(false) }

 Scaffold(
  containerColor = fondoPres,
  topBar = {
   CenterAlignedTopAppBar(
    title = { Text("Nuevo presupuesto") },
    navigationIcon = {
     IconButton(onClick = onVolver) {
      Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
     }
    }
   )
  }
 ) { padding ->
  Column(
   modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
   verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
   Text("Establece cuánto deseas gastar en cada categoría.", color = Color.Gray)
   ExposedDropdownMenuBox(
    expanded = expandido,
    onExpandedChange = { expandido = it }
   ) {
    OutlinedTextField(
     value = categoria,
     onValueChange = {},
     modifier = Modifier.menuAnchor().fillMaxWidth(),
     label = { Text("Categoría") },
     readOnly = true,
     trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandido) },
     singleLine = true
    )
    ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
     vm.categorias.forEach { item ->
      DropdownMenuItem(
       text = { Text(item.nombre) },
       onClick = { categoria = item.nombre; expandido = false }
      )
     }
    }
   }
   if (vm.categorias.isEmpty()) {
    Text("Primero crea una categoría en Categorías y alertas.", color = Color.Gray)
   }
   OutlinedTextField(
    value = limite,
    onValueChange = { texto -> limite = texto.filter { it.isDigit() || it == '.' || it == ',' } },
    modifier = Modifier.fillMaxWidth(),
    label = { Text("Límite de gasto") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    leadingIcon = { Text("$") },
    singleLine = true
   )
   Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    listOf("Semanal", "Mensual").forEach { opcion ->
     FilterChip(
      selected = periodo == opcion,
      onClick = { periodo = opcion },
      label = { Text(opcion) }
     )
    }
   }
   Text("La alerta se calcula al alcanzar el 80% del límite.", color = Color.Gray)
   vm.mensaje?.let { Text(it, color = rojoPres) }
   Button(
    onClick = { onGuardar(categoria, limite, periodo) },
    enabled = !vm.cargando && categoria.isNotBlank() && (limite.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0,
    modifier = Modifier.fillMaxWidth()
   ) {
    Text(if (vm.cargando) "Guardando…" else "Guardar presupuesto")
   }
  }
 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasAlertasScreen(
 onVolver: () -> Unit = {},
 onGuardarCategoria: (String, String, Double) -> Unit = { _, _, _ -> },
 vm: PresupuestoViewModel = viewModel()
) {
 LaunchedEffect(vm) { vm.observar() }
 var creando by remember { mutableStateOf(false) }
 var nombre by remember { mutableStateOf("") }
 var limite by remember { mutableStateOf("") }
 var umbral by remember { mutableFloatStateOf(0.8f) }
 var umbralGlobal by remember { mutableFloatStateOf(0.8f) }
 var alertasNuevaCategoria by remember { mutableStateOf(true) }
 var guardadoSolicitado by remember { mutableStateOf(false) }

 // Se espera la confirmación del ViewModel antes de cerrar el formulario.
 LaunchedEffect(vm.mensaje, vm.categorias.size, vm.cargando) {
  val mensaje = vm.mensaje.orEmpty()
  if (guardadoSolicitado && !vm.cargando && mensaje.contains("guardad", ignoreCase = true)) {
   creando = false
   guardadoSolicitado = false
   nombre = ""
   limite = ""
   umbral = 0.8f
   alertasNuevaCategoria = true
  }
 }

 Scaffold(
  containerColor = fondoPres,
  topBar = {
   CenterAlignedTopAppBar(
    title = { Text("Categorías y alertas", fontWeight = FontWeight.Bold) },
    navigationIcon = {
     IconButton(onClick = onVolver) {
      Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
     }
    }
   )
  }
 ) { padding ->
  Column(
   modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(18.dp),
   verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
   Text("Organiza tus gastos y configura los avisos de tus categorías.", color = Color.Gray)
   vm.mensaje?.let { mensaje ->
    Text(
     mensaje,
     color = if (mensaje.contains("guardad", ignoreCase = true)) verdePres else rojoPres
    )
   }
   Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
   ) {
    Text("Mis categorías", fontWeight = FontWeight.Bold, fontSize = 19.sp)
    TextButton(onClick = { creando = !creando; guardadoSolicitado = false }) {
     Icon(Icons.Default.Add, contentDescription = null)
     Text(" Nueva")
    }
   }
   if (vm.cargando && vm.categorias.isEmpty()) {
    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
   }
   vm.categorias.forEach { categoria ->
    Card(
     colors = CardDefaults.cardColors(containerColor = Color.White),
     shape = RoundedCornerShape(14.dp)
    ) {
     Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
     ) {
      Column(Modifier.weight(1f)) {
       Text(categoria.nombre, fontWeight = FontWeight.SemiBold)
       Text(
        "Límite ${dineroPres(categoria.limite)} · alerta al ${(categoria.umbral * 100).toInt()}%",
        color = Color.Gray,
        fontSize = 13.sp
       )
      }
      Switch(
       checked = categoria.alertasActivas,
       onCheckedChange = { activas ->
        vm.actualizarAlertasCategoria(categoria, activas, categoria.umbral)
       }
      )
     }
    }
   }
   if (vm.categorias.isEmpty() && !vm.cargando) {
    Text("Todavía no hay categorías. Crea una para comenzar.", color = Color.Gray)
   }

   if (vm.categorias.isNotEmpty()) {
    HorizontalDivider()
    Text("Umbral de alerta", fontWeight = FontWeight.Bold)
    Text("Configura el porcentaje para todas tus categorías.", color = Color.Gray)
    Text("Avisar al ${(umbralGlobal * 100).toInt()}%", fontWeight = FontWeight.Medium)
    Slider(
     value = umbralGlobal,
     onValueChange = { umbralGlobal = it },
     valueRange = 0.5f..1f
    )
    OutlinedButton(
     onClick = {
      vm.categorias.forEach { categoria ->
       vm.actualizarAlertasCategoria(
        categoria,
        categoria.alertasActivas,
        umbralGlobal.toDouble()
       )
      }
     },
     modifier = Modifier.fillMaxWidth(),
     enabled = !vm.cargando
    ) {
     Text("Aplicar umbral a todas")
    }
   }

   if (creando) {
    HorizontalDivider()
    Text("Nueva categoría", fontWeight = FontWeight.Bold, fontSize = 19.sp)
    OutlinedTextField(
     value = nombre,
     onValueChange = { nombre = it.take(40) },
     modifier = Modifier.fillMaxWidth(),
     label = { Text("Nombre de la categoría") },
     singleLine = true
    )
    OutlinedTextField(
     value = limite,
     onValueChange = { texto -> limite = texto.filter { it.isDigit() || it == '.' || it == ',' } },
     modifier = Modifier.fillMaxWidth(),
     label = { Text("Límite mensual de gasto") },
     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
     leadingIcon = { Text("$") },
     singleLine = true
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
     Text("Activar alertas", modifier = Modifier.weight(1f))
     Switch(
      checked = alertasNuevaCategoria,
      onCheckedChange = { alertasNuevaCategoria = it }
     )
    }
    Text("Umbral de alerta: ${(umbral * 100).toInt()}%")
    Slider(
     value = umbral,
     onValueChange = { umbral = it },
     valueRange = 0.5f..1f,
     enabled = alertasNuevaCategoria
    )
    Text(
     "La categoría se guardará con el umbral elegido. La activación de alertas se puede modificar desde la lista.",
     color = Color.Gray,
     fontSize = 12.sp
    )
    Button(
     onClick = {
      guardadoSolicitado = true
      onGuardarCategoria(nombre.trim(), limite, umbral.toDouble())
     },
     enabled = !vm.cargando && !guardadoSolicitado && nombre.isNotBlank() &&
             (limite.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0,
     modifier = Modifier.fillMaxWidth()
    ) {
     Text(if (vm.cargando) "Guardando…" else "Guardar categoría")
    }
    OutlinedButton(
     onClick = { creando = false; guardadoSolicitado = false },
     modifier = Modifier.fillMaxWidth()
    ) {
     Text("Cancelar")
    }
   } else {
    Button(onClick = { creando = true }, modifier = Modifier.fillMaxWidth()) {
     Icon(Icons.Default.Add, contentDescription = null)
     Text(" Nueva categoría")
    }
   }
  }
 }
}
