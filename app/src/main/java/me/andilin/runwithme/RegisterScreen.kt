package me.andilin.runwithme

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController,
    onClickBack: () -> Unit = {},
    onSuccessfulRegister: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = LocalView.current.context as Activity
    val auth: FirebaseAuth = Firebase.auth
    val db = Firebase.firestore

    // Estados
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var nivel by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    // Animación para el avatar
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    val avatarScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Imagen local
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6),
                                Color(0xFFA855F7)
                            )
                        )
                    )
            ) {
                IconButton(
                    onClick = onClickBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Únete a RunWithMe",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Crea tu perfil de runner",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Formulario con scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Avatar con animación
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.offset(y = (-40).dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(if (imagenUri == null) avatarScale else 1f),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { launcher.launch("image/*") }
                                .border(4.dp, Color(0xFF6366F1), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imagenUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(imagenUri),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.AddAPhoto,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF6366F1)
                                )
                            }
                        }
                    }
                }

                Text(
                    "Toca para agregar foto",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.offset(y = (-30).dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Card del formulario
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Nombre
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = {
                                nombre = it
                                mensajeError = null
                            },
                            label = { Text("Nombre completo") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                focusedLabelColor = Color(0xFF6366F1),
                                cursorColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Correo
                        OutlinedTextField(
                            value = correo,
                            onValueChange = {
                                correo = it
                                mensajeError = null
                            },
                            label = { Text("Correo electrónico") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                focusedLabelColor = Color(0xFF6366F1),
                                cursorColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Contraseña
                        OutlinedTextField(
                            value = contrasena,
                            onValueChange = {
                                contrasena = it
                                mensajeError = null
                            },
                            label = { Text("Contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                focusedLabelColor = Color(0xFF6366F1),
                                cursorColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Confirmar contraseña
                        OutlinedTextField(
                            value = confirmarContrasena,
                            onValueChange = {
                                confirmarContrasena = it
                                mensajeError = null
                            },
                            label = { Text("Confirmar contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                focusedLabelColor = Color(0xFF6366F1),
                                cursorColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            isError = confirmarContrasena.isNotEmpty() && contrasena != confirmarContrasena,
                            supportingText = {
                                if (confirmarContrasena.isNotEmpty() && contrasena != confirmarContrasena) {
                                    Text(
                                        "Las contraseñas no coinciden",
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        )

                        // Fecha de nacimiento
                        OutlinedTextField(
                            value = fechaNacimiento,
                            onValueChange = { fechaNacimiento = it },
                            label = { Text("Fecha de nacimiento") },
                            placeholder = { Text("DD/MM/AAAA") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                focusedLabelColor = Color(0xFF6366F1),
                                cursorColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Selección de género
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Wc,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Género",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Masculino", "Femenino", "Otro").forEach { option ->
                                FilterChip(
                                    selected = genero == option,
                                    onClick = { genero = option },
                                    label = { Text(option, fontSize = 13.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF6366F1),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF3F4F6)
                                    ),
                                    border = if (genero == option) {
                                        FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = true,
                                            borderColor = Color(0xFF6366F1)
                                        )
                                    } else {
                                        FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = false
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nivel de corredor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.DirectionsRun,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Nivel de corredor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Principiante", "Intermedio", "Avanzado").forEach { option ->
                                FilterChip(
                                    selected = nivel == option,
                                    onClick = { nivel = option },
                                    label = { Text(option, fontSize = 13.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF8B5CF6),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF3F4F6)
                                    ),
                                    border = if (nivel == option) {
                                        FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = true,
                                            borderColor = Color(0xFF8B5CF6)
                                        )
                                    } else {
                                        FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = false
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error message
                if (mensajeError != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                mensajeError!!,
                                color = Color(0xFFDC2626),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Botón de registro
                Button(
                    onClick = {
                        if (correo.isNotEmpty() && contrasena == confirmarContrasena && nombre.isNotEmpty()) {
                            cargando = true
                            auth.createUserWithEmailAndPassword(correo, contrasena)
                                .addOnCompleteListener(activity) { task ->
                                    cargando = false
                                    if (task.isSuccessful) {
                                        val uid = auth.currentUser?.uid ?: ""
                                        val userData = mapOf(
                                            "nombre" to nombre,
                                            "correo" to correo,
                                            "fechaNacimiento" to fechaNacimiento,
                                            "genero" to genero,
                                            "nivel" to nivel
                                        )
                                        db.collection("usuarios").document(uid).set(userData)
                                            .addOnSuccessListener {
                                                onSuccessfulRegister()
                                            }
                                            .addOnFailureListener {
                                                mensajeError = "Error al guardar datos: ${it.message}"
                                            }
                                    } else {
                                        mensajeError = "Error en el registro: ${task.exception?.message}"
                                    }
                                }
                        } else {
                            mensajeError = "Completa todos los campos correctamente"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Outlined.HowToReg,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Crear cuenta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ya tienes cuenta
                TextButton(
                    onClick = onClickBack,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF6366F1)
                    )
                ) {
                    Text(
                        "¿Ya tienes cuenta? ",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Inicia sesión",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
