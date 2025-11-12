package me.andilin.runwithme

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import me.andilin.runwithme.model.Comentario
import me.andilin.runwithme.model.Publicacion
import me.andilin.runwithme.model.UserData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComentarioScreen(
    navController: NavHostController,
    publicacionId: String
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    var publicacion by remember { mutableStateOf<Publicacion?>(null) }
    var comentarios by remember { mutableStateOf<List<Comentario>>(emptyList()) }
    var nuevoComentario by remember { mutableStateOf("") }
    var currentUserData by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var userDataLoaded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var commentsListener by remember { mutableStateOf<ListenerRegistration?>(null) }


    LaunchedEffect(publicacionId) {
        try {
            // Cargar publicación
            val publicacionDoc = db.collection("publicaciones").document(publicacionId).get().await()
            publicacion = publicacionDoc.toObject(Publicacion::class.java)

            // Cargar datos del usuario actual desde "usuarios"
            if (currentUser != null) {
                val userDoc = db.collection("usuarios").document(currentUser.uid).get().await()
                if (userDoc.exists()) {
                    currentUserData = userDoc.toObject(UserData::class.java)
                } else {
                    currentUserData = UserData(
                        nombre = currentUser.email?.split("@")?.get(0) ?: "Usuario",
                        correo = currentUser.email ?: ""
                    )
                }
                userDataLoaded = true
            }

            // Configurar listener en tiempo real para comentarios
            commentsListener = db.collection("comentarios")
                .whereEqualTo("publicacionId", publicacionId)
                .orderBy("fecha")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(context, "Error cargando comentarios: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    val nuevosComentarios = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Comentario::class.java)?.copy(id = doc.id)
                    } ?: emptyList()

                    comentarios = nuevosComentarios
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            userDataLoaded = true
        } finally {
            isLoading = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            commentsListener?.remove()
        }
    }

    // Función para enviar comentario
    fun enviarComentario() {
        if (nuevoComentario.isBlank()) {
            Toast.makeText(context, "Escribe un comentario", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUser == null) {
            Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        if (!userDataLoaded) {
            Toast.makeText(context, "Cargando datos del usuario...", Toast.LENGTH_SHORT).show()
            return
        }

        coroutineScope.launch {
            try {
                val userName = currentUserData?.nombre ?: currentUser.email?.split("@")?.get(0) ?: "Usuario"
                val userPhoto = currentUserData?.fotoLocal
                val userEmail = currentUser.email ?: ""

                val comentario = Comentario(
                    publicacionId = publicacionId,
                    userId = currentUser.uid,
                    userName = userName,
                    userPhoto = userPhoto,
                    userEmail = userEmail,
                    texto = nuevoComentario.trim(),
                    fecha = Date()
                )

                db.collection("comentarios").add(comentario)
                    .addOnSuccessListener {
                        nuevoComentario = ""
                        Toast.makeText(context, "Comentario enviado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al enviar: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Comentarios",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF40A2E3)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = nuevoComentario,
                    onValueChange = { nuevoComentario = it },
                    placeholder = { Text("Escribe un comentario...") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = { enviarComentario() },
                    enabled = nuevoComentario.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Enviar comentario",
                        tint = if (nuevoComentario.isNotBlank()) Color(0xFF40A2E3) else Color.Gray
                    )
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (publicacion == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Publicación no encontrada")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                PublicacionHeader(publicacion!!)

                if (comentarios.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay comentarios aún\nSé el primero en comentar!",
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        items(comentarios) { comentario ->
                            ComentarioItem(comentario = comentario)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PublicacionHeader(publicacion: Publicacion) {
    var userPhoto by remember { mutableStateOf<String?>(null) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(publicacion.userId) {
        try {
            val userDoc = db.collection("usuarios").document(publicacion.userId).get().await()
            val userData = userDoc.toObject(UserData::class.java)
            userPhoto = userData?.fotoLocal
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(userPhoto ?: ""),
                    contentDescription = "Foto de perfil de ${publicacion.autor}",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(publicacion.autor, fontWeight = FontWeight.Bold)
                    Text(publicacion.tiempo, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                publicacion.texto,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (!publicacion.imagenPath.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(publicacion.imagenPath),
                    contentDescription = "Imagen de la publicación",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (publicacion.grupo.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "En ${publicacion.grupo}",
                    fontSize = 12.sp,
                    color = Color(0xFF40A2E3),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ComentarioItem(comentario: Comentario) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Row(
            modifier = Modifier.padding(12.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(comentario.userPhoto ?: ""),
                contentDescription = "Foto de perfil de ${comentario.userName}",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        comentario.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        formatDate(comentario.fecha),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    comentario.texto,
                    fontSize = 14.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
    return sdf.format(date)
}