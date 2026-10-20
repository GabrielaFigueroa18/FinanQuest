package com.finanquest.sv.ui.metas

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VerdeMeta = Color(0xFF16A34A)
private val FondoMeta = Color(0xFFF6F7F9)
private data class MetaUi(val nombre:String, val actual:Double, val objetivo:Double, val icono:String, val color:Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen(
    onVolver: () -> Unit = {},
    onNuevaMeta: () -> Unit = {},
    onDetalle: (String) -> Unit = {},
    onInicio: () -> Unit = {},
    onMovimientos: () -> Unit = {},
    onMascota: () -> Unit = {},
    onNiveles: () -> Unit = {},
    onPerfil: () -> Unit = {}
) {
    val metas = remember { mutableStateListOf(
        MetaUi("Nueva Laptop",850.0,1200.0,"💻",Color(0xFF16A34A)),
        MetaUi("Viaje de verano",300.0,800.0,"✈️",Color(0xFFE88962)),
        MetaUi("Fondo de emergencia",1500.0,2000.0,"🛡️",Color(0xFF6555D9))
    ) }
    Scaffold(containerColor=FondoMeta, topBar={CenterAlignedTopAppBar(title={Text("Tus Metas",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}})},
        bottomBar={
            NavigationBar(containerColor = Color.White, tonalElevation = 3.dp) {
                ItemMenuMeta("Inicio", Icons.Default.Home, false, onInicio)
                ItemMenuMeta("Mov.", Icons.Default.AccountBalanceWallet, false, onMovimientos)
                ItemMenuMeta("Metas", Icons.Default.TrackChanges, true, {})
                ItemMenuMeta("Mascota", Icons.Default.Pets, false, onMascota)
                ItemMenuMeta("Nivel", Icons.Default.EmojiEvents, false, onNiveles)
                ItemMenuMeta("Perfil", Icons.Default.PersonOutline, false, onPerfil)
            }
        }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).padding(horizontal=18.dp),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(vertical=16.dp)) {
            item { Text("Construye tus sueños, paso a paso.",color=Color.Gray); Spacer(Modifier.height(8.dp)); Button(onClick=onNuevaMeta,colors=ButtonDefaults.buttonColors(containerColor=VerdeMeta),shape=RoundedCornerShape(24.dp)){Icon(Icons.Default.Add,null);Text(" Nueva Meta")} }
            items(metas){ meta ->
                val progreso=(meta.actual/meta.objetivo).coerceIn(0.0,1.0)
                Card(Modifier.fillMaxWidth().clickable{onDetalle(meta.nombre)},shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment=Alignment.CenterVertically){ Text(meta.icono,fontSize=24.sp); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)){Text(meta.nombre,fontWeight=FontWeight.Bold);Text("$${meta.actual.toInt()} / $${meta.objetivo.toInt()}",color=Color.Gray,fontSize=13.sp)}; IconButton(onClick={onDetalle(meta.nombre)}){Icon(Icons.Default.MoreVert,"Opciones")} }
                        Spacer(Modifier.height(14.dp)); LinearProgressIndicator(progress={progreso.toFloat()},modifier=Modifier.fillMaxWidth().height(9.dp),color=meta.color,trackColor=Color(0xFFE9EBEF),strokeCap= androidx.compose.ui.graphics.StrokeCap.Round)
                        Spacer(Modifier.height(6.dp)); Text("${(progreso*100).toInt()}% completado · faltan $${(meta.objetivo-meta.actual).toInt()}",color=Color.Gray,fontSize=12.sp)
                        Spacer(Modifier.height(12.dp)); OutlinedButton(onClick={onDetalle(meta.nombre)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Text("Ahorrar rápido $50",color=VerdeMeta)}
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ItemMenuMeta(
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = seleccionado,
        onClick = onClick,
        icon = { Icon(icono, contentDescription = texto, modifier = Modifier.size(21.dp)) },
        label = { Text(texto, fontSize = 9.sp, maxLines = 1) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF7C3AED),
            selectedTextColor = Color(0xFF7C3AED),
            indicatorColor = Color(0xFFEDE9FE),
            unselectedIconColor = Color(0xFF777D87),
            unselectedTextColor = Color(0xFF777D87)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearMetaScreen(onVolver:()->Unit={}, onCrear:(String,Double,Double)->Unit={_,_,_->}) {
    var nombre by remember{mutableStateOf("")}; var monto by remember{mutableStateOf("")}; var aporte by remember{mutableStateOf("")}; var fecha by remember{mutableStateOf("")}; var icono by remember{mutableStateOf("💻")}
    Scaffold(containerColor=FondoMeta,topBar={CenterAlignedTopAppBar(title={Text("Nueva Meta",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}})}){p->
        Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            Text("¿Qué quieres lograr?",fontWeight=FontWeight.SemiBold); OutlinedTextField(nombre,{nombre=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("Ej. Nueva laptop, viaje…")},shape=RoundedCornerShape(14.dp),singleLine=true)
            Text("Monto objetivo",fontWeight=FontWeight.SemiBold); OutlinedTextField(monto,{monto=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("$ 0.00")},leadingIcon={Text("$")},shape=RoundedCornerShape(14.dp),singleLine=true)
            Text("Ahorro inicial (opcional)",fontWeight=FontWeight.SemiBold); OutlinedTextField(aporte,{aporte=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("$ 0.00")},leadingIcon={Text("$")},shape=RoundedCornerShape(14.dp),singleLine=true)
            Text("Personaliza tu meta",fontWeight=FontWeight.SemiBold); Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("💻","✈️","🚘","🏠","🎓","🛡️").forEach { FilterChip(selected=icono==it,onClick={icono=it},label={Text(it)} ) }}
            Text("Fecha límite",fontWeight=FontWeight.SemiBold); OutlinedTextField(fecha,{fecha=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("dd/mm/aaaa")},shape=RoundedCornerShape(14.dp),singleLine=true)
            Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFEDE9FE))){Text("🏅  Recompensa al completar\nAlcanza tu meta y gana experiencia.",Modifier.padding(16.dp),color=Color(0xFF5B35B1))}
            Spacer(Modifier.height(8.dp)); Button(onClick={val m=monto.toDoubleOrNull()?:0.0;if(nombre.isNotBlank()&&m>0)onCrear(nombre,m,aporte.toDoubleOrNull()?:0.0)},enabled=nombre.isNotBlank()&&(monto.toDoubleOrNull()?:0.0)>0,modifier=Modifier.fillMaxWidth().height(52.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF7C3AED)),shape=RoundedCornerShape(15.dp)){Text("＋  Crear Meta",fontWeight=FontWeight.Bold)}
            OutlinedButton(onClick=onVolver,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp)){Text("Cancelar")}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMetaScreen(nombre:String="Nueva Laptop",onVolver:()->Unit={},onAportar:(Double)->Unit={}){
    var cantidad by remember{mutableStateOf("")}; var mostrar by remember{mutableStateOf(false)}
    Scaffold(containerColor=FondoMeta,topBar={CenterAlignedTopAppBar(title={Text("Detalle de meta")},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}})}){p->Column(Modifier.fillMaxSize().padding(p).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
        Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.fillMaxWidth().padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("💻",fontSize=42.sp);Text(nombre,fontWeight=FontWeight.Bold,fontSize=22.sp);Text("$850 de $1,200",color=Color.Gray);Spacer(Modifier.height(18.dp));CircularProgressIndicator(progress={.70f},color=Color(0xFFE65A91),trackColor=Color(0xFFF0F1F4),strokeWidth=12.dp);Text("70%",fontWeight=FontWeight.Bold);Text("Te faltan $350 para alcanzar tu sueño",color=Color.Gray)}}
        Text("Actividad reciente",fontWeight=FontWeight.Bold,fontSize=18.sp); listOf("Aporte de ahorro rápido · Hoy · +$50","Aporte mensual · 1 oct · +$200","Aporte inicial · 15 sep · +$600").forEach{Text("💰  $it",Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(14.dp)).padding(15.dp))}
        Button(onClick={mostrar=true},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF7C3AED))){Text("＋  Agregar aporte")}
        if(mostrar) AlertDialog(onDismissRequest={mostrar=false},title={Text("Agregar ahorro")},text={OutlinedTextField(cantidad,{cantidad=it},label={Text("Monto")},leadingIcon={Text("$")})},confirmButton={TextButton(onClick={cantidad.toDoubleOrNull()?.let(onAportar);mostrar=false}){Text("Guardar")}},dismissButton={TextButton(onClick={mostrar=false}){Text("Cancelar")}})
    }}
}



