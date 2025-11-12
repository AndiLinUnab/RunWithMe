package me.andilin.runwithme



import android.net.Uri

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import me.andilin.runwithme.model.Group
import me.andilin.runwithme.model.Publicacion
import me.andilin.runwithme.model.UserData

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

val publicacionesGlobales = mutableStateListOf<Publicacion>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePublication(
    onPublicar: () -> Unit,
    onBack: () -> Unit
) {
    var texto by remember { mutableStateOf(TextFieldValue("")) }
    var selectedGroup by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tipoPublicacion by remember { mutableStateOf("Publicación") }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    // 🔹 Lista dinámica de grupos del usuario
    var grupos by remember { mutableStateOf<List<Group>>(emptyList()) }
    var isLoadingGroups by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("") }

    // 🔹 Cargar datos del usuario y grupos
    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            // Cargar nombre del usuario desde la colección usuarios
            val userDoc = db.collection("usuarios").document(uid).get().await()
            val userData = userDoc.toObject(UserData::class.java)
            userName = userData?.nombre ?: "Usuario"

            // 🔹 CORRECCIÓN: Usar la misma colección que en HomeScreen
            // Primero intenta con "grupos", si no funciona prueba con "groups"
            var gruposCollection = "grupos"

            try {
                // Cargar grupos donde el usuario es miembro
                val gruposMiembroSnap = db.collection(gruposCollection)
                    .whereArrayContains("members", uid)
                    .get()
                    .await()

                // Cargar grupos creados por el usuario
                val gruposCreadosSnap = db.collection(gruposCollection)
                    .whereEqualTo("createdBy", uid)
                    .get()
                    .await()

                // Unir y evitar duplicados
                val todosGruposDocs = (gruposCreadosSnap.documents + gruposMiembroSnap.documents)
                    .distinctBy { it.id }

                grupos = todosGruposDocs.mapNotNull { it.toObject(Group::class.java) }

            } catch (e: Exception) {
                // Si falla, intentar con "groups"
                gruposCollection = "groups"
                val gruposMiembroSnap = db.collection(gruposCollection)
                    .whereArrayContains("members", uid)
                    .get()
                    .await()

                val gruposCreadosSnap = db.collection(gruposCollection)
                    .whereEqualTo("createdBy", uid)
                    .get()
                    .await()

                val todosGruposDocs = (gruposCreadosSnap.documents + gruposMiembroSnap.documents)
                    .distinctBy { it.id }

                grupos = todosGruposDocs.mapNotNull { it.toObject(Group::class.java) }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error cargando grupos: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            isLoadingGroups = false
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear ${tipoPublicacion.lowercase()}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Selector tipo ---
            Text("Tipo de contenido", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Publicación", "Historia").forEach { tipo ->
                    FilterChip(
                        selected = tipoPublicacion == tipo,
                        onClick = { tipoPublicacion = tipo },
                        label = { Text(tipo) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- Texto ---
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text("¿Qué tal tu entrenamiento hoy?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(Modifier.height(20.dp))

            // --- Imagen ---
            Text("Subir foto", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F0))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- Menú desplegable de grupos ---
            Text("Grupo", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }

            if (isLoadingGroups) {
                CircularProgressIndicator()
            } else {
                Box {
                    OutlinedTextField(
                        value = selectedGroup,
                        onValueChange = {},
                        placeholder = {
                            Text(
                                if (grupos.isEmpty()) "No tienes grupos"
                                else "Seleccionar grupo"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Desplegar")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (grupos.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No perteneces a ningún grupo") },
                                onClick = { expanded = false }
                            )
                        } else {
                            // Opción para publicar sin grupo
                            DropdownMenuItem(
                                text = { Text("General (sin grupo)") },
                                onClick = {
                                    selectedGroup = ""
                                    expanded = false
                                }
                            )
                            grupos.forEach { grupo ->
                                DropdownMenuItem(
                                    text = { Text(grupo.name) },
                                    onClick = {
                                        selectedGroup = grupo.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            // --- Botón publicar ---
            Button(
                onClick = {
                    if (texto.text.isNotBlank() || selectedImageUri != null) {
                        val id = UUID.randomUUID().toString()
                        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                        val userId = auth.currentUser?.uid ?: ""

                        val data = mapOf(
                            "id" to id,
                            "autor" to userName,
                            "texto" to texto.text,
                            "grupo" to selectedGroup.ifEmpty { "General" },
                            "imagenPath" to (selectedImageUri?.toString() ?: ""),
                            "tiempo" to fecha,
                            "userId" to userId
                        )

                        val coleccion = if (tipoPublicacion == "Historia") "historias" else "publicaciones"

                        db.collection(coleccion)
                            .document(id)
                            .set(data)
                            .addOnSuccessListener {
                                Toast.makeText(context, "$tipoPublicacion creada", Toast.LENGTH_SHORT).show()
                                onPublicar()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Agrega texto o una imagen antes de publicar", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD6B3FF)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Publicar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}