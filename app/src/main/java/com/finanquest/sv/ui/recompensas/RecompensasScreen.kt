package com.finanquest.sv.ui.recompensas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.viewmodel.RecompensasViewModel

private val Morado = Color(0xFF7C3AED)
private val Verde = Color(0xFF16A34A)
private val Fondo = Color(0xFFF6F7F9)

// Se conserva el modelo para mantener compatible DetalleRecompensaScreen y AppNavigation.
data class Recompensa(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val icono: String,
    val requisito: String,
    val desbloqueada: Boolean = false
)

val catalogoRecompensas = listOf(
    Recompensa("ahorrador", "Ahorrador", "Reconocimiento por comenzar a ahorrar.", "Insignias", "🏅", "Ahorrar un total de $100"),
    Recompensa("constante", "Constante", "Reconocimiento por llevar un registro de gastos.", "Insignias", "📅", "Registrar gastos durante 7 días"),
    Recompensa("maestro", "Maestro financiero", "Insignia para quienes alcanzan un nivel avanzado.", "Insignias", "🏆", "Alcanzar el nivel 10"),
    Recompensa("sombrero", "Sombrero financiero", "Personaliza tu mascota con un sombrero.", "Accesorios", "🎩", "Completar el reto correspondiente"),
    Recompensa("lentes", "Lentes inteligentes", "Un accesorio para tu mascota.", "Accesorios", "👓", "Desbloquear los lentes"),
    Recompensa("corona", "Corona financiera", "Una corona para los grandes ahorradores.", "Accesorios", "👑", "Completar el reto de la corona"),
    Recompensa("fondo_morado", "Fondo violeta", "Personaliza el fondo de tu mascota.", "Fondos", "💜", "Desbloquear el fondo violeta"),
    Recompensa("fondo_verde", "Fondo naturaleza", "Un fondo de color verde para tu mascota.", "Fondos", "🌿", "Desbloquear el fondo naturaleza")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecompensasScreen(
    onVolver: () -> Unit = {},
    onDetalle: (Recompensa) -> Unit = {},
    recompensasDesbloqueadas: Set<String> = emptySet()
) {
    val vm: RecompensasViewModel = viewModel()

    DisposableEffect(vm) {
        vm.iniciarEscucha()
        onDispose { vm.detenerEscucha() }
    }

    // También se admite el conjunto proporcionado por navegación, si lo hubiera.
    val desbloqueadas = vm.recompensasDesbloqueadas + recompensasDesbloqueadas
    val categorias = listOf("Insignias", "Accesorios", "Fondos")
    var pestana by remember { mutableIntStateOf(0) }
    var soloDesbloqueadas by remember { mutableStateOf(false) }

    val disponibles = catalogoRecompensas.filter { recompensa ->
        recompensa.categoria == categorias[pestana] &&
                (!soloDesbloqueadas || recompensa.id in desbloqueadas)
    }
    val totalObtenidas = catalogoRecompensas.count { it.id in desbloqueadas }
    val progreso = totalObtenidas.toFloat() / catalogoRecompensas.size

    Scaffold(
        containerColor = Fondo,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis recompensas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("🏆 Tu colección", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "$totalObtenidas de ${catalogoRecompensas.size} recompensas desbloqueadas",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = Verde,
                        trackColor = Color(0xFFEDE9FE)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${(progreso * 100).toInt()}% completado",
                        color = Morado,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            if (vm.cargando) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Morado
                )
            }

            vm.error?.let { mensaje ->
                Text(
                    text = "No se pudieron cargar las recompensas: $mensaje",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Text(
                "Completa desafíos para desbloquear premios.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Gray,
                fontSize = 13.sp
            )

            TabRow(
                selectedTabIndex = pestana,
                containerColor = Color.White,
                contentColor = Morado
            ) {
                categorias.forEachIndexed { indice, categoria ->
                    Tab(
                        selected = pestana == indice,
                        onClick = { pestana = indice },
                        text = { Text(categoria, fontSize = 13.sp) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Solo desbloqueadas", modifier = Modifier.weight(1f), fontSize = 13.sp)
                Switch(
                    checked = soloDesbloqueadas,
                    onCheckedChange = { soloDesbloqueadas = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Verde)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (disponibles.isEmpty()) {
                    item {
                        Text(
                            "Todavía no tienes recompensas desbloqueadas en esta categoría.",
                            modifier = Modifier.padding(vertical = 28.dp),
                            color = Color.Gray
                        )
                    }
                }

                items(disponibles, key = { it.id }) { recompensa ->
                    val desbloqueada = recompensa.id in desbloqueadas
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDetalle(recompensa.copy(desbloqueada = desbloqueada))
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE8E7EE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(recompensa.icono, fontSize = 32.sp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(recompensa.nombre, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    recompensa.descripcion,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    if (desbloqueada) "Desbloqueada" else "Bloqueada",
                                    color = if (desbloqueada) Verde else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                if (desbloqueada) Icons.Default.Star else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (desbloqueada) Verde else Color.Gray
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Ver detalle",
                                tint = Morado
                            )
                        }
                    }
                }
            }
        }
    }
}
