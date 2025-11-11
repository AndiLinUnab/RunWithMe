package me.andilin.runwithme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications

import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController
import androidx.navigation.NavHostController

import coil.compose.rememberAsyncImagePainter

import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import me.andilin.runwithme.model.Group
import me.andilin.runwithme.model.Historia
import me.andilin.runwithme.model.Publicacion
import kotlin.collections.isNotEmpty


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: androidx.navigation.NavHostController,
    onCrearPublicacion: () -> Unit = {},
    navToProfile: () -> Unit = {},
    navToGroups: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()

    var publicaciones by remember { mutableStateOf<List<Publicacion>>(emptyList()) }
    var historias by remember { mutableStateOf<List<Historia>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔹 Cargar publicaciones e historias desde Firestore
    LaunchedEffect(Unit) {
        try {
            val publicacionesSnap = db.collection("publicaciones")
                .orderBy("tiempo")
                .get()
                .await()
            val historiasSnap = db.collection("historias")
                .orderBy("tiempo")
                .get()
                .await()

            publicaciones = publicacionesSnap.toObjects(Publicacion::class.java).reversed()
            historias = historiasSnap.toObjects(Historia::class.java).reversed()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF40A2E3)),
                navigationIcon = {
                    IconButton(onClick = { /* Configuración */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Config")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notificaciones */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
                    IconButton(onClick = onCrearPublicacion) {
                        Icon(Icons.Default.Add, contentDescription = "Crear publicación")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF40A2E3),
                content = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = navToGroups) {
                            Icon(Icons.Default.Groups, contentDescription = "Grupos")
                        }
                        IconButton(onClick = { /* Home actual */ }) {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                        }
                        IconButton(onClick = navToProfile) {
                            Icon(Icons.Default.Person, contentDescription = "Perfil")
                        }
                    }
                }
            )
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
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // 🔹 Sección de historias (solo si existen)
                if (historias.isNotEmpty()) {
                    Text(
                        text = "Historias recientes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(12.dp)
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(historias) { historia ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .border(2.dp, Color(0xFF40A2E3), CircleShape)
                                        .padding(3.dp)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(historia.imagenPath),
                                        contentDescription = "Historia de ${historia.autor}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    historia.autor.take(10),
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // 🔹 Mis grupos (placeholder hasta conectar con Firestore)
                Text(
                    text = "Mis grupos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    val grupos = listOf("Runners Bogotá", "Atletas del Sol", "Nocturnos 5K")
                    items(grupos) { grupo ->
                        Card(
                            modifier = Modifier
                                .padding(6.dp)
                                .size(width = 130.dp, height = 70.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF6FF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(grupo, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // 🔹 Publicaciones
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    items(publicaciones) { post ->
                        PostItem(post)
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Publicacion) {
    var liked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(post.imagenPath ?: ""),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(post.autor, fontWeight = FontWeight.Bold)
                    Text(post.tiempo, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(post.texto)
            Spacer(Modifier.height(8.dp))

            if (!post.imagenPath.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(post.imagenPath),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }

            Row {
                IconButton(onClick = { liked = !liked }) {
                    Icon(
                        painter = painterResource(
                            if (liked) me.andilin.runwithme.R.drawable.corazonlleno
                            else me.andilin.runwithme.R.drawable.corazonvacio
                        ),
                        contentDescription = "Like",
                        tint = if (liked) Color.Red else Color.Gray
                    )
                }
                IconButton(onClick = { /* Abrir comentarios */ }) {
                    Icon(Icons.Default.Message, contentDescription = "Mensaje")
                }
            }
        }
    }
}