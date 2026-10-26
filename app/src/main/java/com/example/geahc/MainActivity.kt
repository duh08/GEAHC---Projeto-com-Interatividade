package com.example.geahc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// --- CORES DO PROJETO ---
val VermelhoPrincipal = Color(0xFFC62828)
val FundoClaro = Color(0xFFF5F5F5)

// --- MODELOS DE DADOS PARA INTERATIVIDADE ---
data class Evento(val id: Int, val titulo: String, val categoria: String, var vagas: Int, var inscrito: Boolean, val cargaHoraria: Int)
data class Comprovante(val id: Int, val nome: String, var status: String, var horasValidadas: Int = 0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { GEAHCApp() }
        }
    }
}

@Composable
fun GEAHCApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("home") { popUpTo("login") { inclusive = true } }
            })
        }
        composable("home") { MainScreen() }
    }
}

// ==========================================
// TELA DE LOGIN & CADASTRO
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var isCreatingAccount by rememberSaveable { mutableStateOf(false) }
    var nome by rememberSaveable { mutableStateOf("") }
    var cpf by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(containerColor = VermelhoPrincipal) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("GEAHC", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(if (isCreatingAccount) "Crie sua conta acadêmica" else "Gestão de Horas Complementares",
                style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.padding(bottom = 32.dp))

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
            )

            if (isCreatingAccount) {
                OutlinedTextField(
                    value = nome, onValueChange = { nome = it }, label = { Text("Nome Completo") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), singleLine = true, colors = textFieldColors
                )
            }

            OutlinedTextField(
                value = cpf, onValueChange = { cpf = it; errorMessage = null },
                label = { Text("CPF") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), singleLine = true, colors = textFieldColors
            )

            OutlinedTextField(
                value = password, onValueChange = { password = it; errorMessage = null },
                label = { Text("Senha") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                    }
                }
            )

            errorMessage?.let { Text(it, color = Color(0xFFFFCDD2), modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)) }
            successMessage?.let { Text(it, color = Color(0xFFA5D6A7), modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)) }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (isCreatingAccount) {
                        if (nome.isNotBlank() && cpf.isNotBlank() && password.isNotBlank()) {
                            successMessage = "Conta criada com sucesso! Faça login."
                            isCreatingAccount = false
                            password = ""
                        } else errorMessage = "Preencha todos os campos."
                    } else {
                        if (cpf.isNotBlank() && password.isNotBlank()) onLoginSuccess()
                        else errorMessage = "CPF ou senha inválidos."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(if (isCreatingAccount) "Cadastrar" else "Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isCreatingAccount) "Já tem uma conta? Voltar ao Login" else "Não tem conta? Criar agora",
                color = Color.White, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    isCreatingAccount = !isCreatingAccount
                    errorMessage = null
                    successMessage = null
                }.padding(8.dp)
            )
        }
    }
}

// ==========================================
// ESTRUTURA PRINCIPAL E ESTADOS COMPARTILHADOS
// ==========================================
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Bancos de dados simulados em memória para interatividade
    val eventosState = remember { mutableStateListOf(
        Evento(1, "Semana de Tecnologia UNIDEP", "Semana Acadêmica", 50, false, 20),
        Evento(2, "Oficina de Kotlin e Compose", "Oficina", 0, false, 4),
        Evento(3, "Palestra: IA na Saúde", "Projeto de Extensão", 15, false, 2)
    )}

    val comprovantesState = remember { mutableStateListOf(
        Comprovante(1, "Curso Alura - Kotlin", "Em análise", 0),
        Comprovante(2, "Palestra Externa Sebrae", "Em análise", 0),
        Comprovante(3, "Certificado UX Design", "Validado", 10)
    )}

    Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(navController = navController, startDestination = "eventos") {
                composable("eventos") { EventCatalogScreen(eventosState) }
                composable("checkin") { QrCheckInScreen(eventosState) }
                composable("horas") { HoursProgressScreen(comprovantesState, eventosState) }
                composable("coordenacao") { CoordenacaoScreen(comprovantesState) }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem("Eventos", "eventos", Icons.Filled.Event),
        NavigationItem("Check-in", "checkin", Icons.Filled.QrCode),
        NavigationItem("Horas", "horas", Icons.Filled.School),
        NavigationItem("Coordenação", "coordenacao", Icons.Filled.AdminPanelSettings)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) { launchSingleTop = true; restoreState = true } },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = VermelhoPrincipal, selectedTextColor = VermelhoPrincipal)
            )
        }
    }
}
data class NavigationItem(val title: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)


