
package com.finanquest.sv.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

private val MoradoFinanQuest = Color(0xFF7C3AED)
private val VerdeFinanQuest = Color(0xFF16A34A)

@Composable
fun FinanQuestBottomBar(
    pantallaActual: String,
    onInicio: () -> Unit,
    onMovimientos: () -> Unit,
    onCuentas: () -> Unit,
    onMetas: () -> Unit,
    onMisiones: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White
    ) {

        val opciones = listOf(
            Triple("Inicio", Icons.Filled.Home, onInicio),
            Triple("Movimientos", Icons.Filled.List, onMovimientos),
            Triple("Cuentas", Icons.Filled.AccountBalanceWallet, onCuentas),
            Triple("Metas", Icons.Filled.Flag, onMetas),
            Triple("Misiones", Icons.Filled.Star, onMisiones)
        )

        opciones.forEach { opcion ->
            NavigationBarItem(
                selected = pantallaActual == opcion.first,
                onClick = opcion.third,
                icon = {
                    Icon(
                        imageVector = opcion.second,
                        contentDescription = opcion.first
                    )
                },
                label = {
                    Text(
                        text = opcion.first,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MoradoFinanQuest,
                    selectedTextColor = MoradoFinanQuest,
                    indicatorColor = Color(0xFFF0E9FF),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}
