package com.finanquest.sv.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.data.model.Cuenta
import com.finanquest.sv.ui.bienvenida.BienvenidaScreen
import com.finanquest.sv.ui.login.LoginScreen
import com.finanquest.sv.ui.registro.RegistroScreen
import com.finanquest.sv.ui.dashboard.DashboardScreen
import com.finanquest.sv.ui.perfil.PerfilScreen
import com.finanquest.sv.ui.perfil.InformacionPersonalScreen
import com.finanquest.sv.ui.perfil.SeguridadScreen
import com.finanquest.sv.ui.misiones.MisionesScreen
import com.finanquest.sv.ui.misiones.LeccionInversionesScreen
import com.finanquest.sv.ui.finanzas.*
import com.finanquest.sv.viewmodel.AuthViewModel
import com.finanquest.sv.viewmodel.CuentaViewModel

private enum class Pantalla {
    BIENVENIDA, LOGIN, REGISTRO, DASHBOARD, PERFIL,
    INFORMACION_PERSONAL, SEGURIDAD, MISIONES, LECCION_INVERSIONES,
    MOVIMIENTOS, NUEVO_MOVIMIENTO, CUENTAS, CREAR_CUENTA, DETALLE_CUENTA, EDITAR_CUENTA
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    var pantalla by remember { mutableStateOf(if (authViewModel.sesionIniciada) Pantalla.DASHBOARD else Pantalla.BIENVENIDA) }
    var cuentaSeleccionada by remember { mutableStateOf<Cuenta?>(null) }
    val cuentaViewModel: CuentaViewModel = viewModel()

    BackHandler(enabled = pantalla != Pantalla.BIENVENIDA && pantalla != Pantalla.DASHBOARD) {
        authViewModel.limpiarMensaje()
        pantalla = when (pantalla) {
            Pantalla.INFORMACION_PERSONAL, Pantalla.SEGURIDAD -> Pantalla.PERFIL
            Pantalla.LECCION_INVERSIONES -> Pantalla.MISIONES
            Pantalla.NUEVO_MOVIMIENTO -> Pantalla.MOVIMIENTOS
            Pantalla.CREAR_CUENTA -> Pantalla.CUENTAS
            Pantalla.EDITAR_CUENTA -> Pantalla.DETALLE_CUENTA
            Pantalla.DETALLE_CUENTA -> Pantalla.CUENTAS
            Pantalla.PERFIL, Pantalla.MISIONES, Pantalla.MOVIMIENTOS, Pantalla.CUENTAS -> Pantalla.DASHBOARD
            else -> Pantalla.BIENVENIDA
        }
    }

