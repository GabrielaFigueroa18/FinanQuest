package com.finanquest.sv.ui.presupuestos

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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

private val VerdePres = Color(0xFF16A34A)
private val FondoPres = Color(0xFFF6F7F9)
private data class CategoriaUi(val nombre:String,val icono:String,val limite:Double,val gastado:Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestosScreen(onVolver:()->Unit={},onCrear:()->Unit={},onCategorias:()->Unit={},onDetalle:(String)->Unit={},onReportes:()->Unit={}){
    val categorias=listOf(CategoriaUi("Comida","🍴",200.0,126.0),CategoriaUi("Transporte","🚌",50.0,32.0),CategoriaUi("Estudios","🎓",150.0,72.0),CategoriaUi("Ocio","🎮",100.0,55.0))
    Scaffold(containerColor=FondoPres,topBar={CenterAlignedTopAppBar(title={Text("Presupuestos",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}},actions={IconButton(onClick=onCategorias){Icon(Icons.Default.Tune,"Categorías y alertas")}})}){p->
        Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFEAF8EF)),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(18.dp)){Text("Presupuesto de octubre",fontWeight=FontWeight.Bold,fontSize=17.sp);Text("$213 de $500 utilizados",color=Color.Gray);Spacer(Modifier.height(10.dp));LinearProgressIndicator(progress={.426f},modifier=Modifier.fillMaxWidth().height(9.dp),color=VerdePres,trackColor=Color.White);Text("Te quedan $287 este mes",Modifier.padding(top=8.dp),color=VerdePres,fontWeight=FontWeight.SemiBold)}}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Mis categorías",fontSize=19.sp,fontWeight=FontWeight.Bold);TextButton(onClick=onCategorias){Text("ALERTAS",color=VerdePres)}}
            categorias.forEach{ c -> val ratio=(c.gastado/c.limite).toFloat().coerceIn(0f,1f); Card(Modifier.fillMaxWidth().clickable{onDetalle(c.nombre)},shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.padding(16.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text(c.icono,fontSize=24.sp);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(c.nombre,fontWeight=FontWeight.Bold);Text("$${c.gastado.toInt()} de $${c.limite.toInt()}",color=Color.Gray,fontSize=13.sp)};Text("${(ratio*100).toInt()}%",color=if(ratio>.8f)Color(0xFFDC2626) else VerdePres,fontWeight=FontWeight.Bold)};Spacer(Modifier.height(12.dp));LinearProgressIndicator(progress={ratio},modifier=Modifier.fillMaxWidth().height(8.dp),color=if(ratio>.8f)Color(0xFFEF4444)else VerdePres,trackColor=Color(0xFFE9EBEF))}}
            }
            OutlinedButton(onClick=onCategorias,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Icon(Icons.Default.NotificationsActive,null);Text("  Configurar categorías y alertas")}
            Button(onClick=onCrear,modifier=Modifier.fillMaxWidth().height(52.dp),colors=ButtonDefaults.buttonColors(containerColor=VerdePres),shape=RoundedCornerShape(15.dp)){Icon(Icons.Default.Add,null);Text("  Crear presupuesto")}
            TextButton(onClick=onReportes,modifier=Modifier.align(Alignment.CenterHorizontally)){Text("Ver reportes de gastos")}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPresupuestoScreen(onVolver:()->Unit={},onGuardar:(String,Double)->Unit={_,_->}){
    var categoria by remember{mutableStateOf("Comida")};var limite by remember{mutableStateOf("")};var periodo by remember{mutableStateOf("Mensual")}
    Scaffold(containerColor=FondoPres,topBar={CenterAlignedTopAppBar(title={Text("Nuevo presupuesto",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}})}){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Text("Categoría",fontWeight=FontWeight.SemiBold);var expanded by remember{mutableStateOf(false)};ExposedDropdownMenuBox(expanded=expanded,onExpandedChange={expanded=!expanded}){OutlinedTextField(categoria,{categoria=it},modifier=Modifier.menuAnchor().fillMaxWidth(),readOnly=true,trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},shape=RoundedCornerShape(14.dp));ExposedDropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){listOf("Comida","Transporte","Estudios","Ocio","Otros").forEach{DropdownMenuItem(text={Text(it)},onClick={categoria=it;expanded=false})}}}
        Text("Límite de gasto",fontWeight=FontWeight.SemiBold);OutlinedTextField(limite,{limite=it},modifier=Modifier.fillMaxWidth(),leadingIcon={Text("$")},placeholder={Text("0.00")},singleLine=true,shape=RoundedCornerShape(14.dp))
        Text("Periodo",fontWeight=FontWeight.SemiBold);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Semanal","Mensual").forEach{FilterChip(selected=periodo==it,onClick={periodo=it},label={Text(it)})}}
        Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFEFF3FF)),shape=RoundedCornerShape(16.dp)){Text("🔔  Recibirás una alerta cuando alcances el 80% del límite.",Modifier.padding(16.dp),color=Color(0xFF3B5EB5))}
        Spacer(Modifier.height(12.dp));Button(onClick={val v=limite.toDoubleOrNull()?:0.0;if(v>0)onGuardar(categoria,v)},enabled=(limite.toDoubleOrNull()?:0.0)>0,modifier=Modifier.fillMaxWidth().height(52.dp),colors=ButtonDefaults.buttonColors(containerColor=VerdePres),shape=RoundedCornerShape(15.dp)){Text("Guardar presupuesto")}
    }}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasAlertasScreen(onVolver:()->Unit={},onGuardarCategoria:(String,Double)->Unit={_,_->}){
    var push by remember{mutableStateOf(true)};var semanal by remember{mutableStateOf(false)};var umbral by remember{mutableFloatStateOf(.8f)};var nueva by remember{mutableStateOf(false)};var nombre by remember{mutableStateOf("")};var limite by remember{mutableStateOf("")}
    Scaffold(containerColor=FondoPres,topBar={CenterAlignedTopAppBar(title={Text("Categorías y Alertas",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}})}){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Text("Configura tus gastos y recibe notificaciones para mantener tu presupuesto bajo control.",color=Color.Gray)
        Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Mis categorías",fontWeight=FontWeight.Bold);TextButton(onClick={nueva=true}){Text("＋ Nueva",color=VerdePres)}};listOf("🍴  Comida     Presupuesto: $200.00/mes","🚌  Transporte     Presupuesto: $50.00/mes","🎓  Estudios     Presupuesto: $150.00/mes").forEach{Text(it,Modifier.fillMaxWidth().padding(vertical=8.dp).background(FondoPres,RoundedCornerShape(12.dp)).padding(12.dp),fontSize=13.sp)}}}
        Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp)){Text("Alertas Globales",fontWeight=FontWeight.Bold);FilaSwitch("Notificaciones Push","Recibir alertas en el dispositivo",push){push=it};FilaSwitch("Resumen Semanal","Reporte de gastos todos los lunes",semanal){semanal=it}}}
        Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp)){Text("⚠  Umbrales de Gasto",fontWeight=FontWeight.Bold);Text("Avisarme cuando me acerque al límite mensual.",Modifier.padding(top=10.dp),color=Color.Gray,fontSize=13.sp);Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Slider(value=umbral,onValueChange={umbral=it},valueRange=.5f..1f,modifier=Modifier.weight(1f));Text("${(umbral*100).toInt()}%",color=VerdePres,fontWeight=FontWeight.Bold)};Text("Se enviará una alerta crítica al superar este porcentaje en cualquier categoría.",color=Color(0xFFB42342),fontSize=12.sp)}}
        if(nueva){Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Nueva Categoría",fontWeight=FontWeight.Bold);OutlinedTextField(nombre,{nombre=it},label={Text("Nombre de la categoría")},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp));OutlinedTextField(limite,{limite=it},label={Text("Límite mensual")},leadingIcon={Text("$")},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp));Button(onClick={val v=limite.toDoubleOrNull()?:0.0;if(nombre.isNotBlank()&&v>0)onGuardarCategoria(nombre,v);nueva=false},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=VerdePres)){Text("Guardar Categoría")}}}}
    }}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun FilaSwitch(titulo:String,subtitulo:String,valor:Boolean,onCambio:(Boolean)->Unit){Row(Modifier.fillMaxWidth().padding(top=12.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(titulo,fontWeight=FontWeight.SemiBold);Text(subtitulo,color=Color.Gray,fontSize=12.sp)};Switch(checked=valor,onCheckedChange=onCambio,colors=SwitchDefaults.colors(checkedThumbColor=Color.White,checkedTrackColor=VerdePres))}}

