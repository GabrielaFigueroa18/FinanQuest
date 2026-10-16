
package com.finanquest.sv.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.viewmodel.AuthViewModel
import java.util.Locale

private val Fondo = Color(0xFFF7F8FA)
private val Verde = Color(0xFF16A34A)
private val Morado = Color(0xFF7C3AED)
private val Rojo = Color(0xFFEF4444)
private val Azul = Color(0xFF5086FF)
private val Naranja = Color(0xFFF59E0B)
private val Texto = Color(0xFF22252B)
private val Gris = Color(0xFF7B808A)
private val Borde = Color(0xFFE5E7EB)

private fun formatoDinero(valor: Double): String {
    return String.format(Locale.US, "$%,.2f", valor)
}

@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    onMovimientos: () -> Unit = {},
    onMetas: () -> Unit = {},
    onMascota: () -> Unit = {},
    onNiveles: () -> Unit = {},
    onPerfil: () -> Unit = {},
    onPresupuestos: () -> Unit = {},
    onNuevoGasto: () -> Unit = {},
    onNuevaMeta: () -> Unit = {},
    onNuevaCategoria: () -> Unit = {},
    onReportes: () -> Unit = {},
    onCuentas: () -> Unit = {},
    onRecompensas: () -> Unit = {},
    onNotificaciones: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        authViewModel.cargarPerfil()
    }

    val usuario = authViewModel.usuario

    val primerNombre = usuario?.nombre
        ?.trim()
        ?.split(" ")
        ?.firstOrNull()
        ?.takeUnless { it.isNullOrBlank() }
        ?: "Estudiante"

    val nivel = usuario?.nivel ?: 1
    val xp = usuario?.xp ?: 0

    // DATOS DE DEMOSTRACIÓN
    // Luego se reemplazarán por datos reales de Firebase.
    val ingresos = 2400.00
    val gastos = 1154.50
    val saldo = ingresos - gastos

    Scaffold(
        containerColor = Fondo,
        topBar = {
            EncabezadoDashboard(
                onNotificaciones = onNotificaciones
            )
        },
        bottomBar = {
            MenuInferiorDashboard(
                onMovimientos = onMovimientos,
                onMetas = onMetas,
                onMascota = onMascota,
                onNiveles = onNiveles,
                onPerfil = onPerfil
            )
        }
    ) { paddingInterno ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Hola, $primerNombre 👋",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Texto
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Tu resumen de hoy se ve genial.",
                fontSize = 13.sp,
                color = Gris
            )

            Spacer(Modifier.height(16.dp))

            MensajeMascota(nivel = nivel)

            Spacer(Modifier.height(20.dp))

            TarjetaSaldo(
                saldo = saldo,
                ingresos = ingresos,
                gastos = gastos
            )

            Spacer(Modifier.height(18.dp))

            ResumenGamificacion(
                racha = 12,
                metas = 3,
                nivel = nivel
            )

            Spacer(Modifier.height(20.dp))

            AlertaPresupuesto(
                texto = "Has superado el 80% de tu presupuesto en Alimentación.",
                onClick = onPresupuestos
            )

            Spacer(Modifier.height(18.dp))

            TarjetaMision()

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Presupuestos",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Texto
                )

                TextButton(onClick = onPresupuestos) {
                    Text(
                        text = "VER TODO",
                        color = Verde,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            ResumenPresupuestos()

            Spacer(Modifier.height(20.dp))

            AccionesRapidas(
                onNuevoGasto = onNuevoGasto,
                onNuevaMeta = onNuevaMeta,
                onNuevaCategoria = onNuevaCategoria,
                onReportes = onReportes
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    18.dp,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedButton(
                    onClick = onCuentas,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, Rojo)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = Rojo,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "MIS CUENTAS",
                        color = Rojo,
                        fontSize = 11.sp
                    )
                }

                FilledIconButton(
                    onClick = onRecompensas,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Morado
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Recompensas",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            Text(
                text = "Resumen de demostración · XP actual: $xp",
                modifier = Modifier.fillMaxWidth(),
                color = Gris,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EncabezadoDashboard(
    onNotificaciones: () -> Unit
) {
    Surface(
        color = Fondo,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(29.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Verde),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(9.dp))

            Text(
                text = "FinanQuest SV",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Texto
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onNotificaciones) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notificaciones",
                    tint = Texto
                )
            }
        }
    }
}

