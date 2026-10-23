package com.finanquest.sv.ui.metas

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.data.model.MetaAhorro
import com.finanquest.sv.viewmodel.MetaViewModel
import java.text.NumberFormat
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext

private val vmor=Color(0xFF7C3AED); private val vverde=Color(0xFF16A34A); private val vfondo=Color(0xFFF6F7F9)
private fun monedaMeta(v:Double)=NumberFormat.getCurrencyInstance(Locale("es","SV")).format(v)

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MetasScreen(onVolver:()->Unit={},onNuevaMeta:()->Unit={},onDetalle:(MetaAhorro)->Unit={},vm:MetaViewModel=viewModel()){
 LaunchedEffect(Unit){vm.observar()}
 Scaffold(containerColor=vfondo,topBar={CenterAlignedTopAppBar(title={Text("Mis metas",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}},actions={IconButton(onClick=onNuevaMeta){Icon(Icons.Default.Add,"Nueva meta")}})}){p->
  Column(Modifier.fillMaxSize().padding(p).padding(horizontal=18.dp)){
   Text("Ahorra paso a paso para lo que te importa.",Modifier.padding(vertical=12.dp),color=Color.Gray)
   vm.mensaje?.let{Text(it,color=if(it.contains("guardad",true))vverde else Color(0xFFB42318),modifier=Modifier.padding(bottom=8.dp))}
   when{vm.cargando&&vm.metas.isEmpty()->Box(Modifier.fillMaxSize(),Alignment.Center){CircularProgressIndicator()};vm.metas.isEmpty()->Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){Text("Aún no tienes metas",fontWeight=FontWeight.Bold);Text("Crea una meta para comenzar a ahorrar.",color=Color.Gray);Button(onClick=onNuevaMeta){Text("Crear meta")}};else->LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=24.dp)){items(vm.metas,key={it.id}){m->val progreso=if(m.objetivo>0)(m.actual/m.objetivo).coerceIn(0.0,1.0)else 0.0;Card(Modifier.fillMaxWidth().clickable{onDetalle(m)},shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.padding(16.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text(m.icono,fontSize=26.sp);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(m.nombre,fontWeight=FontWeight.Bold);Text("${monedaMeta(m.actual)} de ${monedaMeta(m.objetivo)}",color=Color.Gray)};Icon(Icons.Default.ChevronRight,null)};Spacer(Modifier.height(12.dp));LinearProgressIndicator(progress={progreso.toFloat()},modifier=Modifier.fillMaxWidth(),color=vmor);Text("${(progreso*100).toInt()}% · faltan ${monedaMeta((m.objetivo-m.actual).coerceAtLeast(0.0))}",Modifier.padding(top=8.dp),color=Color.Gray);OutlinedButton(onClick={onDetalle(m)},modifier=Modifier.fillMaxWidth().padding(top=8.dp)){Text("Ver detalle y aportar")}}}}}}
  }
 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun CrearMetaScreen(onVolver:()->Unit={},onCrear:(String,Double,Double,Long,String)->Unit={_,_,_,_,_->},vm:MetaViewModel=viewModel()){
 var nombre by remember{mutableStateOf("")};var objetivo by remember{mutableStateOf("")};var inicial by remember{mutableStateOf("0")};var fechaLimite by remember{mutableLongStateOf(0L)};var icono by remember{mutableStateOf("🎯")};val context=LocalContext.current
 Scaffold(containerColor=vfondo,topBar={CenterAlignedTopAppBar(title={Text("Nueva meta",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}})}){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  OutlinedTextField(nombre,{nombre=it.take(60)},Modifier.fillMaxWidth(),label={Text("Nombre de la meta")},singleLine=true)
  OutlinedTextField(objetivo,{objetivo=it.filter{c->c.isDigit()||c=='.'||c==','}},Modifier.fillMaxWidth(),label={Text("Monto objetivo")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),leadingIcon={Text("$")},singleLine=true)
  OutlinedTextField(inicial,{inicial=it.filter{c->c.isDigit()||c=='.'||c==','}},Modifier.fillMaxWidth(),label={Text("Ahorro inicial (opcional)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),leadingIcon={Text("$")},singleLine=true)
  Text("Elige un icono",fontWeight=FontWeight.SemiBold);Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){listOf("🎯","💻","✈️","🚘","🏠","🎓","🛡️").forEach{FilterChip(icono==it,{icono=it},{Text(it)})}}
  OutlinedButton(onClick={val hoy=Calendar.getInstance();DatePickerDialog(context,{_,year,month,day->fechaLimite=Calendar.getInstance().apply{set(year,month,day,0,0,0);set(Calendar.MILLISECOND,0)}.timeInMillis},hoy.get(Calendar.YEAR),hoy.get(Calendar.MONTH),hoy.get(Calendar.DAY_OF_MONTH)).apply{datePicker.minDate=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}.timeInMillis}.show()},modifier=Modifier.fillMaxWidth()) { Icon(Icons.Default.CalendarMonth,null);Spacer(Modifier.width(8.dp));Text(if(fechaLimite==0L)"Elegir fecha límite (opcional)" else SimpleDateFormat("dd/MM/yyyy",Locale("es","SV")).format(Date(fechaLimite))) }
  vm.mensaje?.let{Text(it,color=Color(0xFFB42318))}
  Button(onClick={onCrear(nombre,objetivo.replace(",",".").toDoubleOrNull()?:Double.NaN,inicial.replace(",",".").toDoubleOrNull()?:Double.NaN,fechaLimite,icono)},enabled=!vm.cargando&&nombre.isNotBlank()&&(objetivo.replace(",",".").toDoubleOrNull()?:0.0)>0,modifier=Modifier.fillMaxWidth()){Text(if(vm.cargando)"Guardando…" else "Guardar meta")}
  OutlinedButton(onClick=onVolver,modifier=Modifier.fillMaxWidth()){Text("Cancelar")}
 }}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DetalleMetaScreen(meta:MetaAhorro,onVolver:()->Unit={},vm:MetaViewModel=viewModel()){
 var monto by remember{mutableStateOf("")};var dialogo by remember{mutableStateOf(false)}
 LaunchedEffect(meta.id){vm.observar();vm.observarAportes(meta.id)}
 val actual=vm.metas.firstOrNull{it.id==meta.id}?:meta;val progreso=if(actual.objetivo>0)(actual.actual/actual.objetivo).coerceIn(0.0,1.0)else 0.0
 Scaffold(containerColor=vfondo,topBar={CenterAlignedTopAppBar(title={Text("Detalle de meta")},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}})}){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Card(colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.fillMaxWidth().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(actual.icono,fontSize=40.sp);Text(actual.nombre,fontWeight=FontWeight.Bold,fontSize=22.sp);Text("${monedaMeta(actual.actual)} de ${monedaMeta(actual.objetivo)}",color=Color.Gray);LinearProgressIndicator(progress={progreso.toFloat()},modifier=Modifier.fillMaxWidth().padding(top=14.dp));Text("${(progreso*100).toInt()}% completado",Modifier.padding(top=8.dp));if(actual.fechaLimite>0)Text("Límite: ${SimpleDateFormat("dd/MM/yyyy",Locale("es","SV")).format(Date(actual.fechaLimite))}",color=Color.Gray)}}
  vm.mensaje?.let{Text(it,color=if(it.contains("guardad",true))vverde else Color(0xFFB42318))};Button(onClick={dialogo=true},modifier=Modifier.fillMaxWidth()){Text("Agregar aporte")};Text("Actividad reciente",fontWeight=FontWeight.Bold,fontSize=18.sp)
  if(vm.aportes.isEmpty())Text("Aún no hay aportes.",color=Color.Gray) else vm.aportes.forEach{Text("Aporte · ${SimpleDateFormat("dd/MM/yyyy",Locale("es","SV")).format(Date(it.fecha))} · +${monedaMeta(it.monto)}",Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(12.dp)).padding(14.dp))}
 }}
 if(dialogo)AlertDialog(onDismissRequest={dialogo=false},title={Text("Agregar aporte")},text={OutlinedTextField(monto,{monto=it},label={Text("Monto")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),leadingIcon={Text("$")})},confirmButton={TextButton(onClick={vm.aportar(actual,monto){dialogo=false;monto=""}}){Text("Guardar")}},dismissButton={TextButton(onClick={dialogo=false}){Text("Cancelar")}})
}
