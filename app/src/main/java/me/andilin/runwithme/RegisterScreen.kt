package me.andilin.runwithme

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onClickBack: () -> Unit = {},
    onSuccessfulRegister: () -> Unit = {}
) {
    val auth = Firebase.auth
    val activity = LocalView.current.context as Activity

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    var inputName by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var inputPasswordConfirmation by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var registerError by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de perfil
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Agregar foto",
                        tint = Color.Gray,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputName,
                onValueChange = { inputName = it },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { if (nameError.isNotEmpty()) Text(nameError, color = Color.Red) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputEmail,
                onValueChange = { inputEmail = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                supportingText = { if (emailError.isNotEmpty()) Text(emailError, color = Color.Red) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputPassword,
                onValueChange = { inputPassword = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                supportingText = { if (passwordError.isNotEmpty()) Text(passwordError, color = Color.Red) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputPasswordConfirmation,
                onValueChange = { inputPasswordConfirmation = it },
                label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        inputName.isEmpty() -> nameError = "Campo requerido"
                        inputEmail.isEmpty() -> emailError = "Campo requerido"
                        inputPassword.length < 6 -> passwordError = "Mínimo 6 caracteres"
                        inputPassword != inputPasswordConfirmation -> passwordError = "Las contraseñas no coinciden"
                        else -> {
                            auth.createUserWithEmailAndPassword(inputEmail, inputPassword)
                                .addOnSuccessListener { onSuccessfulRegister() }
                                .addOnFailureListener { e ->
                                    registerError = when (e) {
                                        is FirebaseAuthUserCollisionException -> "El correo ya está registrado"
                                        is FirebaseAuthInvalidCredentialsException -> "Correo inválido"
                                        else -> "Error: ${e.message}"
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Registrarse")
            }

            if (registerError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(registerError, color = Color.Red, fontSize = 14.sp)
            }
        }
    }
}