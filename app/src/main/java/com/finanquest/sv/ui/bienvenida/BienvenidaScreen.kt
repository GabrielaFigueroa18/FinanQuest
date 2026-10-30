
package com.finanquest.sv.ui.bienvenida

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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

// Colores de FinanQuest SV
private val Fondo = Color(0xFFF4F5FA)
private val Morado = Color(0xFF7C3AED)
private val Verde = Color(0xFF16A34A)
private val TextoOscuro = Color(0xFF172033)
private val TextoSecundario = Color(0xFF667085)

@Composable
fun BienvenidaScreen(
    onIniciarSesion: () -> Unit,
    onCrearCuenta: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        // ENCABEZADO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "FinanQuest SV",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro
            )
        }

        HorizontalDivider(
            color = Color(0xFFE3E5EE),
            thickness = 1.dp
        )

        // CONTENIDO PRINCIPAL
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(35.dp))

            // LOGO ORIGINAL
            Image(
                painter = painterResource(
                    id = R.drawable.logo_finanquest
                ),
                contentDescription = "Logo de FinanQuest SV",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(25.dp))

            // FRASE DE BIENVENIDA
            Text(
                text = "¡Tu aventura financiera comienza aquí!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // DESCRIPCIÓN
            Text(
                text = "Organiza tu dinero, supera misiones " +
                        "y alcanza tus metas mientras ganas recompensas.",
                fontSize = 14.sp,
                color = TextoSecundario,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // BOTÓN INICIAR SESIÓN
            BotonBienvenida(
                texto = "Iniciar Sesión",
                onClick = onIniciarSesion
            )

            Spacer(modifier = Modifier.height(18.dp))

            // BOTÓN CREAR CUENTA
            BotonBienvenida(
                texto = "Crear Cuenta",
                onClick = onCrearCuenta
            )

            Spacer(modifier = Modifier.height(36.dp))

            // TEXTO INFERIOR
            Text(
                text = "APRENDE · AHORRA · CUMPLE TUS METAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Morado,
                textAlign = TextAlign.Center,
                letterSpacing = 0.7.sp
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// COMPONENTE REUTILIZABLE PARA LOS BOTONES
@Composable
private fun BotonBienvenida(
    texto: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 9.dp,
                shape = RoundedCornerShape(18.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = texto,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Morado
        )

        Spacer(modifier = Modifier.width(10.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Verde,
            modifier = Modifier.size(19.dp)
        )
    }
}