    when (pantalla) {
        Pantalla.BIENVENIDA -> BienvenidaScreen(
            onIniciarSesion = { authViewModel.limpiarMensaje(); pantalla = Pantalla.LOGIN },
            onCrearCuenta = { authViewModel.limpiarMensaje(); pantalla = Pantalla.REGISTRO }
        )
        Pantalla.LOGIN -> LoginScreen(authViewModel,
            onLoginExitoso = { pantalla = Pantalla.DASHBOARD },
            onIrRegistro = { authViewModel.limpiarMensaje(); pantalla = Pantalla.REGISTRO },
            onVolver = { authViewModel.limpiarMensaje(); pantalla = Pantalla.BIENVENIDA })
        Pantalla.REGISTRO -> RegistroScreen(authViewModel,
            onRegistroExitoso = { pantalla = Pantalla.DASHBOARD },
            onIrLogin = { authViewModel.limpiarMensaje(); pantalla = Pantalla.LOGIN },
            onVolver = { authViewModel.limpiarMensaje(); pantalla = Pantalla.BIENVENIDA })
        Pantalla.DASHBOARD -> DashboardScreen(
            authViewModel = authViewModel,
            onMovimientos = { pantalla = Pantalla.MOVIMIENTOS },
            onCuentas = { pantalla = Pantalla.CUENTAS },
            onPerfil = { pantalla = Pantalla.PERFIL },
            onMisiones = { pantalla = Pantalla.MISIONES },
            onNuevoGasto = { pantalla = Pantalla.NUEVO_MOVIMIENTO }
        )
        Pantalla.MOVIMIENTOS -> MovimientosScreen(
            onInicio = { pantalla = Pantalla.DASHBOARD },
            onCuentas = { pantalla = Pantalla.CUENTAS },
            onNuevo = { pantalla = Pantalla.NUEVO_MOVIMIENTO }
        )
        Pantalla.NUEVO_MOVIMIENTO -> NuevoMovimientoScreen(
            onVolver = { pantalla = Pantalla.MOVIMIENTOS },
            onExito = { pantalla = Pantalla.MOVIMIENTOS },
            onInicio = { pantalla = Pantalla.DASHBOARD },
            onCuentas = { pantalla = Pantalla.CUENTAS }
        )
        Pantalla.CUENTAS -> CuentasScreen(
            onInicio = { pantalla = Pantalla.DASHBOARD },
            onMovimientos = { pantalla = Pantalla.MOVIMIENTOS },
            onCrear = { pantalla = Pantalla.CREAR_CUENTA },
            onDetalle = { cuentaSeleccionada = it; pantalla = Pantalla.DETALLE_CUENTA },
            vm = cuentaViewModel
        )
        Pantalla.CREAR_CUENTA -> FormularioCuentaScreen(
            cuenta = null,
            onVolver = { pantalla = Pantalla.CUENTAS },
            onGuardado = { cuentaViewModel.observar(); pantalla = Pantalla.CUENTAS },
            vm = cuentaViewModel
        )
        Pantalla.DETALLE_CUENTA -> {
            val cuenta = cuentaSeleccionada
            if (cuenta != null) DetalleCuentaScreen(
                cuenta = cuenta,
                onVolver = { pantalla = Pantalla.CUENTAS },
                onEditar = { pantalla = Pantalla.EDITAR_CUENTA }
            ) else pantalla = Pantalla.CUENTAS
        }
        Pantalla.EDITAR_CUENTA -> FormularioCuentaScreen(
            cuenta = cuentaSeleccionada,
            onVolver = { pantalla = Pantalla.DETALLE_CUENTA },
            onGuardado = { pantalla = Pantalla.CUENTAS },
            vm = cuentaViewModel
        )
        Pantalla.PERFIL -> PerfilScreen(
            authViewModel = authViewModel,
            onVolver = { pantalla = Pantalla.DASHBOARD },
            onInformacionPersonal = { authViewModel.limpiarMensaje(); pantalla = Pantalla.INFORMACION_PERSONAL },
            onSeguridad = { authViewModel.limpiarMensaje(); pantalla = Pantalla.SEGURIDAD },
            onCerrarSesion = { authViewModel.cerrarSesion(); pantalla = Pantalla.BIENVENIDA }
        )
        Pantalla.INFORMACION_PERSONAL -> InformacionPersonalScreen(
            authViewModel = authViewModel,
            onVolver = { pantalla = Pantalla.PERFIL },
            onGuardarDatos = { nombre, universidad -> authViewModel.actualizarDatosPersonales(nombre, universidad) { pantalla = Pantalla.PERFIL } }
        )
        Pantalla.SEGURIDAD -> SeguridadScreen(onVolver = { pantalla = Pantalla.PERFIL })
        Pantalla.MISIONES -> MisionesScreen(
            onVolver = { pantalla = Pantalla.DASHBOARD },
            onInicio = { pantalla = Pantalla.DASHBOARD },
            onPerfil = { pantalla = Pantalla.PERFIL },
            onAbrirLeccionInversiones = { pantalla = Pantalla.LECCION_INVERSIONES }
        )
        Pantalla.LECCION_INVERSIONES -> LeccionInversionesScreen(
            onVolver = { pantalla = Pantalla.MISIONES },
            onAprobado = { pantalla = Pantalla.MISIONES }
        )
    }
}
