package me.andilin.runwithme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import me.andilin.runwithme.model.GroupDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnirGrupo(navController: NavHostController? = null) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid
    val coroutineScope = rememberCoroutineScope()

    var grupos by remember { mutableStateOf<List<GroupDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successGroupName by remember { mutableStateOf("") }
    var joiningGroupId by remember { mutableStateOf<String?>(null) }

    // Cargar grupos desde Firestore
    LaunchedEffect(Unit) {
        try {
            val gruposSnap = db.collection("grupos").get().await()
            grupos = gruposSnap.documents.mapNotNull { doc ->
                val members = doc.get("members") as? List<*> ?: emptyList<String>()
                val memberIds = members.mapNotNull { it as? String }

                GroupDetail(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    experienceLevel = doc.getString("experienceLevel") ?: "",
                    sportType = doc.getString("sportType") ?: "",
                    distance = (doc.getDouble("distance") ?: 0.0).toFloat(),
                    trainingDays = (doc.get("trainingDays") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    meetingTime = doc.getString("meetingTime") ?: "",
                    memberCount = (doc.getLong("memberCount") ?: 0).toInt(),
                    members = memberIds,
                    createdBy = doc.getString("createdBy") ?: ""
                )
            }
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }

    // Función para unirse a un grupo
    suspend fun unirseAGrupo(grupoId: String, grupoNombre: String) {
        if (currentUserId == null) {
            println("❌ Error: Usuario no autenticado")
            return
        }

        joiningGroupId = grupoId
        try {
            // Actualizar el documento del grupo
            db.collection("grupos").document(grupoId)
                .update(
                    mapOf(
                        "members" to FieldValue.arrayUnion(currentUserId),
                        "memberCount" to FieldValue.increment(1)
                    )
                ).await()

            println("✅ Usuario $currentUserId unido al grupo $grupoId")

            // Actualizar la lista local
            grupos = grupos.map { grupo ->
                if (grupo.id == grupoId) {
                    grupo.copy(
                        members = grupo.members + currentUserId,
                        memberCount = grupo.memberCount + 1
                    )
                } else {
                    grupo
                }
            }

            successGroupName = grupoNombre
            showSuccessMessage = true
            joiningGroupId = null
        } catch (e: Exception) {
            println("❌ Error al unirse al grupo: ${e.message}")
            joiningGroupId = null
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
                            Icons.Outlined.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Unirse a Grupos",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Mensaje de éxito
                AnimatedVisibility(
                    visible = showSuccessMessage,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF10B981),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "¡Te uniste a $successGroupName!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            IconButton(onClick = { showSuccessMessage = false }) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

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
                                "Cargando grupos...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else if (grupos.isEmpty()) {
                    // Estado vacío
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.GroupOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.LightGray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No hay grupos disponibles",
                                color = Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Sé el primero en crear uno",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // Lista de grupos
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(grupos) { grupo ->
                            GrupoCard(
                                grupo = grupo,
                                isJoined = currentUserId in grupo.members,
                                isJoining = joiningGroupId == grupo.id,
                                onUnirse = {
                                    coroutineScope.launch {
                                        unirseAGrupo(grupo.id, grupo.name)
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
fun GrupoCard(
    grupo: GroupDetail,
    isJoined: Boolean,
    isJoining: Boolean,
    onUnirse: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header con nombre y badge de nivel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        grupo.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    if (grupo.description.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            grupo.description,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Badge de nivel
                Surface(
                    color = when (grupo.experienceLevel) {
                        "Principiante" -> Color(0xFF10B981)
                        "Intermedio" -> Color(0xFFF59E0B)
                        "Avanzado" -> Color(0xFFEF4444)
                        else -> Color.Gray
                    }.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        grupo.experienceLevel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (grupo.experienceLevel) {
                            "Principiante" -> Color(0xFF10B981)
                            "Intermedio" -> Color(0xFFF59E0B)
                            "Avanzado" -> Color(0xFFEF4444)
                            else -> Color.Gray
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info del grupo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Distancia
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Route,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${grupo.distance.toInt()} km",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                }

                // Hora
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        grupo.meetingTime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                }

                // Miembros
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.People,
                        contentDescription = null,
                        tint = Color(0xFFA855F7),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${grupo.memberCount} miembros",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                }

                // Tipo
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (grupo.sportType == "Running") Icons.Outlined.DirectionsRun
                        else Icons.Outlined.DirectionsBike,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        grupo.sportType,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                }
            }

            // Días de entrenamiento
            if (grupo.trainingDays.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        grupo.trainingDays.joinToString(", "),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botón de unirse
            Button(
                onClick = onUnirse,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isJoined && !isJoining,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isJoined) Color(0xFF10B981) else Color(0xFF6366F1),
                    disabledContainerColor = if (isJoined) Color(0xFF10B981) else Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isJoining) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else if (isJoined) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Ya eres miembro",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        Icons.Outlined.GroupAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Unirse al grupo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
