package com.finanquest.sv.navigation
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.ui.bienvenida.BienvenidaScreen
import com.finanquest.sv.ui.login.LoginScreen
import com.finanquest.sv.ui.registro.RegistroScreen
import com.finanquest.sv.ui.dashboard.DashboardScreen
import com.finanquest.sv.ui.perfil.PerfilScreen
import com.finanquest.sv.ui.perfil.InformacionPersonalScreen
import com.finanquest.sv.ui.perfil.SeguridadScreen
import com.finanquest.sv.ui.misiones.MisionesScreen
import com.finanquest.sv.ui.misiones.LeccionInversionesScreen
import com.finanquest.sv.viewmodel.AuthViewModel

private enum class Pantalla {
    BIENVENIDA,
    LOGIN,
    REGISTRO,
    DASHBOARD,
    PERFIL,
    INFORMACION_PERSONAL,
    SEGURIDAD,
    MISIONES,
    LECCION_INVERSIONES
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    var pantalla by remember {
        mutableStateOf(
            if (authViewModel.sesionIniciada) {
                Pantalla.DASHBOARD
            } else {
                Pantalla.BIENVENIDA
            }
        )
    }

    BackHandler(
        enabled = pantalla == Pantalla.LOGIN ||
                pantalla == Pantalla.REGISTRO ||
                pantalla == Pantalla.PERFIL ||
                pantalla == Pantalla.INFORMACION_PERSONAL ||
                pantalla == Pantalla.SEGURIDAD ||
                pantalla == Pantalla.MISIONES ||
                pantalla == Pantalla.LECCION_INVERSIONES
    ) {
        authViewModel.limpiarMensaje()

        pantalla = when (pantalla) {
            Pantalla.INFORMACION_PERSONAL,
            Pantalla.SEGURIDAD -> Pantalla.PERFIL

            Pantalla.LECCION_INVERSIONES -> Pantalla.MISIONES

            Pantalla.PERFIL,
            Pantalla.MISIONES -> Pantalla.DASHBOARD

            else -> Pantalla.BIENVENIDA
        }
    }

    when (pantalla) {

        Pantalla.BIENVENIDA -> BienvenidaScreen(
            onIniciarSesion = {
                authViewModel.limpiarMensaje()
                pantalla = Pantalla.LOGIN
            },
            onCrearCuenta = {
                authViewModel.limpiarMensaje()
                pantalla = Pantalla.REGISTRO
            }
        )

        Pantalla.LOGIN -> LoginScreen(
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

        Pantalla.REGISTRO -> RegistroScreen(
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

        Pantalla.DASHBOARD -> DashboardScreen(
            authViewModel = authViewModel,
            onPerfil = {
                pantalla = Pantalla.PERFIL
            },
            onMisiones = {
                pantalla = Pantalla.MISIONES
            }
        )

        Pantalla.PERFIL -> PerfilScreen(
            authViewModel = authViewModel,
            onVolver = {
                pantalla = Pantalla.DASHBOARD
            },
            onInformacionPersonal = {
                authViewModel.limpiarMensaje()
                pantalla = Pantalla.INFORMACION_PERSONAL
            },
            onSeguridad = {
                authViewModel.limpiarMensaje()
                pantalla = Pantalla.SEGURIDAD
            },
            onCerrarSesion = {
                authViewModel.cerrarSesion()
                pantalla = Pantalla.BIENVENIDA
            }
        )

        Pantalla.INFORMACION_PERSONAL -> InformacionPersonalScreen(
            authViewModel = authViewModel,
            onVolver = {
                pantalla = Pantalla.PERFIL
            },
            onGuardarDatos = { nombre, universidad ->
                authViewModel.actualizarDatosPersonales(
                    nombre = nombre,
                    universidad = universidad,
                    onExito = {
                        pantalla = Pantalla.PERFIL
                    }
                )
            }
        )

        Pantalla.SEGURIDAD -> SeguridadScreen(
            onVolver = {
                pantalla = Pantalla.PERFIL
            }
        )

        Pantalla.MISIONES -> MisionesScreen(
            onVolver = {
                pantalla = Pantalla.DASHBOARD
            },
            onInicio = {
                pantalla = Pantalla.DASHBOARD
            },
            onPerfil = {
                pantalla = Pantalla.PERFIL
            },
            onAbrirLeccionInversiones = {
                pantalla = Pantalla.LECCION_INVERSIONES
            }
        )

        Pantalla.LECCION_INVERSIONES -> LeccionInversionesScreen(
            onVolver = {
                pantalla = Pantalla.MISIONES
            },
            onAprobado = {
                pantalla = Pantalla.MISIONES
            }
        )
    }
}
