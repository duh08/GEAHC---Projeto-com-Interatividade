package com.example.geahc

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

// --- CORES INSTITUCIONAIS AFYA & MODO ESCURO ---
val VermelhoAfya = Color(0xFFE20074)
val FundoClaro = Color(0xFFF8F9FA)
val FundoEscuro = Color(0xFF121212)
val CardEscuro = Color(0xFF1E1E1E)

// --- MODELOS DE DADOS ---
enum class PerfilUsuario { ALUNO, COORDENACAO }

data class Usuario(
    var nome: String,
    val cpf: String,
    var perfil: PerfilUsuario,
    var curso: String = "Análise e Desenvolvimento de Sistemas",
    var periodo: String = "4º Semestre",
    var telefone: String = "(46) 99999-8888",
    var email: String = "eduardo.brandalise@aluno.unidep.edu.br",
    var endereco: String = "Pato Branco - PR",
    var avatarSimbolo: String = "👤",
    var fotoPerfilNome: String? = null,
    var senhaAtual: String = "123456"
)

data class Evento(
    val id: Int,
    val titulo: String,
    val categoria: String,
    var vagas: Int,
    var inscrito: Boolean,
    val cargaHoraria: Int,
    val palestrante: String,
    val dataHora: String,
    val local: String,
    val descricao: String,
    val inscritosNomes: MutableList<String> = mutableListOf()
)

data class Comprovante(
    val id: Int,
    val nome: String,
    var status: String,
    var horasValidadas: Int = 0,
    var nomeArquivoAnexo: String? = null,
    var alunoNome: String = "Eduardo Leandro Brandalise",
    var avaliadoPor: String? = null
)

data class CertificadoEmitido(
    val id: Int,
    val nomeEvento: String,
    val dataEmissao: String,
    val cargaHoraria: Int,
    val codigoAutenticidade: String
)

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
    var usuarioLogado by remember { mutableStateOf<Usuario?>(null) }
    var isDarkMode by remember { mutableStateOf(false) }

    val backgroundColor = if (isDarkMode) FundoEscuro else FundoClaro
    val textColor = if (isDarkMode) Color.White else Color.Black
    val cardColor = if (isDarkMode) CardEscuro else Color.White

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = { user ->
                usuarioLogado = user
                navController.navigate("home") { popUpTo("login") { inclusive = true } }
            })
        }
        composable("home") {
            usuarioLogado?.let { user ->
                MainScreen(
                    usuario = user,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { isDarkMode = it },
                    backgroundColor = backgroundColor,
                    textColor = textColor,
                    cardColor = cardColor,
                    onLogout = {
                        usuarioLogado = null
                        navController.navigate("login") { popUpTo("home") { inclusive = true } }
                    }
                )
            }
        }
    }
}

