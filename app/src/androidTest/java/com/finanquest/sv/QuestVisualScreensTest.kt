package com.finanquest.sv

import androidx.activity.ComponentActivity
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanquest.sv.data.firebase.FirebaseProvider
import com.finanquest.sv.data.model.MetaAhorro
import com.finanquest.sv.ui.bienvenida.BienvenidaScreen
import com.finanquest.sv.ui.dashboard.DashboardScreen
import com.finanquest.sv.ui.finanzas.*
import com.finanquest.sv.ui.login.LoginScreen
import com.finanquest.sv.ui.mascota.MascotaScreen
import com.finanquest.sv.ui.metas.*
import com.finanquest.sv.ui.misiones.MisionesScreen
import com.finanquest.sv.ui.niveles.NivelesScreen
import com.finanquest.sv.ui.recompensas.RecompensasScreen
import com.finanquest.sv.ui.registro.RegistroScreen
import com.finanquest.sv.viewmodel.*
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Datos y cuentas efímeras exclusivamente en los emuladores locales. */
@RunWith(AndroidJUnit4::class)
class QuestVisualScreensTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val db
        get() = FirebaseProvider.firestore

    private val usuario
        get() = db.collection("usuarios").document(FirebaseProvider.auth.currentUser!!.uid)

    @Before
    fun datosLocales() {
        Tasks.await(
            FirebaseProvider.auth.createUserWithEmailAndPassword(
                "visual-${System.nanoTime()}@example.invalid",
                "Prueba12345!",
            ),
            20,
            TimeUnit.SECONDS,
        )
        val batch = db.batch()
        batch.set(
            usuario,
            mapOf(
                "nombre" to "Lucía",
                "correo" to "prueba@example.invalid",
                "xp" to 150,
                "nivel" to 2,
                "mascota" to "Michi",
            ),
        )
        batch.set(
            usuario.collection("cuentas").document("principal00000000000"),
            mapOf(
                "nombre" to "Cuenta principal",
                "tipo" to "Efectivo",
                "saldo" to 600.0,
                "activa" to true,
            ),
        )
        batch.set(
            usuario.collection("metas").document("viaje000000000000000"),
            mapOf(
                "nombre" to "Vacaciones",
                "objetivo" to 1000.0,
                "actual" to 150.0,
                "icono" to "✈️",
                "completada" to false,
                "creadaEn" to System.currentTimeMillis(),
            ),
        )
        batch.set(
            usuario.collection("metas").document("casa0000000000000000"),
            mapOf(
                "nombre" to "Mi vivienda",
                "objetivo" to 500.0,
                "actual" to 500.0,
                "icono" to "🏠",
                "completada" to true,
                "creadaEn" to System.currentTimeMillis(),
            ),
        )
        batch.set(
            usuario.collection("movimientos").document("comida"),
            mapOf(
                "cuentaId" to "principal00000000000",
                "tipo" to "Gasto",
                "monto" to 25.0,
                "categoria" to "Comida",
                "fecha" to System.currentTimeMillis(),
                "nota" to "Almuerzo",
            ),
        )
        batch.set(
            usuario.collection("recompensas_desbloqueadas").document("sombrero"),
            mapOf("desbloqueada" to true),
        )
        Tasks.await(batch.commit(), 20, TimeUnit.SECONDS)
    }

    @After
    fun cerrar() {
        FirebaseProvider.auth.currentUser?.let { Tasks.await(it.delete(), 20, TimeUnit.SECONDS) }
        FirebaseProvider.auth.signOut()
    }

    private fun captura(nombre: String) {
        if (
            androidx.test.platform.app.InstrumentationRegistry.getArguments()
                .getString("capturas") != "true"
        )
            return
        compose.waitForIdle()
        val resolver = compose.activity.contentResolver
        val valores =
            android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "quest-$nombre.png")
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FinanQuest")
            }
        val uri =
            resolver.insert(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                valores,
            )!!
        resolver.openOutputStream(uri)!!.use {
            compose
                .onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test
    fun inicioMantieneResumenYAccesos() {
        val nuevaMeta = AtomicBoolean(false)
        compose.setContent {
            MaterialTheme {
                DashboardScreen(AuthViewModel(), onNuevaMeta = { nuevaMeta.set(true) })
            }
        }
        compose.onNodeWithText("SALDO DISPONIBLE").assertIsDisplayed()
        compose.onNodeWithText("INGRESOS").assertIsDisplayed()
        compose.onNodeWithText("GASTOS").assertIsDisplayed()
        captura("inicio")
        compose.onNodeWithText("NUEVA META").performScrollTo().performClick()
        assertTrue(nuevaMeta.get())
    }

    @Test
    fun metasFiltranYAbrenDetalle() {
        val vm = MetaViewModel()
        val detalle = AtomicBoolean(false)
        compose.setContent {
            MaterialTheme { MetasScreen(vm = vm, onDetalle = { detalle.set(true) }) }
        }
        compose.waitUntil(20_000) { vm.metas.size == 2 }
        captura("metas")
        compose.onNodeWithText("Activas").performClick()
        compose.onNodeWithTag("lista-metas").performScrollToNode(hasText("Vacaciones"))
        compose.onNodeWithText("Vacaciones").assertIsDisplayed().performClick()
        assertTrue(detalle.get())
        compose.onNodeWithTag("lista-metas").performScrollToNode(hasText("Completadas"))
        compose.onNodeWithText("Completadas").performClick()
        compose.onNodeWithTag("lista-metas").performScrollToNode(hasText("Mi vivienda"))
        compose.onNodeWithText("Mi vivienda").assertIsDisplayed()
        compose.onNodeWithText("Vacaciones").assertDoesNotExist()
    }

    @Test
    fun crearMetaConImagenConservaGuardado() {
        val vm = MetaViewModel()
        val guardada = AtomicBoolean(false)
        compose.setContent {
            MaterialTheme {
                CrearMetaScreen(
                    vm = vm,
                    onCrear = { n, o, i, f, k -> vm.crear(n, o, i, f, k) { guardada.set(true) } },
                )
            }
        }
        captura("crear-meta")
        compose.onNodeWithText("Nombre de la meta").performScrollTo().performTextInput("Mi carro")
        compose.onNodeWithText("Monto objetivo").performScrollTo().performTextInput("2500")
        closeSoftKeyboard()
        compose.onNodeWithText("Elige una imagen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Carro").performScrollTo().performClick()
        compose.onNodeWithText("Guardar meta").performScrollTo().performClick()
        compose.waitUntil(20_000) { guardada.get() }
        val datos = Tasks.await(usuario.collection("metas").get())
        assertTrue(
            datos.documents.any {
                it.getString("nombre") == "Mi carro" && it.getString("icono") == "🚘"
            }
        )
    }

    @Test
    fun aporteDesdeDetalleConservaSaldoYRegistro() {
        val vm = MetaViewModel()
        compose.setContent {
            MaterialTheme {
                DetalleMetaScreen(
                    MetaAhorro(
                        id = "viaje000000000000000",
                        nombre = "Vacaciones",
                        objetivo = 1000.0,
                        actual = 150.0,
                        icono = "✈️",
                    ),
                    vm = vm,
                )
            }
        }
        compose.waitUntil(20_000) { vm.metas.size == 2 }
        captura("detalle-meta")
        compose.onNodeWithText("Agregar aporte").performScrollTo().performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Cuenta principal").fetchSemanticsNodes().size == 1
        }
        compose.onNodeWithText("Cuenta principal").performClick()
        compose.onNodeWithText("Monto del aporte").performTextInput("25")
        closeSoftKeyboard()
        compose.onNodeWithText("Guardar aporte").assertIsEnabled().performClick()
        compose.waitUntil(25_000) { !vm.cargando && vm.mensaje != null }
        assertEquals("Aporte guardado correctamente", vm.mensaje)
        compose.waitUntil(10_000) {
            vm.metas.any { it.id == "viaje000000000000000" && it.actual == 175.0 }
        }
        assertEquals(
            575.0,
            Tasks.await(usuario.collection("cuentas").document("principal00000000000").get())
                .getDouble("saldo")!!,
            0.0,
        )
    }

    @Test
    fun cuentasYMovimientosConservanDetalle() {
        val mostrarMovimientos = mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                if (mostrarMovimientos.value)
                    MovimientosScreen(onInicio = {}, onCuentas = {}, onNuevo = {})
                else
                    CuentasScreen(
                        onInicio = {},
                        onMovimientos = {},
                        onCrear = {},
                        onDetalle = { mostrarMovimientos.value = true },
                    )
            }
        }
        compose.waitUntil(20_000) {
            compose.onAllNodesWithText("Cuenta principal").fetchSemanticsNodes().size == 1
        }
        captura("cuentas")
        compose.onNodeWithText("Cuenta principal").performClick()
        compose.waitUntil(20_000) {
            compose.onAllNodesWithText("Comida").fetchSemanticsNodes().size == 1
        }
        compose.onNodeWithText("Comida").assertIsDisplayed()
        captura("movimientos")
    }

    @Test
    fun misionesConservanBusquedaYNavegacion() {
        val volver = AtomicBoolean(false)
        compose.setContent { MaterialTheme { MisionesScreen(onVolver = { volver.set(true) }) } }
        compose.onNodeWithText("Buscar misiones...").assertIsDisplayed()
        captura("misiones")
        compose.onNodeWithContentDescription("Volver").performClick()
        assertTrue(volver.get())
    }

    @Test
    fun recompensasConservanFiltrosYDetalle() {
        val abrir = AtomicBoolean(false)
        compose.setContent {
            MaterialTheme {
                RecompensasScreen(
                    onDetalle = { abrir.set(true) },
                    recompensasDesbloqueadas = setOf("sombrero"),
                )
            }
        }
        compose.onNodeWithText("Accesorios").performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Sombrero financiero"))
        captura("recompensas")
        compose.onNodeWithText("Sombrero financiero").performClick()
        assertTrue(abrir.get())
    }

    @Test
    fun nivelesMuestranProgreso() {
        compose.setContent { MaterialTheme { NivelesScreen(AuthViewModel(), onVolver = {}) } }
        compose.waitUntil(20_000) {
            compose.onAllNodesWithText("Nivel 2").fetchSemanticsNodes().isNotEmpty()
        }
        captura("niveles")
        compose.onNodeWithText("Tu camino de niveles").assertIsDisplayed()
    }

    @Test
    fun mascotaConservaArmario() {
        compose.setContent { MaterialTheme { MascotaScreen(onVolver = {}) } }
        compose.onNodeWithText("Progreso").assertIsDisplayed()
        captura("mascota")
        compose.onNodeWithText("Armario").performClick()
        captura("armario")
    }

    @Test
    fun bienvenidaRegistroYLoginSeMuestran() {
        val pagina = mutableStateOf(0)
        compose.setContent {
            MaterialTheme {
                when (pagina.value) {
                    0 ->
                        BienvenidaScreen(
                            onIniciarSesion = { pagina.value = 1 },
                            onCrearCuenta = { pagina.value = 2 },
                        )
                    1 ->
                        LoginScreen(
                            AuthViewModel(),
                            onLoginExitoso = {},
                            onIrRegistro = { pagina.value = 2 },
                            onVolver = { pagina.value = 0 },
                        )
                    else ->
                        RegistroScreen(
                            AuthViewModel(),
                            onRegistroExitoso = {},
                            onIrLogin = { pagina.value = 1 },
                        )
                }
            }
        }
        captura("bienvenida")
        compose.onNodeWithText("Crear Cuenta").performScrollTo().performClick()
        compose.onNodeWithText("¡Crea tu perfil!").assertIsDisplayed()
        captura("registro")
        compose.runOnIdle { pagina.value = 1 }
        compose.onNodeWithText("¡Bienvenido de nuevo!").assertIsDisplayed()
        captura("login")
    }

    @Test
    fun metasConTemaOscuro() {
        val vm = MetaViewModel()
        compose.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) { MetasScreen(vm = vm) }
        }
        compose.waitUntil(20_000) { vm.metas.size == 2 }
        compose.onNodeWithText("Mis metas").assertIsDisplayed()
        captura("metas-oscuro")
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun emuladores() {
            FirebaseProvider.auth.useEmulator("127.0.0.1", 9099)
            FirebaseProvider.firestore.useEmulator("127.0.0.1", 8080)
        }
    }
}
