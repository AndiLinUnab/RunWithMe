package me.andilin.runwithme
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import me.andilin.runwithme.model.UserData
import me.andilin.runwithme.model.Group
import me.andilin.runwithme.model.Publicacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf(UserData()) }
    var posts by remember { mutableStateOf(listOf<Publicacion>()) }
    var groups by remember { mutableStateOf(listOf<Group>()) }
    var selectedTab by remember { mutableStateOf("posts") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
        }

    // ---- Cargar datos del usuario, publicaciones y grupos ----
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        try {
            // Usuario
            val userDoc = db.collection("usuarios").document(uid).get().await()
            if (userDoc.exists()) {
                user = userDoc.toObject(UserData::class.java) ?: UserData()
            }

            // Grupos donde el usuario es miembro
            val groupsSnap = db.collection("grupos")
                .whereArrayContains("members", uid)
                .get().await()
            groups = groupsSnap.toObjects(Group::class.java)

            // Publicaciones del usuario
            val postsSnap = db.collection("publicaciones")
                .whereEqualTo("userId", uid)
                .get().await()
            posts = postsSnap.toObjects(Publicacion::class.java).reversed()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ---- Cálculo de estadísticas ----
    val totalKm = groups.sumOf { it.distance.toDouble() }
    val avgPace = if (groups.isNotEmpty()) groups.map { it.pace }.average() else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF40A2E3))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ---- Imagen de perfil ----
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF40A2E3), CircleShape)
                    .clickable { pickImageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val painter = imageUri?.let { rememberAsyncImagePainter(it) }
                    ?: painterResource(R.drawable.placeholderprofile)
                Image(
                    painter = painter,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(user.nombre.ifEmpty { "Usuario" }, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(user.nivel.ifEmpty { "Sin nivel definido" }, color = Color.Gray, fontSize = 14.sp)

            Spacer(Modifier.height(16.dp))

            // ---- Estadísticas ----
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$totalKm", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("kilómetros", color = Color.Gray, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(String.format("%.2f", avgPace), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("ritmo promedio", color = Color.Gray, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Tabs ----
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    "Mis grupos",
                    fontWeight = if (selectedTab == "grupos") FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == "grupos") Color(0xFFB388EB) else Color.Gray,
                    modifier = Modifier
                        .clickable { selectedTab = "grupos" }
                        .padding(8.dp)
                )
                Text(
                    "Mis publicaciones",
                    fontWeight = if (selectedTab == "posts") FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == "posts") Color(0xFFB388EB) else Color.Gray,
                    modifier = Modifier
                        .clickable { selectedTab = "posts" }
                        .padding(8.dp)
                )
            }

            Divider(color = Color(0xFFB388EB), thickness = 1.dp)

            // ---- Contenido dinámico ----
            when (selectedTab) {
                "posts" -> {
                    if (posts.isEmpty()) {
                        Text("Aún no has publicado nada.", color = Color.Gray, modifier = Modifier.padding(20.dp))
                    } else {
                        posts.forEach { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                shape = RoundedCornerShape(10.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(post.texto, fontWeight = FontWeight.Normal)
                                    post.imagenPath?.takeIf { it.isNotEmpty() }?.let {
                                        Spacer(Modifier.height(8.dp))
                                        Image(
                                            painter = rememberAsyncImagePainter(it),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(post.tiempo, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                "grupos" -> {
                    if (groups.isEmpty()) {
                        Text("No perteneces a ningún grupo todavía.", color = Color.Gray, modifier = Modifier.padding(20.dp))
                    } else {
                        groups.forEach { group ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                shape = RoundedCornerShape(10.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(group.name, fontWeight = FontWeight.Bold)
                                    Text(group.sportType, color = Color.Gray)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Distancia: ${group.distance} km")
                                    Text("Ritmo: ${group.pace} min/km")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}