@Composable
private fun MensajeMascota(
    nivel: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEDE9FE)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🐱",
                fontSize = 42.sp
            )
        }

        Spacer(Modifier.width(10.dp))

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(17.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Borde),
            shadowElevation = 2.dp
        ) {
            Text(
                text = "¡Estás a solo 150 XP de subir al Nivel ${nivel + 1}! ¡Sigue así!",
                modifier = Modifier.padding(12.dp),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Texto
            )
        }
    }
}

@Composable
private fun TarjetaSaldo(
    saldo: Double,
    ingresos: Double,
    gastos: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Verde
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {

            Text(
                text = "SALDO DISPONIBLE",
                color = Color(0xFFD7FBE5),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = formatoDinero(saldo),
                color = Color.White,
                fontSize = 33.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(23.dp))

            HorizontalDivider(
                color = Color(0xFF70D99A)
            )

            Spacer(Modifier.height(15.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(Modifier.width(5.dp))

                        Text(
                            "INGRESOS",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(Modifier.height(5.dp))

                    Text(
                        formatoDinero(ingresos),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.TrendingDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(Modifier.width(5.dp))

                        Text(
                            "GASTOS",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(Modifier.height(5.dp))

                    Text(
                        formatoDinero(gastos),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumenGamificacion(
    racha: Int,
    metas: Int,
    nivel: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {

        TarjetaEstadistica(
            icono = Icons.Filled.LocalFireDepartment,
            colorIcono = Color(0xFFF97316),
            valor = "$racha",
            etiqueta = "RACHA DÍAS",
            modifier = Modifier.weight(1f)
        )

        TarjetaEstadistica(
            icono = Icons.Filled.TrackChanges,
            colorIcono = Verde,
            valor = "$metas",
            etiqueta = "METAS",
            modifier = Modifier.weight(1f)
        )

        TarjetaEstadistica(
            icono = Icons.Filled.EmojiEvents,
            colorIcono = Morado,
            valor = "Lvl $nivel",
            etiqueta = "NIVEL",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TarjetaEstadistica(
    icono: ImageVector,
    colorIcono: Color,
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = Fondo,
        border = BorderStroke(1.dp, Borde)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = valor,
                color = Texto,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = etiqueta,
                color = Gris,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AlertaPresupuesto(
    texto: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFFF3F2),
        border = BorderStroke(
            1.dp,
            Color(0xFFFFC9C4)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFDC2626)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = texto,
                color = Color(0xFFB91C1C),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Rojo,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TarjetaMision() {
    val progreso = 0.65f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = Fondo,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = Naranja
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Misión Activa",
                    color = Texto,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "+200 XP",
                    color = Morado,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Ahorra $20.00 en entretenimiento esta semana.",
                color = Gris,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(13.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    "PROGRESO DE MISIÓN",
                    color = Gris,
                    fontSize = 11.sp
                )

                Text(
                    "65% de $20.00",
                    color = Texto,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))

            BarraProgreso(
                progreso = progreso,
                color = Morado
            )
        }
    }
}

@Composable
private fun BarraProgreso(
    progreso: Float,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE5E7EB))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progreso.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(color)
        )
    }
}

@Composable
private fun ResumenPresupuestos() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = Fondo
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            FilaPresupuesto(
                nombre = "ALIMENTACIÓN",
                porcentaje = 85,
                limite = 200.00,
                color = Rojo
            )

            Spacer(Modifier.height(20.dp))

            FilaPresupuesto(
                nombre = "TRANSPORTE",
                porcentaje = 40,
                limite = 80.00,
                color = Verde
            )

            Spacer(Modifier.height(20.dp))

            FilaPresupuesto(
                nombre = "SUSCRIPCIONES",
                porcentaje = 15,
                limite = 50.00,
                color = Azul
            )
        }
    }
}

@Composable
private fun FilaPresupuesto(
    nombre: String,
    porcentaje: Int,
    limite: Double,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = nombre,
            color = Gris,
            fontSize = 11.sp
        )

        Text(
            text = "$porcentaje% de ${formatoDinero(limite)}",
            color = Texto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }

    Spacer(Modifier.height(9.dp))

    BarraProgreso(
        progreso = porcentaje / 100f,
        color = color
    )
}

@Composable
private fun AccionesRapidas(
    onNuevoGasto: () -> Unit,
    onNuevaMeta: () -> Unit,
    onNuevaCategoria: () -> Unit,
    onReportes: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            BotonAccion(
                texto = "NUEVO GASTO",
                icono = Icons.Filled.Add,
                colorIcono = Azul,
                onClick = onNuevoGasto,
                modifier = Modifier.weight(1f)
            )

            BotonAccion(
                texto = "NUEVA META",
                icono = Icons.Filled.TrackChanges,
                colorIcono = Verde,
                onClick = onNuevaMeta,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            BotonAccion(
                texto = "NUEVA CATEGORÍA",
                icono = Icons.Filled.Add,
                colorIcono = Naranja,
                onClick = onNuevaCategoria,
                modifier = Modifier.weight(1f)
            )

            BotonAccion(
                texto = "REPORTES",
                icono = Icons.Filled.Assessment,
                colorIcono = Morado,
                onClick = onReportes,
                modifier = Modifier.weight(1f),
                borde = Morado
            )
        }
    }
}

