
package com.finanquest.sv.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.finanquest.sv.ui.bienvenida.BienvenidaScreen
import com.finanquest.sv.ui.login.LoginScreen
import com.finanquest.sv.ui.registro.RegistroScreen
import com.finanquest.sv.ui.dashboard.DashboardScreen
import com.finanquest.sv.viewmodel.AuthViewModel

private enum class Pantalla {
    BIENVENIDA,
    LOGIN,
    REGISTRO,
    DASHBOARD,
    PERFIL
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    var pantalla by remember {
        mutableStateOf(
            if (authViewModel.sesionIniciada)
                Pantalla.DASHBOARD
            else
                Pantalla.BIENVENIDA
        )
    }

    BackHandler(
        enabled = pantalla == Pantalla.LOGIN ||
                pantalla == Pantalla.REGISTRO ||
                pantalla == Pantalla.PERFIL
    ) {
        authViewModel.limpiarMensaje()

        pantalla = when (pantalla) {
            Pantalla.PERFIL -> Pantalla.DASHBOARD
            else -> Pantalla.BIENVENIDA
        }
    }

    when (pantalla) {

        Pantalla.BIENVENIDA -> {
            BienvenidaScreen(
                onIniciarSesion = {
                    authViewModel.limpiarMensaje()
                    pantalla = Pantalla.LOGIN
                },
                onCrearCuenta = {
                    authViewModel.limpiarMensaje()
                    pantalla = Pantalla.REGISTRO
                }
            )
        }

        Pantalla.LOGIN -> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginExitoso = {
                    pantalla = Pantalla.DASHBOARD
                },
                onIrRegistro = {
                    authViewModel.limpiarMensaje()
                    pantalla = Pantalla.REGISTRO
                },
                onVolver = {
                    authViewModel.limpiarMensaje()
                    pantalla = Pantalla.BIENVENIDA
                }
            )
        }

        Pantalla.REGISTRO -> {
            RegistroScreen(
                viewModel = authViewModel,
                onRegistroExitoso = {
                    pantalla = Pantalla.DASHBOARD
                },
                onIrLogin = {
                    authViewModel.limpiarMensaje()
                    pantalla = Pantalla.LOGIN
                },
                onVolver = {
                    authViewModel.limpiarMensaje()
                    pantalla = Pantalla.BIENVENIDA
                }
            )
        }

        Pantalla.DASHBOARD -> {
            DashboardScreen(
                authViewModel = authViewModel,
                onPerfil = {
                    pantalla = Pantalla.PERFIL
                }
            )
        }

        Pantalla.PERFIL -> {
            PerfilTemporal(
                nombre = authViewModel.usuario?.nombre
                    ?: "Estudiante",
                onVolver = {
                    pantalla = Pantalla.DASHBOARD
                },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    pantalla = Pantalla.BIENVENIDA
                }
            )
        }
    }
}

@Composable
private fun PerfilTemporal(
    nombre: String,
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Mi Perfil",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        Text("Usuario: $nombre")

        Spacer(Modifier.height(24.dp))

        Button(onClick = onVolver) {
            Text("Volver al Dashboard")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(onClick = onCerrarSesion) {
            Text("Cerrar sesión")
        }
    }
}
