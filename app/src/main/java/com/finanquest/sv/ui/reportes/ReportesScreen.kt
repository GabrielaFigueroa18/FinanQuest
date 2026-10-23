package com.finanquest.sv.ui.reportes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanquest.sv.viewmodel.MovimientoViewModel
import com.finanquest.sv.utils.FinanzasIntegrante2
import java.util.Calendar
import java.text.NumberFormat
import java.util.Locale

private val FondoReporte=Color(0xFFF6F7F9)
private val VerdeReporte=Color(0xFF16B65A)
private val colores= listOf(Color(0xFF9BDEF2),Color(0xFF20BF69),Color(0xFFE65F91),Color(0xFF8C87E8))
private data class ItemGasto(val nombre:String,val monto:Double,val porcentaje:Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(onVolver:()->Unit={},onMovimientos:()->Unit={},onInicio:()->Unit={},vm:MovimientoViewModel=viewModel()){
    LaunchedEffect(Unit){vm.observar()}
    var periodo by remember{mutableStateOf("Este mes")};var menu by remember{mutableStateOf(false)};var filtros by remember{mutableStateOf(false)}
    val desde=FinanzasIntegrante2.inicioPeriodoReporte(periodo)
    val movimientos=vm.movimientos.filter{it.fecha>=desde}
    val (ingresos,gastos,ahorro)=FinanzasIntegrante2.totales(movimientos,desde)
    val items=movimientos.filter{it.tipo.equals("Gasto",true)}.groupBy{it.categoria.ifBlank{"Otros"}}.map{(nombre,lista)->ItemGasto(nombre,lista.sumOf{it.monto},if(gastos>0)(lista.sumOf{it.monto}*100/gastos).toInt()else 0)}.sortedByDescending{it.monto}.take(4)
    fun dinero(v:Double)=NumberFormat.getCurrencyInstance(Locale("es","SV")).format(v)
    Scaffold(containerColor=FondoReporte,topBar={CenterAlignedTopAppBar(title={Text("Reportes",fontWeight=FontWeight.Bold,fontSize=22.sp)},navigationIcon={IconButton(onClick=onVolver){Icon(Icons.Default.ArrowBack,"Volver")}},actions={Box{IconButton(onClick={menu=true}){Icon(Icons.Default.MoreVert,"Más opciones")};DropdownMenu(expanded=menu,onDismissRequest={menu=false}){DropdownMenuItem(text={Text("Compartir reporte")},onClick={onMovimientos});DropdownMenuItem(text={Text("Ver movimientos")},onClick=onMovimientos)}}})}){p->
        Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(horizontal=18.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
            Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFEDEDF0)),shape=RoundedCornerShape(18.dp)){Row(Modifier.fillMaxWidth().clickable{filtros=true}.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.CalendarMonth,null,tint=Color(0xFF7C3AED));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("PERIODO ACTUAL",fontSize=10.sp,color=Color.Gray,fontWeight=FontWeight.Bold);Text(periodo,fontWeight=FontWeight.SemiBold)};Icon(Icons.Default.FilterList,null,tint=Color(0xFF7C3AED));Text(" Filtrar",color=Color(0xFF7C3AED))}}
            Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(20.dp)){Column(Modifier.fillMaxWidth().padding(18.dp)){Box(Modifier.fillMaxWidth().height(130.dp),contentAlignment=Alignment.Center){Text("📈",fontSize=72.sp);Text("💵      📊      💰",fontSize=28.sp)};Text("¡Buen trabajo!",color=VerdeReporte,fontWeight=FontWeight.Bold);Text("Ingresos menos gastos en el periodo seleccionado: ${dinero(ahorro)}.",fontWeight=FontWeight.SemiBold)}}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Resumen("INGRESOS",dinero(ingresos),Color(0xFFE9F8EE),VerdeReporte,Modifier.weight(1f));Resumen("GASTOS",dinero(gastos),Color(0xFFFFECEF),Color(0xFFD84C5C),Modifier.weight(1f));Resumen("AHORRO",dinero(ahorro),Color(0xFFEAF5FA),Color(0xFF1683A5),Modifier.weight(1f))}
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Distribución de Gastos",Modifier.weight(1f),fontWeight=FontWeight.Bold,fontSize=18.sp);TextButton(onClick=onMovimientos){Text("VER TODO",color=Color(0xFF159BC2),fontWeight=FontWeight.Bold)}}
            if(vm.cargando&&movimientos.isEmpty()) Box(Modifier.fillMaxWidth().height(130.dp),Alignment.Center){CircularProgressIndicator()}
            else if(items.isEmpty()) Card(colors=CardDefaults.cardColors(containerColor=Color.White)){Text("No hay gastos en este periodo.",Modifier.padding(18.dp),color=Color.Gray)}
            else Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(horizontal=16.dp,vertical=18.dp)){Box(Modifier.fillMaxWidth().height(205.dp),contentAlignment=Alignment.Center){Donut(items);Column(horizontalAlignment=Alignment.CenterHorizontally){Text("TOTAL",fontSize=10.sp,color=Color.Gray);Text(dinero(gastos),fontSize=21.sp,fontWeight=FontWeight.Bold)}};Spacer(Modifier.height(14.dp));items.forEachIndexed{index,item->Row(Modifier.fillMaxWidth().clickable{onMovimientos()}.padding(vertical=9.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(11.dp).background(colores[index%colores.size],RoundedCornerShape(50)));Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(item.nombre);Text("${item.porcentaje}%",fontSize=11.sp,color=Color.Gray)};Text(dinero(item.monto),fontWeight=FontWeight.SemiBold)};if(index<items.lastIndex)HorizontalDivider(color=Color(0xFFE8E8EA))};Spacer(Modifier.height(8.dp));Button(onClick=onMovimientos,modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=VerdeReporte),shape=RoundedCornerShape(16.dp)){Text("Ver movimientos",fontWeight=FontWeight.Bold)}}}
            Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF8050D9)),shape=RoundedCornerShape(20.dp)){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.TrendingUp,null,tint=Color.White,modifier=Modifier.size(32.dp));Spacer(Modifier.width(12.dp));Column{Text("¡Sigue así!",color=Color.White,fontWeight=FontWeight.Bold,fontSize=16.sp);Text("Revisa la distribución de gastos para decidir cuánto destinar a tus metas.",color=Color.White,fontSize=13.sp)}}}
            Spacer(Modifier.height(8.dp))
        }
    }
    if(filtros)AlertDialog(onDismissRequest={filtros=false},title={Text("Selecciona un periodo")},text={Column{listOf("Este mes","Últimos 3 meses","Este año").forEach{nombre->TextButton(onClick={periodo=nombre;filtros=false}){Text(nombre)}}}},confirmButton={TextButton(onClick={filtros=false}){Text("Cerrar")}})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Resumen(titulo:String,valor:String,fondo:Color,tinta:Color,modifier:Modifier=Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=fondo),shape=RoundedCornerShape(18.dp)){Column(Modifier.fillMaxWidth().padding(vertical=15.dp,horizontal=6.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(if(titulo=="INGRESOS")Icons.Default.TrendingUp else if(titulo=="GASTOS")Icons.Default.TrendingDown else Icons.Default.Savings,null,tint=tinta);Text(titulo,fontSize=10.sp,color=Color.Gray,fontWeight=FontWeight.SemiBold);Text(valor,fontSize=19.sp,color=tinta,fontWeight=FontWeight.Bold);Text("ESTE MES",fontSize=9.sp,color=Color.Gray)}}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Donut(items:List<ItemGasto>){Canvas(Modifier.size(190.dp)){val stroke=size.minDimension*.19f;var start=-90f;items.forEachIndexed{index,item->val sweep=360f*item.porcentaje/100f;drawArc(color=colores[index],startAngle=start,sweepAngle=sweep-3f,useCenter=false,topLeft=Offset(stroke/2,stroke/2),size=androidx.compose.ui.geometry.Size(size.width-stroke,size.height-stroke),style=Stroke(stroke));start+=sweep}}}



