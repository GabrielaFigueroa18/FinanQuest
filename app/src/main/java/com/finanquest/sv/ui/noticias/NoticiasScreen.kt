package com.finanquest.sv.ui.noticias

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

// Dirección IP de tu computadora en la red Wi-Fi.
// Si cambia la IP de tu PC, actualiza este valor.
private const val BACKEND_URL = "http://192.168.0.53:3001"

private val Morado = Color(0xFF6B4EB0)
private val MoradoOscuro = Color(0xFF241653)
private val Lila = Color(0xFFF0EBFC)
private val Fondo = Color(0xFFFAF8FF)
private val TextoSecundario = Color(0xFF79738F)

private data class Noticia(
    val titulo: String,
    val descripcion: String,
    val imagen: String,
    val url: String,
    val fecha: String,
    val fuente: String
)

@Composable
fun NoticiasScreen(onVolver: () -> Unit) {
    var busqueda by remember { mutableStateOf("") }
    var consulta by remember { mutableStateOf("") }
    var tema by remember { mutableStateOf("Todas") }
    var noticias by remember { mutableStateOf<List<Noticia>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableIntStateOf(0) }
    val contexto = LocalContext.current

    LaunchedEffect(tema, consulta, recargar) {
        cargando = true
        error = null
        try {
            noticias = cargarNoticias(tema, consulta)
        } catch (e: Exception) {
            noticias = emptyList()
            error = when (e) {
                is SocketTimeoutException -> "La conexión tardó demasiado. Inténtalo otra vez."
                else -> e.message ?: "No se pudieron cargar las noticias."
            }
        } finally {
            cargando = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Morado)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Noticias Financieras", color = MoradoOscuro, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Entérate de las novedades sobre economía, ahorro e inversiones",
                    color = TextoSecundario,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar noticias...", color = TextoSecundario) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Morado) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Morado,
                        unfocusedBorderColor = Color(0xFFE4DCF5),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { consulta = busqueda.trim() })
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { consulta = busqueda.trim(); recargar++ },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Morado)
                ) { Text("Buscar noticias", fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Todas", "Ahorro", "Economía", "Inversión").forEach { opcion ->
                        val seleccionado = tema == opcion
                        Surface(
                            modifier = Modifier.weight(1f).clickable {
                                tema = opcion
                                consulta = ""
                                busqueda = ""
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (seleccionado) Morado else Lila
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 11.dp, horizontal = 2.dp)) {
                                Text(
                                    opcion,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    color = if (seleccionado) Color.White else MoradoOscuro,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            when {
                cargando -> item {
                    Box(Modifier.fillMaxWidth().padding(45.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Morado)
                    }
                }
                error != null -> item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No pudimos cargar las noticias", fontWeight = FontWeight.Bold, color = MoradoOscuro)
                            Spacer(Modifier.height(8.dp))
                            Text(error.orEmpty(), color = TextoSecundario, fontSize = 13.sp)
                            Spacer(Modifier.height(14.dp))
                            Button(onClick = { recargar++ }, colors = ButtonDefaults.buttonColors(containerColor = Morado)) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                noticias.isEmpty() -> item {
                    Text("No encontramos noticias para esta búsqueda.", color = TextoSecundario, modifier = Modifier.padding(18.dp))
                }
                else -> items(noticias, key = { it.url.ifBlank { it.titulo } }) { noticia ->
                    TarjetaNoticia(noticia = noticia, onAbrir = {
                        if (noticia.url.isNotBlank()) {
                            contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(noticia.url)))
                        }
                    })
                }
            }
        }
    }
}

@Composable
private fun TarjetaNoticia(noticia: Noticia, onAbrir: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(15.dp)) {
            if (noticia.imagen.isNotBlank()) {
                AsyncImage(
                    model = noticia.imagen,
                    contentDescription = noticia.titulo,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(15.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Lila, shape = RoundedCornerShape(9.dp)) {
                    Text("${noticia.fuente.ifBlank { "Noticias" }}", modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontSize = 11.sp, color = Morado)
                }
                Spacer(Modifier.weight(1f))
                Text(formatearFecha(noticia.fecha), fontSize = 11.sp, color = TextoSecundario)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                noticia.titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                color = MoradoOscuro,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (noticia.descripcion.isNotBlank()) {
                Spacer(Modifier.height(7.dp))
                Text(
                    noticia.descripcion,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = TextoSecundario,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onAbrir, enabled = noticia.url.isNotBlank()) {
                    Text("Leer noticia completa", color = Morado, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Morado, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

private fun formatearFecha(fecha: String): String {
    if (fecha.isBlank()) return ""
    return try {
        val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val salida = SimpleDateFormat("dd MMM yyyy", Locale("es"))
        val parsed = entrada.parse(fecha.take(19))
        if (parsed != null) salida.format(parsed) else fecha.take(10)
    } catch (_: Exception) { fecha.take(10) }
}

private suspend fun cargarNoticias(tema: String, consulta: String): List<Noticia> = withContext(Dispatchers.IO) {
    val parametros = if (consulta.isNotBlank()) {
        "q=${URLEncoder.encode(consulta, "UTF-8")}"
    } else {
        val temaApi = when (tema) {
            "Todas" -> "todas"
            "Economía" -> "economia"
            "Inversión" -> "inversion"
            "Ahorro" -> "ahorro"
            else -> tema.lowercase(Locale.ROOT)
        }
        "tema=${URLEncoder.encode(temaApi, "UTF-8")}"
    }
    val conexion = URL("$BACKEND_URL/api/noticias?$parametros").openConnection() as HttpURLConnection
    try {
        conexion.requestMethod = "GET"
        conexion.connectTimeout = 15000
        conexion.readTimeout = 15000
        conexion.setRequestProperty("Accept", "application/json")
        val codigo = conexion.responseCode
        val contenido = (if (codigo in 200..299) conexion.inputStream else conexion.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (codigo !in 200..299) {
            val mensaje = runCatching { JSONObject(contenido).optString("mensaje") }.getOrNull()
            throw Exception("HTTP $codigo: ${mensaje?.ifBlank { null } ?: "Error del servidor"}")
        }
        val json = JSONObject(contenido)
        if (!json.optBoolean("ok", false)) throw Exception(json.optString("mensaje", "Error al cargar noticias"))
        val arreglo = json.optJSONArray("noticias") ?: return@withContext emptyList()
        List(arreglo.length()) { i ->
            val item = arreglo.getJSONObject(i)
            Noticia(
                titulo = item.optString("titulo"),
                descripcion = item.optString("descripcion"),
                imagen = item.optString("imagen").takeUnless { it == "null" }.orEmpty(),
                url = item.optString("url"),
                fecha = item.optString("fecha"),
                fuente = item.optString("fuente")
            )
        }
    } finally { conexion.disconnect() }
}
