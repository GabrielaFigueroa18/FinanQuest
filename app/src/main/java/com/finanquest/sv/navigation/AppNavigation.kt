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
import com.finanquest.sv.ui.metas.MetasScreen
import com.finanquest.sv.ui.metas.CrearMetaScreen
import com.finanquest.sv.ui.metas.DetalleMetaScreen
import com.finanquest.sv.ui.presupuestos.PresupuestosScreen
import com.finanquest.sv.ui.presupuestos.CrearPresupuestoScreen
import com.finanquest.sv.ui.presupuestos.CategoriasAlertasScreen
import com.finanquest.sv.ui.reportes.ReportesScreen

private enum class Pantalla {
    BIENVENIDA, LOGIN, REGISTRO, DASHBOARD, PERFIL,
    INFORMACION_PERSONAL, SEGURIDAD, MISIONES, LECCION_INVERSIONES, METAS, CREAR_META, DETALLE_META, PRESUPUESTOS, CREAR_PRESUPUESTO, CATEGORIAS_ALERTAS, REPORTES
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    var pantalla by remember {
        mutableStateOf(
            if (authViewModel.sesionIniciada) Pantalla.DASHBOARD
            else Pantalla.BIENVENIDA
        )
    }

    BackHandler(
        enabled = pantalla == Pantalla.LOGIN ||
                pantalla == Pantalla.REGISTRO ||
                pantalla == Pantalla.PERFIL ||
                pantalla == Pantalla.INFORMACION_PERSONAL ||
                pantalla == Pantalla.SEGURIDAD ||
                pantalla == Pantalla.MISIONES ||
                pantalla == Pantalla.LECCION_INVERSIONES ||
                pantalla == Pantalla.METAS || pantalla == Pantalla.CREAR_META ||
                pantalla == Pantalla.DETALLE_META || pantalla == Pantalla.PRESUPUESTOS ||
                pantalla == Pantalla.CREAR_PRESUPUESTO || pantalla == Pantalla.CATEGORIAS_ALERTAS || pantalla == Pantalla.REPORTES
    ) {
        authViewModel.limpiarMensaje()
        pantalla = when (pantalla) {
            Pantalla.INFORMACION_PERSONAL, Pantalla.SEGURIDAD -> Pantalla.PERFIL
            Pantalla.LECCION_INVERSIONES -> Pantalla.MISIONES
            Pantalla.CREAR_META, Pantalla.DETALLE_META -> Pantalla.METAS
            Pantalla.CREAR_PRESUPUESTO, Pantalla.CATEGORIAS_ALERTAS -> Pantalla.PRESUPUESTOS
            Pantalla.METAS, Pantalla.PRESUPUESTOS, Pantalla.REPORTES -> Pantalla.DASHBOARD
            Pantalla.PERFIL, Pantalla.MISIONES -> Pantalla.DASHBOARD
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
            onLoginExitoso = { pantalla = Pantalla.DASHBOARD },
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
            onRegistroExitoso = { pantalla = Pantalla.DASHBOARD },
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
            onPerfil = { pantalla = Pantalla.PERFIL },
            onMisiones = { pantalla = Pantalla.MISIONES },
            onMetas = { pantalla = Pantalla.METAS },
            onPresupuestos = { pantalla = Pantalla.PRESUPUESTOS },
            onReportes = { pantalla = Pantalla.REPORTES }
        )
        Pantalla.PERFIL -> PerfilScreen(
            authViewModel = authViewModel,
            onVolver = { pantalla = Pantalla.DASHBOARD },
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
            onVolver = { pantalla = Pantalla.PERFIL },
            onGuardarDatos = { nombre, universidad ->
                authViewModel.actualizarDatosPersonales(
                    nombre = nombre,
                    universidad = universidad,
                    onExito = { pantalla = Pantalla.PERFIL }
                )
            }
        )
        Pantalla.SEGURIDAD -> SeguridadScreen(
            onVolver = { pantalla = Pantalla.PERFIL }
        )
        Pantalla.MISIONES -> MisionesScreen(
            onVolver = { pantalla = Pantalla.DASHBOARD },
            onInicio = { pantalla = Pantalla.DASHBOARD },
            onPerfil = { pantalla = Pantalla.PERFIL },
            onAbrirLeccionInversiones = { pantalla = Pantalla.LECCION_INVERSIONES }
        )
        Pantalla.METAS -> MetasScreen(
            onVolver = { pantalla = Pantalla.DASHBOARD },
            onInicio = { pantalla = Pantalla.DASHBOARD },
            onNuevaMeta = { pantalla = Pantalla.CREAR_META },
            onDetalle = { pantalla = Pantalla.DETALLE_META },
            onPerfil = { pantalla = Pantalla.PERFIL }
        )
        Pantalla.CREAR_META -> CrearMetaScreen(onVolver={pantalla=Pantalla.METAS},onCrear={_,_,_->pantalla=Pantalla.METAS})
        Pantalla.DETALLE_META -> DetalleMetaScreen(onVolver={pantalla=Pantalla.METAS})
        Pantalla.PRESUPUESTOS -> PresupuestosScreen(onVolver={pantalla=Pantalla.DASHBOARD},onCrear={pantalla=Pantalla.CREAR_PRESUPUESTO},onCategorias={pantalla=Pantalla.CATEGORIAS_ALERTAS},onDetalle={pantalla=Pantalla.CATEGORIAS_ALERTAS},onReportes={pantalla=Pantalla.REPORTES})
        Pantalla.CREAR_PRESUPUESTO -> CrearPresupuestoScreen(onVolver={pantalla=Pantalla.PRESUPUESTOS},onGuardar={_,_->pantalla=Pantalla.PRESUPUESTOS})
        Pantalla.CATEGORIAS_ALERTAS -> CategoriasAlertasScreen(onVolver={pantalla=Pantalla.PRESUPUESTOS})
        Pantalla.REPORTES -> ReportesScreen(onVolver={pantalla=Pantalla.DASHBOARD},onInicio={pantalla=Pantalla.DASHBOARD})
        Pantalla.LECCION_INVERSIONES -> LeccionInversionesScreen(
            onVolver = { pantalla = Pantalla.MISIONES },
            onAprobado = { pantalla = Pantalla.MISIONES }
        )
    }
}





