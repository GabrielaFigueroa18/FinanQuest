
package com.finanquest.sv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.finanquest.sv.ui.bienvenida.BienvenidaScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                BienvenidaScreen(
                    onIniciarSesion = {
                        // Aquí conectaremos LoginScreen
                    },
                    onCrearCuenta = {
                        // Aquí conectaremos RegistroScreen
                    }
                )
            }
        }
    }
}
