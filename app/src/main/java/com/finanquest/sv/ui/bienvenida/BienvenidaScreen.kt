package com.finanquest.sv.ui.bienvenida

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.R
import com.finanquest.sv.ui.components.*

// Colores de FinanQuest SV
private val Fondo: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val Morado: Color
    @Composable get() = MaterialTheme.colorScheme.secondary
private val Verde: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val TextoOscuro: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val TextoSecundario: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun BienvenidaScreen(onIniciarSesion: () -> Unit, onCrearCuenta: () -> Unit) {
    QuestVisualTheme {
        Column(
            modifier =
                Modifier.fillMaxSize().background(Fondo).statusBarsPadding().navigationBarsPadding()
        ) {

            // ENCABEZADO
            Box(
                modifier = Modifier.fillMaxWidth().height(58.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "FinanQuest SV",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoOscuro,
                )
            }

            HorizontalDivider(color = Color(0xFFE3E5EE), thickness = 1.dp)

            // CONTENIDO PRINCIPAL
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(35.dp))

                // LOGO ORIGINAL
                Image(
                    painter = painterResource(id = R.drawable.logo_finanquest),
                    contentDescription = "Logo de FinanQuest SV",
                    modifier = Modifier.fillMaxWidth().height(235.dp),
                    contentScale = ContentScale.Fit,
                )

                Spacer(modifier = Modifier.height(25.dp))

                // FRASE DE BIENVENIDA
                Text(
                    text = "¡Tu aventura financiera comienza aquí!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // DESCRIPCIÓN
                Text(
                    text =
                        "Organiza tu dinero, supera misiones " +
                            "y alcanza tus metas mientras ganas recompensas.",
                    fontSize = 14.sp,
                    color = TextoSecundario,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp,
                )

                Row(
                    Modifier.fillMaxWidth().padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    QuestArtwork(
                        R.drawable.presupuesto_wallet,
                        Modifier.size(54.dp),
                        "Organiza tu dinero",
                    )
                    QuestArtwork(R.drawable.quest_goal, Modifier.size(54.dp), "Cumple tus metas")
                    QuestArtwork(R.drawable.quest_reward, Modifier.size(54.dp), "Gana recompensas")
                }
                Spacer(modifier = Modifier.height(8.dp))

                // BOTÓN INICIAR SESIÓN
                BotonBienvenida(texto = "Iniciar Sesión", onClick = onIniciarSesion)

                Spacer(modifier = Modifier.height(18.dp))

                // BOTÓN CREAR CUENTA
                BotonBienvenida(texto = "Crear Cuenta", onClick = onCrearCuenta)

                Spacer(modifier = Modifier.height(36.dp))

                // TEXTO INFERIOR
                Text(
                    text = "APRENDE · AHORRA · CUMPLE TUS METAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Morado,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.7.sp,
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

// COMPONENTE REUTILIZABLE PARA LOS BOTONES
@Composable
private fun BotonBienvenida(texto: String, onClick: () -> Unit) {

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .height(56.dp)
                .shadow(elevation = 9.dp, shape = RoundedCornerShape(18.dp))
                .background(color = Color.White, shape = RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Morado)

        Spacer(modifier = Modifier.width(10.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Verde,
            modifier = Modifier.size(19.dp),
        )
    }
}