@Composable
private fun BotonAccion(
    texto: String,
    icono: ImageVector,
    colorIcono: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borde: Color = Borde
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, borde),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(5.dp))

            Text(
                text = texto,
                color = if (borde == Morado) Morado else Texto,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MenuInferiorDashboard(
    onMovimientos: () -> Unit,
    onMetas: () -> Unit,
    onMascota: () -> Unit,
    onNiveles: () -> Unit,
    onPerfil: () -> Unit
) {
    NavigationBar(
        containerColor = Fondo,
        tonalElevation = 3.dp
    ) {

        ItemMenu(
            texto = "Inicio",
            icono = Icons.Filled.Home,
            seleccionado = true,
            onClick = {}
        )

        ItemMenu(
            texto = "Movimientos",
            icono = Icons.Filled.AccountBalanceWallet,
            seleccionado = false,
            onClick = onMovimientos
        )

        ItemMenu(
            texto = "Metas",
            icono = Icons.Filled.TrackChanges,
            seleccionado = false,
            onClick = onMetas
        )

        ItemMenu(
            texto = "Mascota",
            icono = Icons.Filled.Pets,
            seleccionado = false,
            onClick = onMascota
        )

        ItemMenu(
            texto = "Nivel",
            icono = Icons.Filled.EmojiEvents,
            seleccionado = false,
            onClick = onNiveles
        )

        ItemMenu(
            texto = "Perfil",
            icono = Icons.Filled.PersonOutline,
            seleccionado = false,
            onClick = onPerfil
        )
    }
}

@Composable
private fun RowScope.ItemMenu(
    texto: String,
    icono: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = seleccionado,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icono,
                contentDescription = texto,
                modifier = Modifier.size(21.dp)
            )
        },
        label = {
            Text(
                text = texto,
                fontSize = 9.sp,
                maxLines = 1
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Morado,
            selectedTextColor = Morado,
            indicatorColor = Color(0xFFEDE9FE),
            unselectedIconColor = Gris,
            unselectedTextColor = Gris
        )
    )
}
