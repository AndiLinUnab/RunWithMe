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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    onCrearPublicacion: () -> Unit = {},
    navToProfile: () -> Unit = {},
    navToGroups: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()

    var publicaciones by remember { mutableStateOf<List<Publicacion>>(emptyList()) }
    var historias by remember { mutableStateOf<List<Historia>>(emptyList()) }
    var grupos by remember { mutableStateOf<List<Group>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔹 Cargar datos desde Firestore
    LaunchedEffect(Unit) {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

            // 🔹 Cargar publicaciones e historias
            val publicacionesSnap = db.collection("publicaciones")
                .orderBy("tiempo")
                .get()
                .await()
            val historiasSnap = db.collection("historias")
                .orderBy("tiempo")
                .get()
                .await()

            // 🔹 Cargar grupos del usuario (como miembro o creador)
            val gruposCreadosSnap = db.collection("grupos")
                .whereEqualTo("createdBy", uid)
                .get()
                .await()

            val gruposMiembroSnap = db.collection("grupos")
                .whereArrayContains("members", uid)
                .get()
                .await()

            // 🔹 Unir y evitar duplicados
            val todosGruposDocs = (gruposCreadosSnap.documents + gruposMiembroSnap.documents)
                .distinctBy { it.id }

            publicaciones = publicacionesSnap.toObjects(Publicacion::class.java).reversed()
            historias = historiasSnap.toObjects(Historia::class.java).reversed()
            grupos = todosGruposDocs.mapNotNull { it.toObject(Group::class.java) }

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
                        IconButton(onClick = { navController.navigate("crear_grupo") }) {
                            Icon(Icons.Default.Groups, contentDescription = "Grupos", tint = Color.White)
                        }
                        IconButton(onClick = { navController.navigate("mapa") }) {
                            Icon(Icons.Default.Map, contentDescription = "Mapa", tint = Color.White)
                        }
                        IconButton(onClick = { /* Ya estás en Home */ }) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White)
                        }
                        IconButton(onClick = { navController.navigate("profile") }) {
                            Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
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
                // 🔹 Historias
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
                                Text(historia.autor.take(10), fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // 🔹 Mis grupos (solo si existen)
                if (grupos.isNotEmpty()) {
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
                        items(grupos) { grupo ->
                            Card(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(width = 130.dp, height = 70.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF6FF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(grupo.name, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                }

                // 🔹 Publicaciones
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    items(publicaciones) { post ->
                        PostItem(post = post, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Publicacion, navController: NavHostController) {
    var liked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 🔹 Encabezado (autor + tiempo)
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

            // 🔹 Texto de la publicación
            Text(post.texto)
            Spacer(Modifier.height(8.dp))

            // 🔹 Imagen (si hay)
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

            // 🔹 Botones de interacción
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

                // 🔹 BOTÓN DE COMENTARIOS - AHORA FUNCIONAL
                IconButton(
                    onClick = {
                        // Navegar a la pantalla de comentarios con el ID de la publicación
                        navController.navigate("comentarios/${post.id}")
                    }
                ) {
                    Icon(Icons.Default.Message, contentDescription = "Comentarios")
                }
            }
        }
    }
}