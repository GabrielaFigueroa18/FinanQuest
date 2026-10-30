package com.finanquest.sv.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.R
import com.finanquest.sv.ui.components.*
import com.finanquest.sv.viewmodel.AuthViewModel

private val Fondo: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val Verde: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val Morado: Color
    @Composable get() = MaterialTheme.colorScheme.secondary
private val Texto: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val Gris: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val Borde: Color
    @Composable get() = MaterialTheme.colorScheme.outline

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: () -> Unit,
    onIrRegistro: () -> Unit,
    onVolver: () -> Unit,
) {
    QuestVisualTheme {
        var correo by remember { mutableStateOf("") }
        var contrasena by remember { mutableStateOf("") }
        var mostrarContrasena by remember { mutableStateOf(false) }
        var mostrarRecuperacion by remember { mutableStateOf(false) }

        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(Fondo)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Texto,
                    )
                }
            }

            QuestArtwork(R.drawable.financat_hero, Modifier.size(112.dp))
            Spacer(Modifier.height(16.dp))

            Text(
                text = "¡Bienvenido de nuevo!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Texto,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Inicia sesión para continuar tu aventura financiera.",
                fontSize = 14.sp,
                color = Gris,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(35.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Fondo),
                border = androidx.compose.foundation.BorderStroke(1.dp, Borde),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("CORREO ELECTRÓNICO", fontSize = 11.sp, color = Gris)

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        placeholder = { Text("estudiante@universidad.edu") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(13.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Verde),
                    )

                    Spacer(Modifier.height(20.dp))

                    Text("CONTRASEÑA", fontSize = 11.sp, color = Gris)

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        placeholder = { Text("Ingresa tu contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                                Icon(
                                    imageVector =
                                        if (mostrarContrasena) Icons.Filled.VisibilityOff
                                        else Icons.Filled.Visibility,
                                    contentDescription = "Mostrar u ocultar contraseña",
                                )
                            }
                        },
                        visualTransformation =
                            if (mostrarContrasena) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(13.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Verde),
                    )

                    Spacer(Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            viewModel.limpiarMensaje()
                            mostrarRecuperacion = !mostrarRecuperacion
                        },
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text("¿Olvidaste tu contraseña?", color = Morado, fontSize = 12.sp)
                    }

                    if (mostrarRecuperacion) {
                        Text(
                            "Escribe tu correo arriba para recuperar tu acceso.",
                            fontSize = 12.sp,
                            color = Gris,
                        )

                        TextButton(
                            onClick = { viewModel.recuperarContrasena(correo) },
                            enabled = !viewModel.cargando,
                        ) {
                            Text("Enviar correo de recuperación")
                        }
                    }

                    if (viewModel.mensaje.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))

                        Text(text = viewModel.mensaje, color = Color(0xFFDC2626), fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.iniciarSesion(
                                correo = correo,
                                contrasena = contrasena,
                                onExito = onLoginExitoso,
                            )
                        },
                        enabled = !viewModel.cargando,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    ) {
                        if (viewModel.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Iniciar Sesión", fontWeight = FontWeight.Bold)

                            Spacer(Modifier.width(8.dp))

                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            Text("¿Todavía no tienes una cuenta?", fontSize = 13.sp, color = Gris)

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onIrRegistro,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(13.dp),
            ) {
                Text("Crear Cuenta", color = Morado, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}
