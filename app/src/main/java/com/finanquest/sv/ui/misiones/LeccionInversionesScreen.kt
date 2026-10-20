package com.finanquest.sv.ui.misiones
import com.finanquest.sv.network.MisionesApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VerdeLeccion = Color(0xFF16A34A)
private val MoradoLeccion = Color(0xFF9B70FA)
private val FondoLeccion = Color(0xFFF7F8FA)

private data class PreguntaInversion(val texto: String, val opciones: List<String>, val correcta: Int)

private val preguntasInversion = listOf(
    PreguntaInversion("¿Qué significa invertir?", listOf("Guardar dinero debajo del colchón", "Destinar recursos buscando un rendimiento futuro", "Gastar todo el salario"), 1),
    PreguntaInversion("¿Qué es un fondo de inversión?", listOf("Un préstamo sin intereses", "Una cuenta para pagar servicios", "Un patrimonio que reúne aportes de inversionistas y sigue una estrategia"), 2),
    PreguntaInversion("¿Qué relación existe entre riesgo y rendimiento?", listOf("Un rendimiento alto está garantizado", "Generalmente, buscar mayor rendimiento implica asumir más riesgo", "Toda inversión tiene el mismo riesgo"), 1),
    PreguntaInversion("¿Qué es diversificar?", listOf("Distribuir el dinero entre distintas inversiones", "Invertir todo en una sola empresa", "Retirar el dinero diariamente"), 0),
    PreguntaInversion("Antes de invertir, ¿qué conviene revisar?", listOf("Solo la publicidad", "Solo lo que recomiendan las redes sociales", "Riesgos, comisiones, liquidez y regulación"), 2)
)

@Composable
fun LeccionInversionesScreen(onVolver: () -> Unit, onAprobado: () -> Unit = {}) {
    val respuestas = remember { mutableStateListOf<Int?>().apply { repeat(preguntasInversion.size) { add(null) } } }
    var resultado by remember { mutableStateOf<Int?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var guardado by remember { mutableStateOf(false) }
    var errorGuardado by remember { mutableStateOf<String?>(null) }
    val correctas = resultado
    var xpOtorgados by remember { mutableIntStateOf(0) }
    var recompensaYaOtorgada by remember { mutableStateOf(false) }

    fun guardarResultado() {
        if (guardando || guardado) return
        if (respuestas.any { it == null }) {
            errorGuardado = "Responde todas las preguntas."
            return
        }

        guardando = true
        errorGuardado = null

        MisionesApi.completarInversionistaNovato(
            respuestas = respuestas.map { it!! },
            onResultado = { respuesta ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    guardando = false
                    if (respuesta.optBoolean("ok", false)) {
                        resultado = respuesta.optInt("correctas", 0)
                        xpOtorgados = respuesta.optInt("xpOtorgados", 0)
                        recompensaYaOtorgada = respuesta.optBoolean("recompensaYaOtorgada", false)
                        guardado = true
                    } else {
                        errorGuardado = "No se pudo registrar la evaluación."
                    }
                }
            },
            onError = { mensaje ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    guardando = false
                    errorGuardado = mensaje
                }
            }
        )
    }

    Scaffold(containerColor = FondoLeccion, topBar = {
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().height(62.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                Text("Inversionista Novato", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 20.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F6ED)), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.School, null, tint = VerdeLeccion)
                        Spacer(Modifier.width(10.dp))
                        Text("Aprende a invertir", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Misión educativa · Recompensa prevista: 100 XP", color = VerdeLeccion)
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Lección: primeros pasos en las inversiones", fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Invertir consiste en destinar dinero u otros recursos a una alternativa con la expectativa de obtener un beneficio futuro. A diferencia del ahorro, una inversión puede aumentar o disminuir de valor.", lineHeight = 22.sp)
            Spacer(Modifier.height(12.dp))
            Text("Un fondo de inversión reúne los aportes de varias personas y los administra según una estrategia definida. Cada fondo tiene riesgos, costos y condiciones particulares; no garantiza ganancias.", lineHeight = 22.sp)
            Spacer(Modifier.height(12.dp))
            Text("Diversificar significa distribuir el dinero entre diferentes activos para reducir la dependencia de uno solo. Antes de invertir, es importante entender los riesgos, las comisiones, la facilidad para retirar el dinero y verificar que la entidad esté autorizada por el regulador correspondiente.", lineHeight = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text("Este contenido es educativo y no constituye una recomendación de inversión.", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(24.dp))
            Text("Evaluación de conocimientos", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Responde las 5 preguntas. Necesitas 4 correctas para aprobar.", fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            preguntasInversion.forEachIndexed { index, pregunta ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${index + 1}. ${pregunta.texto}", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        pregunta.opciones.forEachIndexed { opcionIndex, opcion ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = respuestas[index] == opcionIndex, onClick = { if (resultado == null) respuestas[index] = opcionIndex }, enabled = resultado == null, colors = RadioButtonDefaults.colors(selectedColor = MoradoLeccion))
                                Text(opcion, modifier = Modifier.weight(1f), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (correctas == null) {
                Button(onClick = {
                    resultado = preguntasInversion.indices.count { respuestas[it] == preguntasInversion[it].correcta }
                    guardado = false
                    errorGuardado = null
                }, enabled = respuestas.all { it != null }, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdeLeccion)) {
                    Text("Finalizar evaluación")
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = if (correctas >= 4) Color(0xFFE7F6ED) else Color(0xFFFFF3E6))) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (correctas >= 4) "¡Evaluación aprobada!" else "Puedes intentarlo nuevamente", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("$correctas de 5 respuestas correctas · ${correctas * 20}%", fontSize = 15.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(if (correctas >= 4) "Aprobaste la evaluación. Registra tu resultado para actualizar la misión." else "Repasa la lección y vuelve a responder.", fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (!guardado) {
                    Button(onClick = { guardarResultado() }, enabled = !guardando, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = VerdeLeccion)) {
                        Text(if (guardando) "Guardando..." else "Registrar resultado")
                    }
                } else {
                    Text("Resultado registrado correctamente en el servidor.", color = VerdeLeccion)
                    if (correctas >= 4) {
                        Text(
                            when {
                                xpOtorgados > 0 -> "¡Ganaste $xpOtorgados XP!"
                                recompensaYaOtorgada -> "Ya habías recibido los XP de esta misión."
                                else -> "Misión aprobada."
                            },
                            color = VerdeLeccion
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    if (correctas >= 4) {
                        Button(onClick = onAprobado, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = VerdeLeccion)) { Text("Volver a Mis Misiones") }
                    }
                }
                errorGuardado?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (correctas < 4 && guardado) {
                    OutlinedButton(onClick = {
                        resultado = null
                        guardado = false
                        errorGuardado = null
                        xpOtorgados = 0
                        recompensaYaOtorgada = false
                        for (i in respuestas.indices) respuestas[i] = null
                    }, modifier = Modifier.fillMaxWidth()) { Text("Volver a intentar") }
                }
            }
            Spacer(Modifier.height(26.dp))
        }
    }
}
