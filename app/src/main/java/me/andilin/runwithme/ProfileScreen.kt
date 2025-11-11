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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    // ---- Cargar datos desde Firestore ----
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        try {
            val userDoc = db.collection("usuarios").document(uid).get().await()
            if (userDoc.exists()) user = userDoc.toObject(UserData::class.java) ?: UserData()

            val groupsSnap = db.collection("grupos")
                .whereArrayContains("members", uid)
                .get().await()
            groups = groupsSnap.documents.mapNotNull { doc ->
                Group(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    distance = (doc.getDouble("distance") ?: 0.0).toFloat(),
                    pace = doc.getDouble("pace") ?: 0.0
                )
            }

            val postsSnap = db.collection("publicaciones")
                .whereEqualTo("userId", uid)
                .get().await()
            posts = postsSnap.documents.mapNotNull { doc ->
                Publicacion(
                    id = doc.id,
                    texto = doc.getString("texto") ?: "",
                    imagenPath = doc.getString("imageUri"),
                    tiempo = doc.getString("timeAgo") ?: ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ---- Calcular estadísticas ----
    val totalKm = groups.sumOf { it.distance.toDouble() }
    val avgPace = if (groups.isNotEmpty()) groups.map { it.pace }.average() else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ---- Foto y nombre ----
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.LightGray, CircleShape)
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

            Text(user.nombre.ifEmpty { "Usuario" }, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(user.nivel.ifEmpty { "Sin nivel" }, color = Color.Gray, fontSize = 14.sp)

            Spacer(Modifier.height(8.dp))

            // ---- Métricas ----
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

            Spacer(Modifier.height(10.dp))

            // ---- Tabs ----
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    "mis grupos",
                    fontWeight = if (selectedTab == "grupos") FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == "grupos") Color(0xFFB388EB) else Color.Gray,
                    modifier = Modifier
                        .clickable { selectedTab = "grupos" }
                        .padding(8.dp)
                )
                Text(
                    "mis posts",
                    fontWeight = if (selectedTab == "posts") FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == "posts") Color(0xFFB388EB) else Color.Gray,
                    modifier = Modifier
                        .clickable { selectedTab = "posts" }
                        .padding(8.dp)
                )
            }

            Divider(color = Color(0xFFB388EB), thickness = 1.dp)

            // ---- Contenido según pestaña ----
            if (selectedTab == "posts") {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    items(posts) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(3.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(R.drawable.placeholderprofile),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(user.nombre, fontWeight = FontWeight.Bold)
                                        Text(post.tiempo, fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(post.texto)
                                Spacer(Modifier.height(8.dp))
                                post.imagenPath?.let {
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
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    items(groups) { group ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(group.name, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("Kilómetros: ${group.distance}")
                                Text("Ritmo: ${group.pace} min/km")
                            }
                        }
                    }
                }
            }
        }
    }
}