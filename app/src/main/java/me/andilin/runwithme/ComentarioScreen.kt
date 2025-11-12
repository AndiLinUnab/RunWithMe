package me.andilin.runwithme



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
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

    var publicacion by remember { mutableStateOf<Publicacion?>(null) }
    var comentarios by remember { mutableStateOf<List<Comentario>>(emptyList()) }
    var nuevoComentario by remember { mutableStateOf("") }
    var currentUserData by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    var commentsListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Cargar datos de la publicación y comentarios
    LaunchedEffect(publicacionId) {
        try {
            // Cargar publicación
            val publicacionDoc = db.collection("publicaciones").document(publicacionId).get().await()
            publicacion = publicacionDoc.toObject(Publicacion::class.java)

            // Cargar datos del usuario actual
            if (currentUser != null) {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                currentUserData = userDoc.toObject(UserData::class.java)
            }

            // Configurar listener en tiempo real para comentarios
            commentsListener = db.collection("comentarios")
                .whereEqualTo("publicacionId", publicacionId)
                .orderBy("fecha")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener

                    val nuevosComentarios = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Comentario::class.java)?.copy(id = doc.id)
                    } ?: emptyList()

                    comentarios = nuevosComentarios
                }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // Limpiar listener cuando se desmonte el composable
    DisposableEffect(Unit) {
        onDispose {
            commentsListener?.remove()
        }
    }

    // Función para enviar comentario
    fun enviarComentario() {
        if (nuevoComentario.isBlank() || currentUser == null || currentUserData == null) return

        coroutineScope.launch {
            try {
                val comentario = Comentario(
                    publicacionId = publicacionId,
                    userId = currentUser.uid,
                    userName = currentUserData?.nombre ?: "Usuario",
                    userPhoto = currentUserData?.fotoLocal,
                    userEmail = currentUser.email ?: "",
                    texto = nuevoComentario.trim(),
                    fecha = Date()
                )

                db.collection("comentarios").add(comentario).await()
                nuevoComentario = "" // Limpiar el campo de texto

            } catch (e: Exception) {
                e.printStackTrace()
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
            // Input para nuevo comentario
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
                // Información de la publicación
                PublicacionHeader(publicacion!!)

                // Lista de comentarios
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Encabezado (autor + tiempo)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(publicacion.imagenPath ?: ""),
                    contentDescription = null,
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

            // Texto de la publicación
            Text(
                publicacion.texto,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Grupo (si existe)
            if (publicacion.grupo.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
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
            // Foto del usuario
            Image(
                painter = rememberAsyncImagePainter(comentario.userPhoto ?: ""),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(8.dp))

            // Contenido del comentario
            Column(modifier = Modifier.weight(1f)) {
                // Nombre y fecha
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

                // Texto del comentario
                Text(
                    comentario.texto,
                    fontSize = 14.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// Función para formatear la fecha
fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
    return sdf.format(date)
}