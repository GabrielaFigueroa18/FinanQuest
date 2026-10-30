package com.finanquest.sv

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanquest.sv.data.firebase.FirebaseProvider
import com.finanquest.sv.data.model.Categoria
import com.finanquest.sv.data.repository.PresupuestoRepository
import com.finanquest.sv.ui.presupuestos.*
import com.finanquest.sv.viewmodel.PresupuestoViewModel
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Solo usa los emuladores locales de Auth y Firestore; nunca crea datos en producción. */
@RunWith(AndroidJUnit4::class)
class PresupuestosIntegrationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val repo = PresupuestoRepository()

    private fun resultado(accion: ((String?) -> Unit) -> Unit): String? {
        val latch = CountDownLatch(1)
        var error: String? = null
        accion {
            error = it
            latch.countDown()
        }
        assertTrue("La operación no respondió", latch.await(25, TimeUnit.SECONDS))
        return error
    }

    private fun captura(nombre: String) {
        if (
            androidx.test.platform.app.InstrumentationRegistry.getArguments()
                .getString("capturas") != "true"
        )
            return
        compose.waitForIdle()
        val archivo = java.io.File(compose.activity.getExternalFilesDir(null), "$nombre.png")
        archivo.outputStream().use {
            compose
                .onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Before
    fun usuario() {
        Tasks.await(
            FirebaseProvider.auth.createUserWithEmailAndPassword(
                "presupuesto-${System.nanoTime()}@example.invalid",
                "Prueba12345!",
            ),
            20,
            TimeUnit.SECONDS,
        )
    }

    @After
    fun salir() {
        FirebaseProvider.auth.currentUser?.let { Tasks.await(it.delete(), 20, TimeUnit.SECONDS) }
        FirebaseProvider.auth.signOut()
    }

    @Test
    fun persistenciaDuplicadosEdicionYEliminacionSegura() {
        assertNull(resultado { repo.guardarCategoria("Alimentación", 150.0, .8, it) })
        assertNotNull(resultado { repo.guardarCategoria(" ALIMENTACION ", 200.0, .8, it) })
        assertNull(resultado { repo.guardarPresupuesto("alimentacion", 100.0, "Semanal", it) })
        val categoria = Categoria(id = "alimentacion", nombre = "Alimentación", limite = 150.0)
        assertNotNull(resultado { repo.eliminarCategoria(categoria, it) })
        assertNull(resultado { repo.editarCategoria(categoria, 200.0, .9, it) })
        assertNull(resultado { repo.actualizarAlertasCategoria(categoria, false, .9, it) })
        assertNull(resultado { repo.guardarPresupuesto("alimentacion", 250.0, "Mensual", it) })
        val usuario =
            FirebaseProvider.firestore
                .collection("usuarios")
                .document(FirebaseProvider.auth.currentUser!!.uid)
        val presupuesto =
            Tasks.await(
                usuario.collection("presupuestos").document("alimentacion").get(),
                20,
                TimeUnit.SECONDS,
            )
        assertEquals(250.0, presupuesto.getDouble("limite")!!, 0.0)
        assertEquals("Mensual", presupuesto.getString("periodo"))
        assertNull(resultado { repo.eliminarPresupuesto("alimentacion", it) })
        assertNull(resultado { repo.eliminarCategoria(categoria, it) })
        assertFalse(
            Tasks.await(usuario.collection("categorias").document("alimentacion").get()).exists()
        )
    }

    @Test
    fun alertasRespetanInterruptorYUmbralActual() {
        assertNull(resultado { repo.guardarCategoria("Comida", 100.0, .8, it) })
        assertNull(resultado { repo.guardarPresupuesto("comida", 100.0, "Mensual", it) })
        val usuario =
            FirebaseProvider.firestore
                .collection("usuarios")
                .document(FirebaseProvider.auth.currentUser!!.uid)
        Tasks.await(
            usuario
                .collection("movimientos")
                .add(
                    mapOf(
                        "tipo" to "Gasto",
                        "categoria" to "Comida",
                        "monto" to 85.0,
                        "fecha" to System.currentTimeMillis(),
                    )
                )
        )
        val vm = PresupuestoViewModel()
        compose.setContent { MaterialTheme { PresupuestosScreen(vm = vm) } }
        compose.waitUntil(20_000) {
            vm.presupuestos.size == 1 && vm.movimientos.size == 1 && vm.categorias.size == 1
        }
        captura("presupuestos")
        assertTrue(vm.alerta(vm.presupuestos.single()))
        val categoria = vm.categorias.single()
        assertNull(resultado { repo.actualizarAlertasCategoria(categoria, false, .8, it) })
        compose.waitUntil(10_000) { !vm.categorias.single().alertasActivas }
        assertFalse(vm.alerta(vm.presupuestos.single()))
        assertNull(resultado { repo.actualizarAlertasCategoria(categoria, true, .9, it) })
        compose.waitUntil(10_000) { vm.categorias.single().umbral == .9 }
        assertFalse(vm.alerta(vm.presupuestos.single()))
        assertNull(resultado { repo.actualizarUmbrales(vm.categorias, .5, it) })
        compose.waitUntil(10_000) { vm.categorias.single().umbral == .5 }
        assertTrue(vm.alerta(vm.presupuestos.single()))
    }

    @Test
    fun formularioCategoriaPermiteReintentarTrasNombreDuplicado() {
        assertNull(resultado { repo.guardarCategoria("Comida", 100.0, .8, it) })
        val vm = PresupuestoViewModel()
        compose.setContent {
            MaterialTheme {
                CategoriasAlertasScreen(
                    vm = vm,
                    onGuardarCategoria = { nombre, limite, umbral ->
                        vm.guardarCategoria(nombre, limite, umbral) {}
                    },
                )
            }
        }
        compose.waitUntil(20_000) { !vm.cargando && vm.categorias.size == 1 }
        captura("categorias")
        compose.onAllNodesWithText("Nueva", substring = true)[0].performClick()
        compose
            .onNodeWithText("Nombre de la categoría")
            .performScrollTo()
            .performTextInput("Comida")
        compose.onNodeWithText("Límite mensual de gasto").performScrollTo().performTextInput("50")
        compose.onNodeWithText("Guardar categoría").performScrollTo().performClick()
        compose.waitUntil(20_000) { !vm.cargando && vm.mensaje?.contains("Ya existe") == true }
        compose
            .onNodeWithText("Nombre de la categoría")
            .performScrollTo()
            .performTextReplacement("Transporte")
        compose
            .onNodeWithText("Guardar categoría")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        compose.waitUntil(20_000) { vm.categorias.size == 2 && !vm.cargando }
        assertTrue(vm.categorias.any { it.nombre == "Transporte" })
    }

    @Test
    fun detallePermiteEditarYEliminarSinBorrarCategoria() {
        assertNull(resultado { repo.guardarCategoria("Comida", 100.0, .8, it) })
        assertNull(resultado { repo.guardarPresupuesto("comida", 100.0, "Mensual", it) })
        val vm = PresupuestoViewModel()
        val detalle = androidx.compose.runtime.mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                if (detalle.value) DetallePresupuestoScreen("comida", vm) { detalle.value = false }
                else PresupuestosScreen(vm = vm, onDetalle = { detalle.value = true })
            }
        }
        compose.waitUntil(20_000) { !vm.cargando && vm.presupuestos.size == 1 }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Comida"))
        compose.onNodeWithText("Comida").performClick()
        captura("detalle-presupuesto")
        compose.onNodeWithText("Límite de gasto").performScrollTo().performTextReplacement("200")
        compose.onNodeWithText("Guardar cambios").performScrollTo().performClick()
        compose.waitUntil(20_000) { !detalle.value && vm.presupuestos.single().limite == 200.0 }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Comida"))
        compose.onNodeWithText("Comida").performClick()
        compose.onNodeWithText("Eliminar presupuesto").performScrollTo().performClick()
        compose.onNodeWithText("¿Eliminar presupuesto?").assertIsDisplayed()
        compose.onNodeWithText("Eliminar", substring = false).performClick()
        compose.waitUntil(20_000) { !detalle.value && vm.presupuestos.isEmpty() }
        assertEquals("Comida", vm.categorias.single().nombre)
    }

    @Test
    fun nuevoPresupuestoGuardaDesdeElFormulario() {
        assertNull(resultado { repo.guardarCategoria("Educación", 200.0, .8, it) })
        val vm = PresupuestoViewModel()
        var guardado = false
        compose.setContent {
            MaterialTheme {
                CrearPresupuestoScreen(
                    vm = vm,
                    onGuardar = { categoria, limite, periodo ->
                        vm.guardarPresupuesto(categoria, limite, periodo) { guardado = true }
                    },
                )
            }
        }
        compose.waitUntil(20_000) { !vm.cargando && vm.categorias.size == 1 }
        captura("nuevo-presupuesto")
        compose.onNodeWithText("Categoría").performClick()
        compose.onNodeWithText("Educación").performClick()
        compose.onNodeWithText("Límite de gasto").performTextInput("125,50")
        compose.onNodeWithText("Guardar presupuesto").performScrollTo().performClick()
        compose.waitUntil(20_000) { guardado && vm.presupuestos.size == 1 }
        assertEquals(125.5, vm.presupuestos.single().limite, 0.0)
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun emuladores() {
            FirebaseProvider.auth.useEmulator("10.0.2.2", 9099)
            FirebaseProvider.firestore.useEmulator("10.0.2.2", 8080)
        }
    }
}
