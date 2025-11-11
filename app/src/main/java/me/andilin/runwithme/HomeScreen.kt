package me.andilin.runwithme

import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group

import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.navigation.NavController

import coil.compose.rememberAsyncImagePainter

import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import me.andilin.runwithme.model.Group
import me.andilin.runwithme.model.Publicacion




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onCrearPublicacion: () -> Unit = { navController.navigate("create_publication") }
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var grupos by remember { mutableStateOf<List<Group>>(emptyList()) }
    var publicaciones by remember { mutableStateOf<List<Publicacion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔹 Cargar grupos y publicaciones del usuario
    LaunchedEffect(userId) {
        if (userId != null) {
            val gruposSnapshot = db.collection("groups")
                .whereArrayContains("members", userId)
                .get()
                .await()

            val publicacionesSnapshot = db.collection("publicaciones")
                .orderBy("tiempo")
                .get()
                .await()

            grupos = gruposSnapshot.toObjects(Group::class.java)
            publicaciones = publicacionesSnapshot.toObjects(Publicacion::class.java)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RunWithMe 🏃", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearPublicacion) {
                Icon(Icons.Default.Add, contentDescription = "Crear publicación")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                item {
                    Text(
                        "Mis Grupos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (grupos.isEmpty()) {
                    item {
                        Text("No perteneces a ningun grupo")
                    }
                } else {
                    items(grupos) { grupo ->
                        GrupoCard(grupo)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Estados Recientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (publicaciones.isEmpty()) {
                    item { Text("Aún no hay publicaciones 💤") }
                } else {
                    items(publicaciones) { post ->
                        PublicacionCard(post)
                    }
                }
            }
        }
    }
}

@Composable
fun GrupoCard(grupo: Group) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(grupo.name, fontWeight = FontWeight.Bold)
                Text(
                    "${grupo.sportType} • ${grupo.experienceLevel} • ${grupo.memberCount} miembros",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PublicacionCard(post: Publicacion) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(post.autor, fontWeight = FontWeight.Bold)
            Text(post.texto)
            post.imagenPath?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }
            if (post.grupo.isNotEmpty()) {
                Text(
                    "📍 Grupo: ${post.grupo}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}