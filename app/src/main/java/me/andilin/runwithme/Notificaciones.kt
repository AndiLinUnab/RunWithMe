package me.andilin.runwithme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import me.andilin.runwithme.model.Notificacion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(navController: NavHostController? = null) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid
    val coroutineScope = rememberCoroutineScope()

    var notificaciones by remember { mutableStateOf<List<Notificacion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("todas") } // todas, no_leidas

    // Cargar notificaciones desde Firestore
    LaunchedEffect(Unit) {
        try {
            if (currentUserId != null) {
                val notifSnap = db.collection("notificaciones")
                    .whereEqualTo("usuarioId", currentUserId)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                notificaciones = notifSnap.documents.mapNotNull { doc ->
                    Notificacion(
                        id = doc.id,
                        tipo = doc.getString("tipo") ?: "",
                        titulo = doc.getString("titulo") ?: "",
                        mensaje = doc.getString("mensaje") ?: "",
                        tiempo = doc.getString("tiempo") ?: "",
                        leida = doc.getBoolean("leida") ?: false,
                        usuarioId = doc.getString("usuarioId") ?: "",
                        usuarioNombre = doc.getString("usuarioNombre") ?: "",
                        usuarioFoto = doc.getString("usuarioFoto") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // Función para marcar como leída
    suspend fun marcarComoLeida(notifId: String) {
        try {
            db.collection("notificaciones").document(notifId)
                .update("leida", true)
                .await()

            notificaciones = notificaciones.map { notif ->
                if (notif.id == notifId) notif.copy(leida = true) else notif
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Función para marcar todas como leídas
    suspend fun marcarTodasComoLeidas() {
        try {
            val batch = db.batch()
            notificaciones.filter { !it.leida }.forEach { notif ->
                val docRef = db.collection("notificaciones").document(notif.id)
                batch.update(docRef, "leida", true)
            }
            batch.commit().await()

            notificaciones = notificaciones.map { it.copy(leida = true) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val notificacionesFiltradas = when (selectedFilter) {
        "no_leidas" -> notificaciones.filter { !it.leida }
        else -> notificaciones
    }

    val noLeidas = notificaciones.count { !it.leida }

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
                        onClick = { navController?.popBackStack() },
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
                            Icons.Outlined.Notifications,
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
                                "Notificaciones",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (noLeidas > 0) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFEF4444),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        "$noLeidas",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Filtros y marcar todas como leídas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedFilter == "todas",
                            onClick = { selectedFilter = "todas" },
                            label = { Text("Todas", fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6366F1),
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "no_leidas",
                            onClick = { selectedFilter = "no_leidas" },
                            label = { Text("No leídas ($noLeidas)", fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6366F1),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    if (noLeidas > 0) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    marcarTodasComoLeidas()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Outlined.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF6366F1)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Marcar todas",
                                color = Color(0xFF6366F1),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Loading state
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
                                "Cargando notificaciones...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else if (notificacionesFiltradas.isEmpty()) {
                    // Estado vacío
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.LightGray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (selectedFilter == "no_leidas") "No hay notificaciones sin leer"
                                else "No hay notificaciones",
                                color = Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Te avisaremos cuando haya algo nuevo",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // Lista de notificaciones
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notificacionesFiltradas) { notif ->
                            NotificacionItem(
                                notificacion = notif,
                                onClick = {
                                    if (!notif.leida) {
                                        coroutineScope.launch {
                                            marcarComoLeida(notif.id)
                                        }
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacionItem(
    notificacion: Notificacion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) Color.White else Color(0xFF6366F1).copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(if (notificacion.leida) 1.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono de tipo de notificación
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (notificacion.tipo) {
                            "like" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            "comentario" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                            "grupo" -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                            "seguidor" -> Color(0xFF10B981).copy(alpha = 0.15f)
                            "evento" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                            else -> Color.LightGray.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notificacion.tipo) {
                        "like" -> Icons.Filled.Favorite
                        "comentario" -> Icons.Outlined.ModeComment
                        "grupo" -> Icons.Outlined.Groups
                        "seguidor" -> Icons.Outlined.PersonAdd
                        "evento" -> Icons.Outlined.Event
                        else -> Icons.Outlined.Notifications
                    },
                    contentDescription = null,
                    tint = when (notificacion.tipo) {
                        "like" -> Color(0xFFEF4444)
                        "comentario" -> Color(0xFF3B82F6)
                        "grupo" -> Color(0xFF8B5CF6)
                        "seguidor" -> Color(0xFF10B981)
                        "evento" -> Color(0xFFF59E0B)
                        else -> Color.Gray
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        notificacion.titulo,
                        fontSize = 15.sp,
                        fontWeight = if (notificacion.leida) FontWeight.Medium else FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.weight(1f)
                    )
                    if (!notificacion.leida) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6366F1))
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    notificacion.mensaje,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.LightGray
                    )
                    Text(
                        notificacion.tiempo,
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}