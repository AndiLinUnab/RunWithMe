package me.andilin.runwithme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
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
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf("home") }

    // Cargar publicaciones e historias desde Firestore
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
            // TopBar con gradiente
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF8B5CF6)
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Logo y título
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.DirectionsRun,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "RunWithMe",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        // Acciones
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // NUEVO: Botón para unirse a grupos
                            IconButton(onClick = {
                                navController.navigate("unir_grupo")
                            }) {
                                Icon(
                                    Icons.Outlined.GroupAdd,
                                    contentDescription = "Unirse a grupos",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            IconButton(onClick = {
                                navController.navigate("notificaciones")
                            }) {
                                Badge(
                                    containerColor = Color(0xFFEF4444)
                                ) {
                                    Icon(
                                        Icons.Outlined.Notifications,
                                        contentDescription = "Notificaciones",
                                        tint = Color.White
                                    )
                                }
                            }
                            IconButton(onClick = onCrearPublicacion) {
                                Icon(
                                    Icons.Outlined.AddCircleOutline,
                                    contentDescription = "Crear publicación",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedTab == "home") Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Inicio", fontSize = 11.sp) },
                    selected = selectedTab == "home",
                    onClick = { selectedTab = "home" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6366F1),
                        selectedTextColor = Color(0xFF6366F1),
                        indicatorColor = Color(0xFF6366F1).copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedTab == "groups") Icons.Filled.Groups else Icons.Outlined.Groups,
                            contentDescription = "Grupos"
                        )
                    },
                    label = { Text("Grupos", fontSize = 11.sp) },
                    selected = selectedTab == "groups",
                    onClick = {
                        selectedTab = "groups"
                        navController.navigate("crear_grupo")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6366F1),
                        selectedTextColor = Color(0xFF6366F1),
                        indicatorColor = Color(0xFF6366F1).copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedTab == "map") Icons.Filled.Map else Icons.Outlined.Map,
                            contentDescription = "Mapa"
                        )
                    },
                    label = { Text("Mapa", fontSize = 11.sp) },
                    selected = selectedTab == "map",
                    onClick = {
                        selectedTab = "map"
                        navController.navigate("mapa")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6366F1),
                        selectedTextColor = Color(0xFF6366F1),
                        indicatorColor = Color(0xFF6366F1).copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedTab == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Perfil"
                        )
                    },
                    label = { Text("Perfil", fontSize = 11.sp) },
                    selected = selectedTab == "profile",
                    onClick = {
                        selectedTab = "profile"
                        navController.navigate("profile")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6366F1),
                        selectedTextColor = Color(0xFF6366F1),
                        indicatorColor = Color(0xFF6366F1).copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color(0xFF6366F1),
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Cargando feed...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8FAFC))
            ) {
                // Historias
                if (historias.isNotEmpty()) {
                    item {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Historias",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1F2937),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(historias) { historia ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(
                                                            Color(0xFF6366F1),
                                                            Color(0xFFA855F7)
                                                        )
                                                    )
                                                )
                                                .padding(3.dp)
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(historia.imagenPath),
                                                contentDescription = "Historia de ${historia.autor}",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .border(3.dp, Color.White, CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            historia.autor.take(10),
                                            fontSize = 12.sp,
                                            color = Color(0xFF6B7280),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }

                // Mis grupos
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mis grupos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1F2937)
                            )
                            TextButton(
                                onClick = { navController.navigate("crear_grupo") }
                            ) {
                                Text(
                                    "Ver todos",
                                    color = Color(0xFF6366F1),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val grupos = listOf("Runners Bogotá", "Atletas del Sol", "Nocturnos 5K")
                            items(grupos) { grupo ->
                                Card(
                                    modifier = Modifier
                                        .size(width = 160.dp, height = 90.dp)
                                        .clickable { navController.navigate("crear_grupo") },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xFF6366F1).copy(alpha = 0.1f),
                                                        Color(0xFF8B5CF6).copy(alpha = 0.05f)
                                                    )
                                                )
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Icon(
                                                Icons.Outlined.Groups,
                                                contentDescription = null,
                                                tint = Color(0xFF6366F1),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                grupo,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF1F2937),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // Header de publicaciones
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.DynamicFeed,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Feed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1F2937)
                        )
                    }
                }

                // Publicaciones
                if (publicaciones.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.PostAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "No hay publicaciones",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = onCrearPublicacion) {
                                    Text("Crea la primera publicación")
                                }
                            }
                        }
                    }
                } else {
                    items(publicaciones) { post ->
                        PostItem(post)
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Publicacion) {
    var liked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(156) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header del post
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
                        painter = rememberAsyncImagePainter(post.imagenPath ?: ""),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        post.autor,
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
                            post.tiempo,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                IconButton(onClick = { /* Opciones */ }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "Más opciones",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Contenido del post
            Text(
                post.texto,
                fontSize = 15.sp,
                color = Color(0xFF374151),
                lineHeight = 22.sp
            )

            if (!post.imagenPath.isNullOrEmpty()) {
                Spacer(Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(post.imagenPath),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(12.dp))

            // Contador de likes y comentarios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$likesCount me gusta",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "24 comentarios",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFE5E7EB)
            )

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Like button
                TextButton(
                    onClick = {
                        liked = !liked
                        likesCount += if (liked) 1 else -1
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (liked) Color(0xFFEF4444) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Me gusta",
                        color = if (liked) Color(0xFFEF4444) else Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = if (liked) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                // Comment button
                TextButton(
                    onClick = { /* Abrir comentarios */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.ModeComment,
                        contentDescription = "Comentar",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Comentar",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                // Share button
                TextButton(
                    onClick = { /* Compartir */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = "Compartir",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Compartir",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}