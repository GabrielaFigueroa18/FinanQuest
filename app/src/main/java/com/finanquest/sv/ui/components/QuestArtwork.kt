package com.finanquest.sv.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.R
import java.util.Locale

internal val opcionesMeta =
    listOf(
        "🎯" to "Ahorro",
        "💻" to "Estudios",
        "✈️" to "Viaje",
        "🚘" to "Carro",
        "🏠" to "Vivienda",
        "🎓" to "Educación",
        "🛡️" to "Emergencia",
    )

@DrawableRes
internal fun imagenMeta(icono: String, nombre: String = ""): Int {
    val texto = nombre.lowercase(Locale.ROOT)
    return when {
        icono == "✈️" || texto.contains("viaje") || texto.contains("vacacion") ->
            R.drawable.quest_travel
        icono == "🚘" || texto.contains("carro") || texto.contains("auto") -> R.drawable.quest_car
        icono == "🏠" || texto.contains("casa") || texto.contains("vivienda") ->
            R.drawable.quest_home
        icono == "💻" ||
            icono == "🎓" ||
            texto.contains("estudio") ||
            texto.contains("laptop") ||
            texto.contains("universidad") -> R.drawable.presupuesto_education
        else -> R.drawable.quest_goal
    }
}

@Composable
internal fun QuestArtwork(
    @DrawableRes recurso: Int,
    modifier: Modifier = Modifier.size(64.dp),
    descripcion: String? = null,
) {
    Image(
        painterResource(recurso),
        contentDescription = descripcion,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Composable
internal fun QuestFeatureBanner(
    titulo: String,
    descripcion: String,
    @DrawableRes imagen: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .5f)),
    ) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            QuestArtwork(imagen, Modifier.size(72.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    descripcion,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@DrawableRes
internal fun imagenCategoria(nombre: String): Int =
    when {
        nombre.contains("comida", true) || nombre.contains("alimenta", true) ->
            R.drawable.presupuesto_food
        nombre.contains("transporte", true) -> R.drawable.presupuesto_transport
        nombre.contains("educa", true) -> R.drawable.presupuesto_education
        nombre.contains("vivienda", true) -> R.drawable.quest_home
        else -> R.drawable.presupuesto_wallet
    }

@DrawableRes
internal fun imagenAccesorio(id: String): Int =
    when (id) {
        "ninguno" -> R.drawable.financat_hero
        "sombrero" -> R.drawable.quest_hat
        "lentes" -> R.drawable.quest_glasses
        "corona" -> R.drawable.quest_crown
        else -> R.drawable.quest_reward
    }
