
package com.finanquest.sv.ui.perfil

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanquest.sv.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream

private val VerdePerfil = Color(0xFF16A34A)
private val FondoPerfil: Color @Composable get() = MaterialTheme.colorScheme.background
private val TextoPerfil: Color @Composable get() = MaterialTheme.colorScheme.onSurface
private val MoradoPerfil = Color(0xFF7C3AED)

@Composable
fun InformacionPersonalScreen(
    authViewModel: AuthViewModel,
    onVolver: () -> Unit,
    onGuardarDatos: (String, String) -> Unit
) {
    val context = LocalContext.current
    val usuario = authViewModel.usuario
    val cargando = authViewModel.cargando
    val mensajeFirebase = authViewModel.mensaje

    var nombre by remember(usuario?.nombre) {
        mutableStateOf(usuario?.nombre.orEmpty())
    }

    var universidad by remember(usuario?.universidad) {
        mutableStateOf(usuario?.universidad.orEmpty())
    }

    val correo = usuario?.correo.orEmpty()

    val uid = FirebaseAuth
        .getInstance()
        .currentUser
        ?.uid
        .orEmpty()

    var versionFoto by remember(uid) {
        mutableIntStateOf(0)
    }

    var mensajeLocal by remember {
        mutableStateOf<String?>(null)
    }

    val rutaFoto = remember(uid, versionFoto) {
        obtenerFotoLocal(context, uid)
    }

    val bitmapFoto = remember(rutaFoto, versionFoto) {
        rutaFoto?.let { ruta ->
            try {
                BitmapFactory.decodeFile(ruta)
            } catch (e: Exception) {
                null
            }
        }
    }

    val selectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->

        if (uri != null && uid.isNotBlank()) {

            val guardada = guardarFotoLocal(
                context = context,
                uid = uid,
                uri = uri
            )

            if (guardada) {
                versionFoto++
                mensajeLocal =
                    "Fotografía guardada correctamente en este dispositivo"
            } else {
                mensajeLocal =
                    "No se pudo guardar la fotografía"
            }
        }
    }

    Scaffold(
        containerColor = FondoPerfil,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(60.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }

                    Text(
                        text = "Información Personal",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPerfil
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // FOTO DE PERFIL
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE9F8EF),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(106.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD1FAE5)),
                            contentAlignment = Alignment.Center
                        ) {

                            if (bitmapFoto != null) {

                                Image(
                                    bitmap = bitmapFoto.asImageBitmap(),
                                    contentDescription = "Mi foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                            } else {

                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Avatar",
                                    tint = VerdePerfil,
                                    modifier = Modifier.size(65.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VerdePerfil)
                                .clickable(
                                    enabled = uid.isNotBlank()
                                ) {
                                    selectorFoto.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Cambiar fotografía",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Mi foto de perfil",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPerfil
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "Personaliza tu cuenta con una fotografía",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            selectorFoto.launch("image/*")
                        },
                        enabled = uid.isNotBlank(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = VerdePerfil
                        )
                    ) {

                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(8.dp))

                        Text("Cambiar fotografía")
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // DATOS PERSONALES
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "Mis datos personales",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPerfil
                    )

                    Spacer(Modifier.height(18.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            if (it.length <= 100) {
                                nombre = it
                                mensajeLocal = null
                            }
                        },
                        enabled = !cargando,
                        label = {
                            Text("Nombre completo")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization =
                                KeyboardCapitalization.Words
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(15.dp))

                    OutlinedTextField(
                        value = correo,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Correo electrónico")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Text(
                        text = "El correo está protegido y no se modifica aquí.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 5.dp)
                    )

                    Spacer(Modifier.height(15.dp))

                    OutlinedTextField(
                        value = universidad,
                        onValueChange = {
                            if (it.length <= 150) {
                                universidad = it
                                mensajeLocal = null
                            }
                        },
                        enabled = !cargando,
                        label = {
                            Text("Universidad")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // INFORMACIÓN DE SEGURIDAD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF5F3FF),
                shape = RoundedCornerShape(15.dp)
            ) {

                Row(
                    modifier = Modifier.padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MoradoPerfil
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "Tu fotografía se guarda en este teléfono. " +
                                "El nombre y la universidad se guardan " +
                                "en Firebase.",
                        fontSize = 12.sp,
                        color = Color(0xFF6D28D9)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // GUARDAR CAMBIOS
            Button(
                onClick = {

                    val nombreLimpio = nombre.trim()
                    val universidadLimpia = universidad.trim()

                    when {

                        nombreLimpio.isBlank() -> {
                            mensajeLocal =
                                "Escribe tu nombre completo"
                        }

                        universidadLimpia.isBlank() -> {
                            mensajeLocal =
                                "Escribe tu universidad"
                        }

                        uid.isBlank() -> {
                            mensajeLocal =
                                "Debes iniciar sesión nuevamente"
                        }

                        else -> {
                            mensajeLocal = null
                            authViewModel.limpiarMensaje()

                            onGuardarDatos(
                                nombreLimpio,
                                universidadLimpia
                            )
                        }
                    }
                },
                enabled = !cargando && uid.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePerfil
                ),
                shape = RoundedCornerShape(15.dp)
            ) {

                if (cargando) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(23.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(12.dp))

                    Text("Guardando...")

                } else {

                    Text(
                        text = "Guardar cambios",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // MENSAJES DE LA PANTALLA
            mensajeLocal?.let { texto ->

                Spacer(Modifier.height(12.dp))

                Text(
                    text = texto,
                    color = TextoPerfil,
                    fontSize = 13.sp
                )
            }

            // MENSAJES DE FIREBASE
            if (mensajeFirebase.isNotBlank()) {

                Spacer(Modifier.height(12.dp))

                Text(
                    text = mensajeFirebase,
                    color = Color(0xFFB91C1C),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// GUARDAR FOTO EN EL ALMACENAMIENTO DEL TELÉFONO
private fun guardarFotoLocal(
    context: Context,
    uid: String,
    uri: Uri
): Boolean {

    return try {

        val carpeta = File(
            context.filesDir,
            "fotos_perfil"
        )

        if (!carpeta.exists()) {
            carpeta.mkdirs()
        }

        val archivo = File(
            carpeta,
            "$uid.jpg"
        )

        context.contentResolver
            .openInputStream(uri)
            ?.use { entrada ->

                val bitmap = BitmapFactory.decodeStream(entrada)
                    ?: return false

                FileOutputStream(archivo).use { salida ->

                    bitmap.compress(
                        android.graphics.Bitmap.CompressFormat.JPEG,
                        85,
                        salida
                    )
                }

                bitmap.recycle()

            } ?: return false

        true

    } catch (e: Exception) {

        false
    }
}

// OBTENER FOTO DEL USUARIO ACTUAL
private fun obtenerFotoLocal(
    context: Context,
    uid: String
): String? {

    if (uid.isBlank()) {
        return null
    }

    val archivo = File(
        File(context.filesDir, "fotos_perfil"),
        "$uid.jpg"
    )

    return if (archivo.exists()) {
        archivo.absolutePath
    } else {
        null
    }
}
