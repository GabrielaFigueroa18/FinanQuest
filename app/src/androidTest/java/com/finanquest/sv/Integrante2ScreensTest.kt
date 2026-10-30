package com.finanquest.sv

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanquest.sv.data.model.MetaAhorro
import com.finanquest.sv.ui.dashboard.DashboardScreen
import com.finanquest.sv.ui.metas.CrearMetaScreen
import com.finanquest.sv.ui.metas.MetasScreen
import com.finanquest.sv.ui.presupuestos.CategoriasAlertasScreen
import com.finanquest.sv.ui.presupuestos.PresupuestosScreen
import com.finanquest.sv.ui.reportes.ReportesScreen
import com.finanquest.sv.viewmodel.AuthViewModel
import com.finanquest.sv.viewmodel.MetaViewModel
import com.finanquest.sv.viewmodel.MovimientoViewModel
import com.finanquest.sv.viewmodel.PresupuestoViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Integrante2ScreensTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dashboardMuestraResumenFinanciero() {
        compose.setContent { MaterialTheme { DashboardScreen(authViewModel = AuthViewModel()) } }
        compose.onNodeWithText("SALDO DISPONIBLE").assertIsDisplayed()
        compose.onNodeWithText("INGRESOS").assertIsDisplayed()
        compose.onNodeWithText("GASTOS").assertIsDisplayed()
    }

    @Test
    fun metasMuestraEstadoVacioYAccionParaCrear() {
        compose.setContent { MaterialTheme { MetasScreen(vm = MetaViewModel()) } }
        compose.onNodeWithText("Mis metas").assertIsDisplayed()
        compose.onNodeWithText("Ahorra paso a paso para lo que te importa.").assertIsDisplayed()
    }

    @Test
    fun crearMetaExponeElSelectorDeCalendario() {
        compose.setContent { MaterialTheme { CrearMetaScreen(vm = MetaViewModel()) } }
        compose.onNodeWithText("Elegir fecha límite (opcional)").assertIsDisplayed().performClick()
    }

    @Test
    fun detalleMetaMuestraObjetivoYAccionDeAporte() {
        compose.setContent {
            MaterialTheme {
                com.finanquest.sv.ui.metas.DetalleMetaScreen(
                    meta =
                        MetaAhorro(
                            id = "meta-test",
                            nombre = "Fondo de estudio",
                            objetivo = 500.0,
                            actual = 100.0,
                        ),
                    vm = MetaViewModel(),
                )
            }
        }
        compose.onNodeWithText("Fondo de estudio").assertIsDisplayed()
        compose.onNodeWithText("Agregar aporte").assertIsDisplayed()
    }

    @Test
    fun presupuestosMuestraEstadoVacioYAccionDeCreacion() {
        compose.setContent { MaterialTheme { PresupuestosScreen(vm = PresupuestoViewModel()) } }
        compose.onNodeWithText("Aún no tienes presupuestos").assertIsDisplayed()
        compose.onAllNodesWithText("Crear presupuesto")[0].assertIsDisplayed()
    }

    @Test
    fun categoriasMuestraConfiguracionDeAlertas() {
        compose.setContent {
            MaterialTheme { CategoriasAlertasScreen(vm = PresupuestoViewModel()) }
        }
        compose.onNodeWithText("Categorías y alertas").assertIsDisplayed()
        compose.onNodeWithText("Configura categorías y umbrales de aviso.").assertIsDisplayed()
    }

    @Test
    fun reportesMuestraEstadoVacioSinMovimientos() {
        compose.setContent { MaterialTheme { ReportesScreen(vm = MovimientoViewModel()) } }
        compose.onNodeWithText("Reportes").assertIsDisplayed()
        compose.onNodeWithText("Distribución de Gastos").assertIsDisplayed()
    }
}
