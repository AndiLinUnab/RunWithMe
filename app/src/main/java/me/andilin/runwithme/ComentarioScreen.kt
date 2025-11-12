package me.andilin.runwithme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header con gradiente
            Surface(shadowElevation = 4.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF8B5CF6)
                                )
                            )
                        )
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
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
                            Icons.Outlined.ModeComment,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Comentarios",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (comentarios.isNotEmpty()) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        "${comentarios.size}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(0xFF6366F1),
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Cargando comentarios...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (publicacion == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Publicación no encontrada",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "No hay comentarios aún",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Sé el primero en comentar",
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item { Spacer(Modifier.height(8.dp)) }
                            items(comentarios) { comentario ->
                                ComentarioItem(comentario = comentario)
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }

                // Input para nuevo comentario (fijo en la parte inferior)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Foto del usuario actual
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFF6366F1), CircleShape)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(currentUserData?.fotoLocal ?: ""),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Campo de texto
                        TextField(
                            value = nuevoComentario,
                            onValueChange = { nuevoComentario = it },
                            placeholder = {
                                Text(
                                    "Escribe un comentario...",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF1F5F9),
                                unfocusedContainerColor = Color(0xFFF1F5F9),
                                disabledContainerColor = Color(0xFFF1F5F9),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            maxLines = 3
                        )

                        // Botón de enviar
                        IconButton(
                            onClick = { enviarComentario() },
                            enabled = nuevoComentario.isNotBlank(),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (nuevoComentario.isNotBlank())
                                        Color(0xFF6366F1)
                                    else
                                        Color.LightGray
                                )
                        ) {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "Enviar comentario",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Encabezado (autor + tiempo)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF6366F1), CircleShape)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(publicacion.imagenPath ?: ""),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        publicacion.autor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1F2937)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            publicacion.tiempo,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Texto de la publicación
            Text(
                publicacion.texto,
                fontSize = 15.sp,
                color = Color(0xFF374151),
                lineHeight = 22.sp
            )

            // Grupo (si existe)
            if (publicacion.grupo.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF6366F1).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Groups,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            publicacion.grupo,
                            fontSize = 12.sp,
                            color = Color(0xFF6366F1),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Imagen (si existe)
            if (!publicacion.imagenPath.isNullOrEmpty()) {
                Spacer(Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(publicacion.imagenPath),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun ComentarioItem(comentario: Comentario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Foto del usuario
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFF6366F1).copy(alpha = 0.3f), CircleShape)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(comentario.userPhoto ?: ""),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Contenido del comentario
            Column(modifier = Modifier.weight(1f)) {
                // Nombre y fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        comentario.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1F2937)
                    )

                    Text(
                        formatDate(comentario.fecha),
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Texto del comentario
                Text(
                    comentario.texto,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF374151)
                )
            }
        }
    }
}

// Función para formatear la fecha
fun formatDate(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Ahora"
        minutes < 60 -> "Hace ${minutes}m"
        hours < 24 -> "Hace ${hours}h"
        days < 7 -> "Hace ${days}d"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            sdf.format(date)
        }
    }
}