package com.finanquest.sv.ui.registro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.R
import com.finanquest.sv.ui.components.*
import com.finanquest.sv.viewmodel.AuthViewModel

private val Fondo: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val Verde: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val Texto: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val Gris: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val Borde: Color
    @Composable get() = MaterialTheme.colorScheme.outline

@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onRegistroExitoso: () -> Unit,
    onIrLogin: () -> Unit,
    onVolver: () -> Unit = {},
) {
    QuestVisualTheme {
        var nombre by remember { mutableStateOf("") }
        var correo by remember { mutableStateOf("") }
        var contrasena by remember { mutableStateOf("") }
        var confirmar by remember { mutableStateOf("") }
        var aceptaTerminos by remember { mutableStateOf(false) }
        var verContrasena by remember { mutableStateOf(false) }
        var verConfirmacion by remember { mutableStateOf(false) }
        var errorLocal by remember { mutableStateOf("") }

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

            // ENCABEZADO
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Texto,
                    )
                }

                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "FinanQuest SV",
                    tint = Verde,
                    modifier = Modifier.size(27.dp),
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // IMAGEN SUPERIOR DEL REGISTRO
            Image(
                painter = painterResource(id = R.drawable.financat_hero),
                contentDescription = "Ilustración de registro FinanQuest SV",
                modifier =
                    Modifier.fillMaxWidth(0.72f).height(155.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "¡Crea tu perfil!",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Texto,
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Empieza a organizar tu dinero y cumplir tus metas.",
                fontSize = 12.sp,
                color = Gris,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
            )

            Spacer(modifier = Modifier.height(22.dp))

            // TARJETA DEL FORMULARIO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Fondo),
                border = androidx.compose.foundation.BorderStroke(1.dp, Borde),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CampoRegistro(
                        titulo = "NOMBRE COMPLETO",
                        valor = nombre,
                        alCambiar = { nombre = it },
                        ejemplo = "Juan Pérez",
                        icono = {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Gris)
                        },
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CampoRegistro(
                        titulo = "CORREO UNIVERSITARIO",
                        valor = correo,
                        alCambiar = { correo = it },
                        ejemplo = "estudiante@universidad.edu",
                        tipoTeclado = KeyboardType.Email,
                        icono = { Icon(Icons.Filled.Email, contentDescription = null, tint = Gris) },
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CampoRegistro(
                        titulo = "CONTRASEÑA",
                        valor = contrasena,
                        alCambiar = { contrasena = it },
                        ejemplo = "Mínimo 6 caracteres",
                        esContrasena = true,
                        mostrarContrasena = verContrasena,
                        alCambiarVisibilidad = { verContrasena = !verContrasena },
                        icono = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Gris) },
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CampoRegistro(
                        titulo = "CONFIRMAR CONTRASEÑA",
                        valor = confirmar,
                        alCambiar = { confirmar = it },
                        ejemplo = "Repite tu contraseña",
                        esContrasena = true,
                        mostrarContrasena = verConfirmacion,
                        alCambiarVisibilidad = { verConfirmacion = !verConfirmacion },
                        icono = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Gris) },
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ACEPTACIÓN DE TÉRMINOS
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = aceptaTerminos,
                            onCheckedChange = { aceptaTerminos = it },
                            colors = CheckboxDefaults.colors(checkedColor = Verde),
                        )

                        Text(
                            text =
                                buildAnnotatedString {
                                    append("Acepto los ")
                                    withStyle(
                                        SpanStyle(color = Verde, fontWeight = FontWeight.SemiBold)
                                    ) {
                                        append("Términos de Servicio")
                                    }
                                    append(" y la ")
                                    withStyle(
                                        SpanStyle(color = Verde, fontWeight = FontWeight.SemiBold)
                                    ) {
                                        append("Política de Privacidad")
                                    }
                                    append(".")
                                },
                            fontSize = 11.sp,
                            color = Gris,
                            lineHeight = 17.sp,
                        )
                    }

                    if (errorLocal.isNotEmpty()) {
                        Text(text = errorLocal, color = Color(0xFFDC2626), fontSize = 12.sp)
                    }

                    if (viewModel.mensaje.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = viewModel.mensaje, color = Color(0xFFDC2626), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            errorLocal = ""

                            if (!aceptaTerminos) {
                                errorLocal =
                                    "Debes aceptar los términos y la política de privacidad"
                            } else {
                                viewModel.registrar(
                                    nombre = nombre,
                                    correo = correo,
                                    contrasena = contrasena,
                                    confirmarContrasena = confirmar,
                                    onExito = onRegistroExitoso,
                                )
                            }
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
                            Text(text = "Completar Registro", fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(text = "¿Ya tienes una cuenta?", fontSize = 12.sp, color = Gris)

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onIrLogin,
                modifier = Modifier.fillMaxWidth().height(49.dp),
                shape = RoundedCornerShape(13.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Borde),
            ) {
                Text(text = "Iniciar Sesión", color = Texto, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun CampoRegistro(
    titulo: String,
    valor: String,
    alCambiar: (String) -> Unit,
    ejemplo: String,
    icono: @Composable () -> Unit,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    esContrasena: Boolean = false,
    mostrarContrasena: Boolean = false,
    alCambiarVisibilidad: () -> Unit = {},
) {
    Column {
        Text(text = titulo, fontSize = 11.sp, color = Gris, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(7.dp))

        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            placeholder = { Text(text = ejemplo, fontSize = 12.sp, color = Gris) },
            leadingIcon = icono,
            trailingIcon = {
                if (esContrasena) {
                    IconButton(onClick = alCambiarVisibilidad) {
                        Icon(
                            imageVector =
                                if (mostrarContrasena) Icons.Filled.VisibilityOff
                                else Icons.Filled.Visibility,
                            contentDescription = "Mostrar u ocultar contraseña",
                            tint = Gris,
                        )
                    }
                }
            },
            visualTransformation =
                if (esContrasena && !mostrarContrasena) PasswordVisualTransformation()
                else VisualTransformation.None,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = if (esContrasena) KeyboardType.Password else tipoTeclado
                ),
            singleLine = true,
            shape = RoundedCornerShape(13.dp),
            modifier = Modifier.fillMaxWidth(),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Verde,
                    unfocusedBorderColor = Borde,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
        )
    }
}