// ==========================================
// TELA DE LOGIN & CADASTRO
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (Usuario) -> Unit) {
    var isCreatingAccount by rememberSaveable { mutableStateOf(false) }
    var nome by rememberSaveable { mutableStateOf("") }
    var cpf by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var perfilSelecionado by remember { mutableStateOf(PerfilUsuario.ALUNO) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(containerColor = VermelhoAfya) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("GEAHC", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                text = if (isCreatingAccount) "Crie sua conta no GEAHC" else "Gestão de Horas - Afya UNIDEP",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = perfilSelecionado == PerfilUsuario.ALUNO,
                    onClick = { perfilSelecionado = PerfilUsuario.ALUNO },
                    label = { Text("Aluno") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.White,
                        selectedLabelColor = VermelhoAfya,
                        containerColor = Color.Transparent,
                        labelColor = Color.White
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = perfilSelecionado == PerfilUsuario.COORDENACAO,
                    onClick = { perfilSelecionado = PerfilUsuario.COORDENACAO },
                    label = { Text("Coordenação") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.White,
                        selectedLabelColor = VermelhoAfya,
                        containerColor = Color.Transparent,
                        labelColor = Color.White
                    )
                )
            }

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
                            successMessage = "Conta de ${perfilSelecionado.name.lowercase()} criada com sucesso!"
                            isCreatingAccount = false
                            password = ""
                        } else errorMessage = "Preencha todos os campos."
                    } else {
                        if (cpf.isNotBlank() && password.isNotBlank()) {
                            val nomeFinal = if (perfilSelecionado == PerfilUsuario.COORDENACAO) {
                                if (nome.isNotBlank()) nome else "Prof. Darlan Dalsasso"
                            } else {
                                if (nome.isNotBlank()) nome else "Eduardo Leandro Brandalise"
                            }
                            onLoginSuccess(Usuario(nomeFinal, cpf, perfilSelecionado, senhaAtual = password))
                        } else errorMessage = "CPF ou senha inválidos."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(if (isCreatingAccount) "Cadastrar" else "Entrar como ${if (perfilSelecionado == PerfilUsuario.ALUNO) "Aluno" else "Coordenação"}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
// ESTRUTURA PRINCIPAL
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    usuario: Usuario,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    backgroundColor: Color,
    textColor: Color,
    cardColor: Color,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    var showProfileDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCertificadosDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLgpdDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    val certificadosState = remember { mutableStateListOf(
        CertificadoEmitido(1, "Semana de Tecnologia Afya", "15/10/2026", 20, "CERT-AFYA-8849"),
        CertificadoEmitido(2, "Oficina de Kotlin e Compose", "02/11/2026", 4, "CERT-AFYA-1092")
    )}

    val eventosState = remember { mutableStateListOf(
        Evento(1, "Semana de Tecnologia Afya", "Semana Acadêmica", 50, false, 20, "Prof. Cezar Augusto Mezzalira", "20/11/2026 - 19:00", "Auditório Principal UNIDEP", "Evento anual sobre arquitetura de software, Kotlin Compose e IA aplicada.", mutableListOf("Ana Souza", "Carlos Silva")),
        Evento(2, "Oficina de Kotlin e Mobile", "Oficina", 25, false, 4, "Prof. João G. Brasil", "22/11/2026 - 14:00", "Laboratório 04 - Bloco B", "Oficina prática de desenvolvimento Android com Jetpack Compose.", mutableListOf("Lucas Mendes")),
        Evento(3, "Palestra: IA e Saúde Digital", "Projeto de Extensão", 15, false, 2, "Prof. Darlan Dalsasso", "25/11/2026 - 10:00", "Sala 02", "Discussão sobre avanços de modelos generativos na saúde.", mutableListOf("Beatriz Santos", "Gabriel Lima"))
    )}

    val comprovantesState = remember { mutableStateListOf(
        Comprovante(1, "Curso Alura - Kotlin", "Em análise", 0, "certificado_kotlin.pdf", "Eduardo Leandro Brandalise"),
        Comprovante(2, "Palestra Externa Sebrae", "Em análise", 0, "comprovante_sebrae.jpg", "Ana Souza"),
        Comprovante(3, "Certificado UX Design", "Validado", 10, "certificado_ux.pdf", "Lucas Mendes", "Prof. Darlan Dalsasso")
    )}

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("GEAHC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .clickable { showProfileDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (usuario.fotoPerfilNome != null) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Foto", tint = VermelhoAfya, modifier = Modifier.size(36.dp))
                        } else {
                            Text(usuario.avatarSimbolo, fontSize = 22.sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(Icons.Filled.Person, contentDescription = "Perfil", tint = Color.White)
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Configurações", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = VermelhoAfya)
            )
        },
        bottomBar = { BottomNavigationBar(navController, usuario.perfil) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).background(backgroundColor)) {
            NavHost(navController = navController, startDestination = "eventos") {
                composable("eventos") { EventCatalogScreen(eventosState, usuario, cardColor, textColor) }
                composable("checkin") { QrCheckInScreen(textColor, eventosState, certificadosState) }
                composable("horas") { HoursProgressScreen(comprovantesState, eventosState, usuario, cardColor, textColor) }
                if (usuario.perfil == PerfilUsuario.COORDENACAO) {
                    composable("coordenacao") { CoordenacaoScreen(comprovantesState, usuario, cardColor, textColor) }
                }
            }
        }
    }

    // Modal de Resumo do Perfil (Botão 'Alternar Perfil' Removido)
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Meu Perfil", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Tipo de Acesso: ${usuario.perfil.name}", fontWeight = FontWeight.Bold, color = VermelhoAfya, modifier = Modifier.padding(bottom = 8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(usuario.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("CPF: ${usuario.cpf}", fontSize = 12.sp, color = Color.Gray)
                            Text("Curso: ${usuario.curso}", fontSize = 12.sp)
                            Text("Período: ${usuario.periodo}", fontSize = 12.sp)
                            Text("E-mail: ${usuario.email}", fontSize = 12.sp)
                            Text("Telefone: ${usuario.telefone}", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar Perfil")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showCertificadosDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)
                    ) {
                        Icon(Icons.Filled.WorkspacePremium, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Meus Certificados (${certificadosState.size})")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = VermelhoAfya)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Alterar Senha da Conta", color = VermelhoAfya)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showProfileDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                    Text("Fechar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showProfileDialog = false
                    onLogout()
                }) {
                    Icon(Icons.Filled.Logout, contentDescription = null, tint = VermelhoAfya)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sair da Conta", color = VermelhoAfya)
                }
            }
        )
    }

    // Modal Editar Perfil
    if (showEditProfileDialog) {
        var editaNome by remember { mutableStateOf(usuario.nome) }
        var editaCurso by remember { mutableStateOf(usuario.curso) }
        var editaPeriodo by remember { mutableStateOf(usuario.periodo) }
        var editaTelefone by remember { mutableStateOf(usuario.telefone) }
        var editaEmail by remember { mutableStateOf(usuario.email) }
        var editaEndereco by remember { mutableStateOf(usuario.endereco) }
        var avatarSelecionado by remember { mutableStateOf(usuario.avatarSimbolo) }
        var fotoCarregadaNome by remember { mutableStateOf(usuario.fotoPerfilNome) }

        val avataresDisponiveis = listOf("👤", "🎓", "💻", "👨‍💻", "👩‍💻", "🚀")

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Editar Informações do Perfil", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Foto de Perfil / Avatar:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        avataresDisponiveis.forEach { av ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (avatarSelecionado == av && fotoCarregadaNome == null) Color(0xFFF8BBD0) else Color.Transparent, CircleShape)
                                    .clickable { avatarSelecionado = av; fotoCarregadaNome = null },
                                contentAlignment = Alignment.Center
                            ) { Text(av, fontSize = 20.sp) }
                        }
                    }

                    OutlinedButton(
                        onClick = { fotoCarregadaNome = "foto_perfil_${usuario.cpf}.png" },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = VermelhoAfya)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(fotoCarregadaNome ?: "Upload Imagem (.PNG/.JPG)", color = VermelhoAfya, fontSize = 12.sp)
                    }

                    OutlinedTextField(value = editaNome, onValueChange = { editaNome = it }, label = { Text("Nome Completo") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = editaCurso, onValueChange = { editaCurso = it }, label = { Text("Curso") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = editaPeriodo, onValueChange = { editaPeriodo = it }, label = { Text("Período/Módulo") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = editaTelefone, onValueChange = { editaTelefone = it }, label = { Text("Telefone") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = editaEmail, onValueChange = { editaEmail = it }, label = { Text("Gmail / E-mail") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = editaEndereco, onValueChange = { editaEndereco = it }, label = { Text("Endereço") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        usuario.nome = editaNome
                        usuario.curso = editaCurso
                        usuario.periodo = editaPeriodo
                        usuario.telefone = editaTelefone
                        usuario.email = editaEmail
                        usuario.endereco = editaEndereco
                        usuario.avatarSimbolo = avatarSelecionado
                        usuario.fotoPerfilNome = fotoCarregadaNome

                        comprovantesState.forEach { if (it.alunoNome == usuario.nome) it.alunoNome = editaNome }
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)
                ) { Text("Salvar Alterações") }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Modal Certificados
    if (showCertificadosDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showCertificadosDialog = false },
            title = { Text("Meus Certificados Emitidos", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (certificadosState.isEmpty()) {
                        Text("Nenhum certificado emitido até o momento.", color = Color.Gray)
                    } else {
                        certificadosState.forEach { cert ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(cert.nomeEvento, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    Text("Data: ${cert.dataEmissao} | Carga: ${cert.cargaHoraria}h", fontSize = 12.sp)
                                    Text("Código: ${cert.codigoAutenticidade}", fontSize = 11.sp, color = Color.Gray)
                                    Row(modifier = Modifier.padding(top = 8.dp)) {
                                        Button(
                                            onClick = { Toast.makeText(context, "Baixando certificado em PDF...", Toast.LENGTH_SHORT).show() },
                                            modifier = Modifier.height(32.dp).padding(end = 4.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                                        ) { Text("Baixar PDF", fontSize = 10.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCertificadosDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                    Text("Fechar")
                }
            }
        )
    }

    // Modal Alterar Senha
    if (showChangePasswordDialog) {
        var senhaAtualInput by remember { mutableStateOf("") }
        var novaSenhaInput by remember { mutableStateOf("") }
        var confirmaNovaSenhaInput by remember { mutableStateOf("") }
        var erroSenhaMessage by remember { mutableStateOf<String?>(null) }
        var sucessoSenhaMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Alterar Senha", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = senhaAtualInput, onValueChange = { senhaAtualInput = it; erroSenhaMessage = null },
                        label = { Text("Senha Atual") }, visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = novaSenhaInput, onValueChange = { novaSenhaInput = it; erroSenhaMessage = null },
                        label = { Text("Escrever Nova Senha") }, visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = confirmaNovaSenhaInput, onValueChange = { confirmaNovaSenhaInput = it; erroSenhaMessage = null },
                        label = { Text("Repetir Nova Senha") }, visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    erroSenhaMessage?.let { Text(it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)) }
                    sucessoSenhaMessage?.let { Text(it, color = Color(0xFF388E3C), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (senhaAtualInput != usuario.senhaAtual) {
                            erroSenhaMessage = "Senha atual incorreta!"
                        } else if (novaSenhaInput.isBlank() || novaSenhaInput.length < 4) {
                            erroSenhaMessage = "A nova senha deve ter no mínimo 4 caracteres."
                        } else if (novaSenhaInput != confirmaNovaSenhaInput) {
                            erroSenhaMessage = "As senhas novas não coincidem!"
                        } else {
                            usuario.senhaAtual = novaSenhaInput
                            sucessoSenhaMessage = "Senha alterada com sucesso!"
                            erroSenhaMessage = null
                            showChangePasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)
                ) { Text("Confirmar Troca") }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Modal de Configurações (Com Opções Alinhadas)
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Configurações do App", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Modo Escuro (Dark Mode)", fontWeight = FontWeight.Medium)
                        Switch(checked = isDarkMode, onCheckedChange = { onDarkModeChange(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    TextButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(Icons.Filled.Help, contentDescription = null, tint = VermelhoAfya)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Ajuda & Dicas do App", color = VermelhoAfya, fontWeight = FontWeight.Medium)
                        }
                    }

                    TextButton(
                        onClick = { showSupportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = VermelhoAfya)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Suporte & Contato", color = VermelhoAfya, fontWeight = FontWeight.Medium)
                        }
                    }

                    TextButton(
                        onClick = { showLgpdDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(Icons.Filled.Gavel, contentDescription = null, tint = VermelhoAfya)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Termos de Uso e Privacidade (LGPD)", color = VermelhoAfya, fontWeight = FontWeight.Medium)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Versão do Aplicativo: 1.7.0 (Build Afya)", fontSize = 12.sp, color = Color.Gray)
                    Text("Instituição: Centro Universitário Pato Branco - UNIDEP", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { showSettingsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                    Text("Fechar")
                }
            }
        )
    }

    // Modal Ajuda
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Ajuda & Dicas", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("📌 Como fazer Check-in?", fontWeight = FontWeight.Bold)
                    Text("Vá na aba 'Check-in' e aponte a câmera para o QR Code projetado no evento para registrar sua presença e gerar o certificado.", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📌 Onde ficam meus certificados?", fontWeight = FontWeight.Bold)
                    Text("Certificados gerados por QR Code ficam salvos no seu Perfil na opção 'Meus Certificados'.", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📌 Como enviar atividades externas?", fontWeight = FontWeight.Bold)
                    Text("Na aba 'Horas', clique em 'Novo Comprovante', insira o nome, anexe a foto/documento e envie para a coordenação avaliar.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)) { Text("Entendi") }
            }
        )
    }

    // Modal Suporte
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Suporte Acadêmico", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Caso tenha dúvidas ou problemas com suas horas complementares, entre em contato:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📧 E-mail: suporte.geahc@unidep.edu.br", fontWeight = FontWeight.SemiBold)
                    Text("📱 WhatsApp Coordenação: (46) 98888-7777", fontWeight = FontWeight.SemiBold)
                    Text("📍 Atendimento Presencial: Bloco B - Coordenação de ADS", fontSize = 13.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { showSupportDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Fechar") }
            }
        )
    }

    // Modal LGPD
    if (showLgpdDialog) {
        AlertDialog(
            onDismissRequest = { showLgpdDialog = false },
            title = { Text("Termos de Uso e Privacidade (LGPD)", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Conformidade com a Lei nº 13.709/2018 (Lei Geral de Proteção de Dados Pessoais - LGPD):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "1. Coleta de Dados: O aplicativo GEAHC coleta dados como Nome, CPF, E-mail, Curso e Certificados para fins estritamente acadêmicos.\n\n" +
                                "2. Finalidade: Seus dados são utilizados exclusivamente para registrar presença em eventos e calcular o progresso da carga horária.\n\n" +
                                "3. Segurança: Informações armazenadas nos servidores acadêmicos da instituição Afya UNIDEP.\n\n" +
                                "4. Direitos: Solicitações de alteração ou exclusão podem ser feitas via coordenação.",
                        fontSize = 13.sp, color = Color.DarkGray
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showLgpdDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)) {
                    Text("Concordar e Fechar")
                }
            }
        )
    }
}

// ==========================================
// BARRA DE NAVEGAÇÃO CONDICIONAL
// ==========================================
@Composable
fun BottomNavigationBar(navController: NavHostController, perfil: PerfilUsuario) {
    val items = mutableListOf(
        NavigationItem("Eventos", "eventos", Icons.Filled.Event),
        NavigationItem("Check-in", "checkin", Icons.Filled.QrCode),
        NavigationItem("Horas", "horas", Icons.Filled.School)
    )

    if (perfil == PerfilUsuario.COORDENACAO) {
        items.add(NavigationItem("Coordenação", "coordenacao", Icons.Filled.AdminPanelSettings))
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) { launchSingleTop = true; restoreState = true } },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = VermelhoAfya, selectedTextColor = VermelhoAfya)
            )
        }
    }
}
data class NavigationItem(val title: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

// ==========================================
// TELA 1: EVENTOS
// ==========================================
@Composable
fun EventCatalogScreen(eventos: MutableList<Evento>, usuario: Usuario, cardColor: Color, textColor: Color) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var eventoSelecionadoParaDetalhes by remember { mutableStateOf<Evento?>(null) }
    var eventoParaConfirmarInscricao by remember { mutableStateOf<Evento?>(null) }
    var eventoParaConfirmarCancelamento by remember { mutableStateOf<Evento?>(null) }
    var eventoParaVerInscritos by remember { mutableStateOf<Evento?>(null) }
    var showCreateEventDialog by remember { mutableStateOf(false) }

    val filters = listOf("Todos", "Semana Acadêmica", "Oficina", "Projeto de Extensão")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Catálogo de Eventos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
            if (usuario.perfil == PerfilUsuario.COORDENACAO) {
                Button(onClick = { showCreateEventDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Criar Evento")
                }
            }
        }

        TabRow(selectedTabIndex = selectedTabIndex, containerColor = cardColor, contentColor = VermelhoAfya) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Disponíveis", color = textColor) })
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Meus Eventos", color = textColor) })
        }

        if (selectedTabIndex == 0) {
            LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter, onClick = { selectedFilter = filter }, label = { Text(filter) },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF8BBD0), selectedLabelColor = VermelhoAfya)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            val eventosFiltrados = if (selectedTabIndex == 1) {
                eventos.filter { it.inscrito }
            } else {
                eventos.filter { (selectedFilter == "Todos" || it.categoria == selectedFilter) && !it.inscrito }
            }

            items(eventosFiltrados) { evento ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { eventoSelecionadoParaDetalhes = evento },
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(evento.titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VermelhoAfya, modifier = Modifier.weight(1f))
                            Badge(containerColor = if (evento.vagas > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)) {
                                Text(
                                    if (evento.vagas > 0) "🟢 ${evento.vagas} vagas" else "🔴 Esgotado",
                                    color = if (evento.vagas > 0) Color(0xFF2E7D32) else Color.Red,
                                    modifier = Modifier.padding(4.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text("${evento.categoria} | Carga: ${evento.cargaHoraria}h", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
                        Text("Clique para ver mais detalhes...", fontSize = 12.sp, color = textColor.copy(alpha = 0.7f))

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (selectedTabIndex == 1) {
                                Button(
                                    onClick = { eventoParaConfirmarCancelamento = evento },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                ) { Text("Cancelar Inscrição") }
                            } else {
                                if (evento.vagas > 0) {
                                    Button(
                                        onClick = { eventoParaConfirmarInscricao = evento },
                                        colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)
                                    ) { Text("Inscrever-se") }
                                } else {
                                    Button(onClick = { }, enabled = false) { Text("Vagas Esgotadas") }
                                }
                            }

                            if (usuario.perfil == PerfilUsuario.COORDENACAO) {
                                OutlinedButton(
                                    onClick = { eventoParaVerInscritos = evento }
                                ) {
                                    Icon(Icons.Filled.Group, contentDescription = null, tint = VermelhoAfya)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Inscritos (${evento.inscritosNomes.size})", color = VermelhoAfya, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Detalhes
    eventoSelecionadoParaDetalhes?.let { ev ->
        AlertDialog(
            onDismissRequest = { eventoSelecionadoParaDetalhes = null },
            title = { Text(ev.titulo, color = VermelhoAfya, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Palestrante: ${ev.palestrante}", fontWeight = FontWeight.SemiBold)
                    Text("Data/Horário: ${ev.dataHora}")
                    Text("Local: ${ev.local}")
                    Text("Carga Horária: ${ev.cargaHoraria} horas")
                    Text("Vagas Restantes: ${ev.vagas}", color = VermelhoAfya, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Descrição:", fontWeight = FontWeight.Bold)
                    Text(ev.descricao, color = Color.DarkGray, fontSize = 14.sp)
                }
            },
            confirmButton = {
                if (!ev.inscrito && ev.vagas > 0) {
                    Button(onClick = {
                        eventoParaConfirmarInscricao = ev
                        eventoSelecionadoParaDetalhes = null
                    }, colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)) { Text("Garantir Vaga") }
                }
            },
            dismissButton = { TextButton(onClick = { eventoSelecionadoParaDetalhes = null }) { Text("Fechar") } }
        )
    }

    // Confirmações
    eventoParaConfirmarInscricao?.let { ev ->
        AlertDialog(
            onDismissRequest = { eventoParaConfirmarInscricao = null },
            title = { Text("Confirmar Inscrição") },
            text = { Text("Deseja confirmar a sua inscrição no evento '${ev.titulo}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        val index = eventos.indexOf(ev)
                        eventos[index] = ev.copy(inscrito = true, vagas = ev.vagas - 1)
                        if (!ev.inscritosNomes.contains(usuario.nome)) {
                            ev.inscritosNomes.add(usuario.nome)
                        }
                        eventoParaConfirmarInscricao = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)
                ) { Text("Sim, Inscrever") }
            },
            dismissButton = { TextButton(onClick = { eventoParaConfirmarInscricao = null }) { Text("Cancelar") } }
        )
    }

    eventoParaConfirmarCancelamento?.let { ev ->
        AlertDialog(
            onDismissRequest = { eventoParaConfirmarCancelamento = null },
            title = { Text("Cancelar Inscrição") },
            text = { Text("Tem certeza de que deseja cancelar a inscrição no evento '${ev.titulo}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        val index = eventos.indexOf(ev)
                        eventos[index] = ev.copy(inscrito = false, vagas = ev.vagas + 1)
                        ev.inscritosNomes.remove(usuario.nome)
                        eventoParaConfirmarCancelamento = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Confirmar Cancelamento") }
            },
            dismissButton = { TextButton(onClick = { eventoParaConfirmarCancelamento = null }) { Text("Voltar") } }
        )
    }

    // Lista de Alunos Inscritos
    eventoParaVerInscritos?.let { ev ->
        AlertDialog(
            onDismissRequest = { eventoParaVerInscritos = null },
            title = { Text("Alunos Inscritos: ${ev.titulo}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (ev.inscritosNomes.isEmpty()) {
                        Text("Nenhum aluno inscrito até o momento.", color = Color.Gray)
                    } else {
                        ev.inscritosNomes.forEachIndexed { i, aluno ->
                            Text("${i + 1}. $aluno", fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { eventoParaVerInscritos = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Fechar") }
            }
        )
    }

    // Modal Criar Evento
    if (showCreateEventDialog) {
        var novoTitulo by remember { mutableStateOf("") }
        var novaCategoria by remember { mutableStateOf("Semana Acadêmica") }
        var novoPalestrante by remember { mutableStateOf("Prof. João G. Brasil") }
        var novaData by remember { mutableStateOf("") }
        var novoLocal by remember { mutableStateOf("") }
        var novaCarga by remember { mutableStateOf("4") }
        var novasVagas by remember { mutableStateOf("30") }
        var novaDescricao by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateEventDialog = false },
            title = { Text("Cadastrar Novo Evento (Coordenação)") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = novoTitulo, onValueChange = { novoTitulo = it }, label = { Text("Título do Evento") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = novaCategoria, onValueChange = { novaCategoria = it }, label = { Text("Categoria (ex: Oficina, Semana Acadêmica)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = novoPalestrante, onValueChange = { novoPalestrante = it }, label = { Text("Palestrante/Ministrante") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = novaData, onValueChange = { novaData = it }, label = { Text("Data e Horário") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = novoLocal, onValueChange = { novoLocal = it }, label = { Text("Local / Laboratório") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = novaCarga, onValueChange = { novaCarga = it }, label = { Text("Carga Horária (horas)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = novasVagas, onValueChange = { novasVagas = it }, label = { Text("Quantidade de Vagas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    OutlinedTextField(value = novaDescricao, onValueChange = { novaDescricao = it }, label = { Text("Descrição Completa") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (novoTitulo.isNotBlank()) {
                            eventos.add(
                                Evento(
                                    id = eventos.size + 1,
                                    titulo = novoTitulo,
                                    categoria = novaCategoria,
                                    vagas = novasVagas.toIntOrNull() ?: 20,
                                    inscrito = false,
                                    cargaHoraria = novaCarga.toIntOrNull() ?: 2,
                                    palestrante = novoPalestrante,
                                    dataHora = novaData,
                                    local = novoLocal,
                                    descricao = novaDescricao
                                )
                            )
                            showCreateEventDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)
                ) { Text("Publicar Evento") }
            },
            dismissButton = { TextButton(onClick = { showCreateEventDialog = false }) { Text("Cancelar") } }
        )
    }
}

// ==========================================
// TELA 2: CHECK-IN QR CODE
// ==========================================
@Composable
fun QrCheckInScreen(
    textColor: Color,
    eventos: List<Evento>,
    certificados: MutableList<CertificadoEmitido>
) {
    var checkInSuccess by remember { mutableStateOf(false) }
    var ultimoCertificadoGerado by remember { mutableStateOf<CertificadoEmitido?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
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
        Text(if (checkInSuccess) "Presença Confirmada!" else "Aponte a câmera para o QR Code do Evento", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge, color = textColor)
        Spacer(modifier = Modifier.height(16.dp))

        if (!checkInSuccess) {
            Button(
                onClick = {
                    checkInSuccess = true
                    val eventoTarget = eventos.firstOrNull { it.inscrito } ?: eventos.first()
                    val novoCertificado = CertificadoEmitido(
                        id = certificados.size + 1,
                        nomeEvento = eventoTarget.titulo,
                        dataEmissao = "10/11/2026",
                        cargaHoraria = eventoTarget.cargaHoraria,
                        codigoAutenticidade = "CERT-AFYA-${(1000..9999).random()}"
                    )
                    certificados.add(novoCertificado)
                    ultimoCertificadoGerado = novoCertificado
                },
                colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)
            ) {
                Text("Simular Leitura de QR Code")
            }
        } else {
            ultimoCertificadoGerado?.let { cert ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏆 Certificado Emitido!", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text(cert.nomeEvento, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
                        Text("Data: ${cert.dataEmissao} | Carga Horária: ${cert.cargaHoraria}h", fontSize = 12.sp)
                        Text("Salvo em: Perfil > Meus Certificados", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { checkInSuccess = false; ultimoCertificadoGerado = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                Text("Ler Novo Código")
            }
        }
    }
}

// ==========================================
// TELA 3: MINHAS HORAS & ENVIO DE COMPROVANTES
// ==========================================
@Composable
fun HoursProgressScreen(comprovantes: MutableList<Comprovante>, eventos: List<Evento>, usuario: Usuario, cardColor: Color, textColor: Color) {
    var showForm by remember { mutableStateOf(false) }
    var novoNome by remember { mutableStateOf("") }
    var arquivoAnexoNome by remember { mutableStateOf<String?>(null) }

    val horasComprovantes = comprovantes.filter { it.status == "Validado" }.sumOf { it.horasValidadas }
    val horasEventos = eventos.filter { it.inscrito }.sumOf { it.cargaHoraria }
    val totalHoras = horasComprovantes + horasEventos
    val meta = 200
    val progresso = (totalHoras.toFloat() / meta.toFloat()).coerceIn(0f, 1f)

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Progresso de Horas Complementares", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(progress = { progresso }, modifier = Modifier.fillMaxWidth().height(12.dp), color = VermelhoAfya, trackColor = Color(0xFFF8BBD0))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$totalHoras / $meta horas validadas", color = Color.Gray, modifier = Modifier.align(Alignment.End))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            if (showForm) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Enviar Novo Comprovante", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                        OutlinedTextField(
                            value = novoNome, onValueChange = { novoNome = it },
                            label = { Text("Nome da Atividade / Curso") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { arquivoAnexoNome = "certificado_anexado_${System.currentTimeMillis() % 1000}.pdf" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.AttachFile, contentDescription = null, tint = VermelhoAfya)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(arquivoAnexoNome ?: "Anexar Certificado/Foto (PDF, JPG, PNG)", color = VermelhoAfya)
                        }

                        Button(
                            onClick = {
                                if (novoNome.isNotBlank()) {
                                    comprovantes.add(Comprovante(comprovantes.size + 1, novoNome, "Em análise", 0, arquivoAnexoNome ?: "sem_anexo.pdf", usuario.nome))
                                    novoNome = ""
                                    arquivoAnexoNome = null
                                    showForm = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)
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

        item { Text("Histórico de Envio de Certificados", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor, modifier = Modifier.padding(bottom = 8.dp)) }

        items(comprovantes) { doc ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(doc.nome, fontWeight = FontWeight.Bold, color = textColor)
                        Text("Aluno: ${doc.alunoNome}", fontSize = 12.sp, color = VermelhoAfya, fontWeight = FontWeight.SemiBold)
                        doc.nomeArquivoAnexo?.let { Text("Arquivo: $it", fontSize = 12.sp, color = Color.Gray) }

                        doc.avaliadoPor?.let { avaliador ->
                            Text(
                                text = "${if (doc.status == "Validado") "Aprovado" else "Recusado"} por: $avaliador",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    val statusColor = when(doc.status) { "Validado" -> Color(0xFF388E3C); "Recusado" -> VermelhoAfya; else -> Color(0xFFFBC02D) }
                    Text(if(doc.status == "Validado") "+${doc.horasValidadas}h" else doc.status, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// TELA 4: COORDENAÇÃO
// ==========================================
@Composable
fun CoordenacaoScreen(
    comprovantes: MutableList<Comprovante>,
    usuarioLogado: Usuario,
    cardColor: Color,
    textColor: Color
) {
    val context = LocalContext.current
    var comprovanteSelecionado by remember { mutableStateOf<Comprovante?>(null) }
    var comprovanteParaVisualizar by remember { mutableStateOf<Comprovante?>(null) }
    var horasAprovadas by remember { mutableStateOf("10") }

    val pendentes = comprovantes.filter { it.status == "Em análise" }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Painel da Coordenação - Análise (${pendentes.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor, modifier = Modifier.padding(bottom = 16.dp)) }

        if (pendentes.isEmpty()) {
            item { Text("Nenhum comprovante pendente de análise no momento.", color = Color.Gray) }
        }

        items(pendentes) { analise ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(analise.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text("Aluno Solicitante: ${analise.alunoNome} | Curso: ADS (UNIDEP)", color = VermelhoAfya, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Anexo: ${analise.nomeArquivoAnexo ?: "Nenhum"}", color = Color.Gray, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = { comprovanteParaVisualizar = analise }) {
                            Icon(Icons.Filled.Visibility, contentDescription = null, tint = VermelhoAfya)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Visualizar Anexo", color = VermelhoAfya, fontSize = 12.sp)
                        }
                        Button(onClick = { comprovanteSelecionado = analise }, colors = ButtonDefaults.buttonColors(containerColor = VermelhoAfya)) {
                            Text("Avaliar Documento")
                        }
                    }
                }
            }
        }
    }

    // Modal Visualizar Anexo
    comprovanteParaVisualizar?.let { doc ->
        AlertDialog(
            onDismissRequest = { comprovanteParaVisualizar = null },
            title = { Text("Visualizador de Anexo", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp).background(Color.LightGray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.DarkGray)
                            Text(doc.nomeArquivoAnexo ?: "documento.pdf", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Aluno Solicitante: ${doc.alunoNome}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "Baixando ${doc.nomeArquivoAnexo}...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Baixar", fontSize = 11.sp, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "Compartilhando arquivo...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Enviar", fontSize = 11.sp, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "Enviando para impressora...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Imprimir", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { comprovanteParaVisualizar = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Fechar") }
            }
        )
    }

    // Modal de Avaliação
    if (comprovanteSelecionado != null) {
        AlertDialog(
            onDismissRequest = { comprovanteSelecionado = null },
            title = { Text("Validar: ${comprovanteSelecionado!!.nome}") },
            text = {
                Column {
                    Text("Aluno Solicitante: ${comprovanteSelecionado!!.alunoNome}", fontWeight = FontWeight.Bold)
                    Text("Documento anexo: ${comprovanteSelecionado!!.nomeArquivoAnexo}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Informe a quantidade de horas aprovadas:")
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
                    comprovantes[index] = comprovanteSelecionado!!.copy(
                        status = "Validado",
                        horasValidadas = horasAprovadas.toIntOrNull() ?: 0,
                        avaliadoPor = usuarioLogado.nome
                    )
                    comprovanteSelecionado = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) { Text("Aprovar") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    val index = comprovantes.indexOf(comprovanteSelecionado)
                    comprovantes[index] = comprovanteSelecionado!!.copy(
                        status = "Recusado",
                        avaliadoPor = usuarioLogado.nome
                    )
                    comprovanteSelecionado = null
                }) { Text("Recusar", color = VermelhoAfya) }
            }
        )
    }
}