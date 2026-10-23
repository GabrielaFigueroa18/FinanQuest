package com.finanquest.sv.ui.presupuestos

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
import com.finanquest.sv.data.model.Categoria
import com.finanquest.sv.viewmodel.PresupuestoViewModel
import java.text.NumberFormat
import java.util.Locale
import java.util.Calendar

private val vp=Color(0xFF16A34A);private val fp=Color(0xFFF6F7F9);private fun dineroPres(v:Double)=NumberFormat.getCurrencyInstance(Locale("es","SV")).format(v)
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PresupuestosScreen(onVolver:()->Unit={},onCrear:()->Unit={},onCategorias:()->Unit={},onDetalle:(String)->Unit={},onReportes:()->Unit={},vm:PresupuestoViewModel=viewModel()){
 LaunchedEffect(Unit){vm.observar()};val inicio=Calendar.getInstance().apply{set(Calendar.DAY_OF_MONTH,1);set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}.timeInMillis
 val gastoMes=vm.movimientos.filter{it.tipo.equals("Gasto",true)&&it.fecha>=inicio}.sumOf{it.monto};val limiteTotal=vm.presupuestos.filter{it.activo}.sumOf{it.limite}
 Scaffold(containerColor=fp,topBar={CenterAlignedTopAppBar(title={Text("Presupuestos",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}},actions={IconButton(onClick=onCategorias){Icon(Icons.Default.Tune,"Categorías y alertas")}})}){p->Column(Modifier.fillMaxSize().padding(p).padding(horizontal=18.dp)){
  Card(Modifier.fillMaxWidth().padding(top=12.dp,bottom=12.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFEAF8EF))){Column(Modifier.padding(18.dp)){Text("Resumen del mes",fontWeight=FontWeight.Bold,fontSize=17.sp);Text("${dineroPres(gastoMes)} de ${dineroPres(limiteTotal)} utilizados",color=Color.Gray);LinearProgressIndicator(progress={if(limiteTotal>0)(gastoMes/limiteTotal).coerceIn(0.0,1.0).toFloat()else 0f},modifier=Modifier.fillMaxWidth().padding(top=10.dp),color=vp);Text("${dineroPres((limiteTotal-gastoMes).coerceAtLeast(0.0))} disponibles",Modifier.padding(top=8.dp),color=vp)} }
  vm.mensaje?.let{Text(it,color=if(it.contains("guardad",true))vp else Color(0xFFB42318),modifier=Modifier.padding(bottom=8.dp))}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Mis categorías",fontSize=19.sp,fontWeight=FontWeight.Bold);TextButton(onClick=onCategorias){Text("Alertas")}}
  when{vm.cargando&&vm.presupuestos.isEmpty()->Box(Modifier.fillMaxWidth().weight(1f),Alignment.Center){CircularProgressIndicator()};vm.presupuestos.isEmpty()->Column(Modifier.fillMaxWidth().weight(1f),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){Text("Aún no tienes presupuestos",fontWeight=FontWeight.Bold);Text("Define un límite para controlar tus gastos.",color=Color.Gray);Button(onClick=onCrear){Text("Crear presupuesto")}};else->LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(vertical=8.dp)){items(vm.presupuestos.filter{it.activo},key={it.id}){b->val usado=vm.gasto(b.categoria,b.inicioPeriodo);val ratio=if(b.limite>0)(usado/b.limite).coerceIn(0.0,1.0)else 0.0;Card(Modifier.fillMaxWidth().clickable{onDetalle(b.categoria)},colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(16.dp)){Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(b.categoria,fontWeight=FontWeight.Bold);Text("${dineroPres(usado)} de ${dineroPres(b.limite)} · ${b.periodo}",color=Color.Gray,fontSize=13.sp)};Text("${(ratio*100).toInt()}%",color=if(ratio>=b.alertaUmbral)Color(0xFFDC2626)else vp,fontWeight=FontWeight.Bold)};LinearProgressIndicator(progress={ratio.toFloat()},modifier=Modifier.fillMaxWidth().padding(top=10.dp),color=if(ratio>=b.alertaUmbral)Color(0xFFDC2626)else vp)}}}}}
  Button(onClick=onCrear,modifier=Modifier.fillMaxWidth().padding(top=8.dp)){Icon(Icons.Default.Add,null);Text(" Crear presupuesto")};OutlinedButton(onClick=onCategorias,modifier=Modifier.fillMaxWidth()){Text("Categorías y alertas")};TextButton(onClick=onReportes,modifier=Modifier.align(Alignment.CenterHorizontally)){Text("Ver reportes")}
 }}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun CrearPresupuestoScreen(onVolver:()->Unit={},onGuardar:(String,String,String)->Unit={_,_,_->},vm:PresupuestoViewModel=viewModel()){
 LaunchedEffect(Unit){vm.observar()};var categoria by remember{mutableStateOf("")};var limite by remember{mutableStateOf("")};var periodo by remember{mutableStateOf("Mensual")};var expand by remember{mutableStateOf(false)}
 Scaffold(containerColor=fp,topBar={CenterAlignedTopAppBar(title={Text("Nuevo presupuesto")},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}})}){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  ExposedDropdownMenuBox(expand,{expand=it}){OutlinedTextField(categoria,{categoria=it},modifier=Modifier.menuAnchor().fillMaxWidth(),label={Text("Categoría")},readOnly=true,trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expand)});ExposedDropdownMenu(expand,{expand=false}){vm.categorias.forEach{DropdownMenuItem(text={Text(it.nombre)},onClick={categoria=it.nombre;expand=false})}}}
  if(vm.categorias.isEmpty())Text("Primero crea una categoría en Categorías y alertas.",color=Color.Gray)
  OutlinedTextField(limite,{limite=it.filter{c->c.isDigit()||c=='.'||c==','}},Modifier.fillMaxWidth(),label={Text("Límite de gasto")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),leadingIcon={Text("$")},singleLine=true)
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Semanal","Mensual").forEach{FilterChip(periodo==it,{periodo=it},{Text(it)})}}
  Text("La alerta se calcula al alcanzar el 80% del límite.",color=Color.Gray);vm.mensaje?.let{Text(it,color=Color(0xFFB42318))}
  Button(onClick={onGuardar(categoria,limite,periodo)},enabled=!vm.cargando&&categoria.isNotBlank()&&(limite.replace(",",".").toDoubleOrNull()?:0.0)>0,modifier=Modifier.fillMaxWidth()){Text(if(vm.cargando)"Guardando…" else "Guardar presupuesto")}
 }}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun CategoriasAlertasScreen(onVolver:()->Unit={},onGuardarCategoria:(String,String,Double)->Unit={_,_,_->},vm:PresupuestoViewModel=viewModel()){
 LaunchedEffect(Unit){vm.observar()};var nueva by remember{mutableStateOf(false)};var nombre by remember{mutableStateOf("")};var limite by remember{mutableStateOf("")};var umbral by remember{mutableFloatStateOf(.8f)}
 Scaffold(containerColor=fp,topBar={CenterAlignedTopAppBar(title={Text("Categorías y alertas")},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}})}){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Text("Configura categorías y umbrales de aviso.",color=Color.Gray);vm.mensaje?.let{Text(it,color=if(it.contains("guardad",true))vp else Color(0xFFB42318))}
  vm.categorias.forEach{c->Card(colors=CardDefaults.cardColors(containerColor=Color.White)){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(c.nombre,fontWeight=FontWeight.SemiBold);Text("Límite ${dineroPres(c.limite)} · alerta al ${(c.umbral*100).toInt()}%",color=Color.Gray)};Switch(checked=c.alertasActivas,onCheckedChange={vm.actualizarAlertasCategoria(c,it,c.umbral)})}}}
  if(vm.categorias.isEmpty()&&!vm.cargando)Text("No hay categorías. Agrega una para registrar presupuestos.",color=Color.Gray)
  if(vm.categorias.isNotEmpty()){Text("Umbral de alerta por categoría",fontWeight=FontWeight.SemiBold);var umbralGlobal by remember{mutableFloatStateOf(.8f)};Text("Avisar al ${(umbralGlobal*100).toInt()}%");Slider(umbralGlobal,{umbralGlobal=it},valueRange=.5f..1f,onValueChangeFinished={vm.categorias.forEach{vm.actualizarAlertasCategoria(it,it.alertasActivas,umbralGlobal.toDouble())}})}
  if(nueva){OutlinedTextField(nombre,{nombre=it.take(40)},Modifier.fillMaxWidth(),label={Text("Nombre")},singleLine=true);OutlinedTextField(limite,{limite=it.filter{c->c.isDigit()||c=='.'||c==','}},Modifier.fillMaxWidth(),label={Text("Límite predeterminado")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal));Text("Umbral de alerta: ${(umbral*100).toInt()}%");Slider(umbral,{umbral=it},valueRange=.5f..1f);Button(onClick={onGuardarCategoria(nombre,limite,umbral.toDouble());nueva=false;nombre="";limite=""},enabled=nombre.isNotBlank()&&(limite.replace(",",".").toDoubleOrNull()?:0.0)>0,modifier=Modifier.fillMaxWidth()){Text("Guardar categoría")}}
  else Button(onClick={nueva=true},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Add,null);Text(" Nueva categoría")}
 }}
}
