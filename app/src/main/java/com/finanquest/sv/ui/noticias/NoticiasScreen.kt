package com.finanquest.sv.ui.noticias

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


private const val BACKEND_URL = "http://192.168.0.53:3001"

private data class Noticia(
    val titulo: String,
    val descripcion: String,
    val url: String,
    val fuente: String,
    val fecha: String,
    val imagen: String
)

private suspend fun cargarNoticias(tema: String, consulta: String): List<Noticia> =
    withContext(Dispatchers.IO) {
        val parametros = if (consulta.isNotBlank()) {
            "q=${URLEncoder.encode(consulta, "UTF-8")}"
        } else {
            "tema=${URLEncoder.encode(tema, "UTF-8")}"
        }

        val conexion = (URL("$BACKEND_URL/api/noticias?$parametros")
            .openConnection() as HttpURLConnection)

        try {
            conexion.requestMethod = "GET"
            conexion.connectTimeout = 12000
            conexion.readTimeout = 12000
            conexion.setRequestProperty("Accept", "application/json")

            val codigo = conexion.responseCode
            val contenido = (if (codigo in 200..299) conexion.inputStream else conexion.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (codigo !in 200..299) {
                val mensaje = runCatching {
                    JSONObject(contenido).optString("mensaje")
                }.getOrNull().orEmpty()
                throw IllegalStateException(
                    "El servidor respondió HTTP $codigo" +
                            if (mensaje.isNotBlank()) ": $mensaje" else ""
                )
            }

            val json = JSONObject(contenido)
            if (!json.optBoolean("ok", false)) {
                throw IllegalStateException(
                    json.optString("mensaje", "No se pudieron cargar las noticias")
                )
            }

            val lista = json.optJSONArray("noticias") ?: return@withContext emptyList()
            List(lista.length()) { indice ->
                val item = lista.getJSONObject(indice)
                Noticia(
                    titulo = item.optString("titulo"),
                    descripcion = item.optString("descripcion"),
                    url = item.optString("url"),
                    fuente = item.optString("fuente"),
                    fecha = item.optString("fecha"),
                    imagen = item.optString("imagen")
                )
            }
        } finally {
            conexion.disconnect()
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticiasScreen(onVolver: () -> Unit) {
    val contexto = LocalContext.current
    var tema by remember { mutableStateOf("todas") }
    var textoBusqueda by remember { mutableStateOf("") }
    var consulta by remember { mutableStateOf("") }
    var reintentos by remember { mutableIntStateOf(0) }
    var noticias by remember { mutableStateOf<List<Noticia>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tema, consulta, reintentos) {
        cargando = true
        error = null
        try {
            noticias = cargarNoticias(tema, consulta)
        } catch (e: java.net.ConnectException) {
            noticias = emptyList()
            error = "No se pudo conectar a $BACKEND_URL. Comprueba que el backend esté encendido en el puerto 3001."
        } catch (e: java.net.SocketTimeoutException) {
            noticias = emptyList()
            error = "La conexión tardó demasiado. Comprueba el servidor y vuelve a intentar."
        } catch (e: Exception) {
            noticias = emptyList()
            error = e.localizedMessage ?: "No se pudieron cargar las noticias."
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Noticias Financieras", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onVolver) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text("Entérate de las novedades sobre economía, ahorro e inversiones")
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar noticias") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { consulta = textoBusqueda.trim(); reintentos++ },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Buscar") }
            Spacer(Modifier.height(8.dp))

            // Se desplaza horizontalmente para evitar que los filtros se corten.
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    listOf(
                        "todas" to "Todas",
                        "ahorro" to "Ahorro",
                        "economia" to "Economía",
                        "inversiones" to "Inversión"
                    )
                ) { (clave, etiqueta) ->
                    FilterChip(
                        selected = tema == clave && consulta.isEmpty(),
                        onClick = {
                            tema = clave
                            consulta = ""
                            textoBusqueda = ""
                            reintentos++
                        },
                        label = { Text(etiqueta, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            when {
                cargando -> CircularProgressIndicator()
                error != null -> {
                    Text(
                        "Error al cargar noticias: ${error.orEmpty()}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { reintentos++ }) { Text("Reintentar") }
                }
                noticias.isEmpty() -> Text("No se encontraron noticias.")
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(noticias) { noticia ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (noticia.url.startsWith("https://") ||
                                    noticia.url.startsWith("http://")) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(noticia.url))
                                    runCatching { contexto.startActivity(intent) }
                                }
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(noticia.fuente, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(6.dp))
                                Text(noticia.titulo, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text(noticia.descripcion)
                                Spacer(Modifier.height(10.dp))
                                Text("Leer noticia completa ›", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