// ==========================================
// TELA 1: CATÁLOGO DE EVENTOS (Interativa)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCatalogScreen(eventos: MutableList<Evento>) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Semana Acadêmica", "Oficina", "Projeto de Extensão")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Eventos", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = VermelhoPrincipal)) },
        containerColor = FundoClaro
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.White, contentColor = VermelhoPrincipal) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Catálogo") })
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Meus Eventos") })
            }

            if (selectedTabIndex == 0) {
                LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter, onClick = { selectedFilter = filter }, label = { Text(filter) },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFCDD2), selectedLabelColor = VermelhoPrincipal)
                        )
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val eventosFiltrados = if (selectedTabIndex == 1) {
                    eventos.filter { it.inscrito }
                } else {
                    eventos.filter { (selectedFilter == "Todos" || it.categoria == selectedFilter) && !it.inscrito }
                }

                items(eventosFiltrados) { evento ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(evento.titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VermelhoPrincipal)
                            Text("${evento.categoria} | Carga: ${evento.cargaHoraria}h | Vagas: ${evento.vagas}", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))

                            if (selectedTabIndex == 1) {
                                Button(onClick = {
                                    val index = eventos.indexOf(evento)
                                    eventos[index] = evento.copy(inscrito = false, vagas = evento.vagas + 1)
                                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancelar Inscrição") }
                            } else {
                                if (evento.vagas > 0) {
                                    Button(onClick = {
                                        val index = eventos.indexOf(evento)
                                        eventos[index] = evento.copy(inscrito = true, vagas = evento.vagas - 1)
                                    }, colors = ButtonDefaults.buttonColors(containerColor = VermelhoPrincipal)) { Text("Inscrever-se") }
                                } else {
                                    Button(onClick = { }, enabled = false) { Text("Vagas Esgotadas") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TELA 2: CHECK-IN QR CODE
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCheckInScreen(eventos: MutableList<Evento>) {
    var checkInSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Check-in", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = VermelhoPrincipal)) },
        containerColor = FundoClaro
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(250.dp).background(if (checkInSuccess) Color(0xFFA5D6A7) else Color.LightGray, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (checkInSuccess) Icons.Filled.CheckCircle else Icons.Filled.CameraAlt,
                    contentDescription = "Câmera", modifier = Modifier.size(80.dp), tint = if (checkInSuccess) Color(0xFF2E7D32) else Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(if (checkInSuccess) "Presença Registrada com Sucesso!" else "Aponte a câmera para o QR Code", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            if (!checkInSuccess) {
                Button(onClick = { checkInSuccess = true }, colors = ButtonDefaults.buttonColors(containerColor = VermelhoPrincipal)) {
                    Text("Simular Leitura de QR Code")
                }
            } else {
                Button(onClick = { checkInSuccess = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                    Text("Ler Novo Código")
                }
            }
        }
    }
}

// ==========================================
// TELA 3: PROGRESSO E UPLOAD (Interativa)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursProgressScreen(comprovantes: MutableList<Comprovante>, eventos: List<Evento>) {
    var showForm by remember { mutableStateOf(false) }
    var novoNome by remember { mutableStateOf("") }

    // Calcula as horas reais com base nos dados validados
    val horasComprovantes = comprovantes.filter { it.status == "Validado" }.sumOf { it.horasValidadas }
    val horasEventos = eventos.filter { it.inscrito }.sumOf { it.cargaHoraria } // Assumindo presença nos inscritos para o MVP
    val totalHoras = horasComprovantes + horasEventos
    val meta = 200
    val progresso = (totalHoras.toFloat() / meta.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Minhas Horas", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = VermelhoPrincipal)) },
        containerColor = FundoClaro
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Progresso Atual", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(progress = { progresso }, modifier = Modifier.fillMaxWidth().height(12.dp), color = VermelhoPrincipal, trackColor = Color(0xFFFFCDD2))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$totalHoras / $meta horas", color = Color.Gray, modifier = Modifier.align(Alignment.End))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                if (showForm) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Enviar Comprovante", fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = novoNome, onValueChange = { novoNome = it }, label = { Text("Nome da Atividade") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                            Button(
                                onClick = {
                                    if (novoNome.isNotBlank()) {
                                        comprovantes.add(Comprovante(comprovantes.size + 1, novoNome, "Em análise", 0))
                                        novoNome = ""
                                        showForm = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = VermelhoPrincipal)
                            ) { Text("Enviar para Análise") }
                        }
                    }
                } else {
                    Button(onClick = { showForm = true }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Novo Comprovante", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item { Text("Histórico de Envios", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp)) }

            items(comprovantes) { doc ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(doc.nome, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        val statusColor = when(doc.status) { "Validado" -> Color(0xFF388E3C); "Recusado" -> VermelhoPrincipal; else -> Color(0xFFFBC02D) }
                        Text(if(doc.status == "Validado") "+${doc.horasValidadas}h" else doc.status, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// TELA 4: COORDENAÇÃO (Interativa)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoordenacaoScreen(comprovantes: MutableList<Comprovante>) {
    var comprovanteSelecionado by remember { mutableStateOf<Comprovante?>(null) }
    var horasAprovadas by remember { mutableStateOf("") }

    val pendentes = comprovantes.filter { it.status == "Em análise" }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Validação Coordenação", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = VermelhoPrincipal)) },
        containerColor = FundoClaro
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            item { Text("Documentos Pendentes (${pendentes.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp)) }

            if (pendentes.isEmpty()) {
                item { Text("Nenhum comprovante pendente de análise no momento.", color = Color.Gray) }
            }

            items(pendentes) { analise ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(analise.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Aluno: Lucas (ADS) | Enviado hoje", color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                        Button(onClick = { comprovanteSelecionado = analise; horasAprovadas = "10" }, colors = ButtonDefaults.buttonColors(containerColor = VermelhoPrincipal)) {
                            Text("Analisar Documento")
                        }
                    }
                }
            }
        }
    }

    if (comprovanteSelecionado != null) {
        AlertDialog(
            onDismissRequest = { comprovanteSelecionado = null },
            title = { Text("Análise: ${comprovanteSelecionado!!.nome}") },
            text = {
                Column {
                    Text("Defina a carga horária válida para este documento:")
                    OutlinedTextField(
                        value = horasAprovadas, onValueChange = { horasAprovadas = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Horas Aprovadas") }, modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val index = comprovantes.indexOf(comprovanteSelecionado)
                    comprovantes[index] = comprovanteSelecionado!!.copy(status = "Validado", horasValidadas = horasAprovadas.toIntOrNull() ?: 0)
                    comprovanteSelecionado = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) { Text("Aprovar") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    val index = comprovantes.indexOf(comprovanteSelecionado)
                    comprovantes[index] = comprovanteSelecionado!!.copy(status = "Recusado")
                    comprovanteSelecionado = null
                }) { Text("Recusar", color = VermelhoPrincipal) }
            }
        )
    }
}