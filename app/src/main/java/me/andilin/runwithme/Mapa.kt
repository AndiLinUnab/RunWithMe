package me.andilin.runwithme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mapa(navController: NavHostController? = null) { // Agregar navController como parámetro
    var currentLatitude by remember { mutableStateOf(0.0) }
    var currentLongitude by remember { mutableStateOf(0.0) }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var activityType by remember { mutableStateOf("Running") }
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val activityTypes = listOf("Running", "Ciclismo", "Caminata")

    // Función para obtener ubicación actual (simulada por ahora)
    fun obtenerUbicacionActual() {
        // En una app real, aquí iría la lógica de GPS
        currentLatitude = 19.4326  // Ejemplo: Ciudad de México
        currentLongitude = -99.1332
        address = "Avenida 42"
        city = "Bucaramanga"
        country = "Colombia"
    }

    // Función para guardar datos del mapa en Firebase
    fun guardarMapaEnFirestore() {
        val usuario = auth.currentUser
        if (usuario == null) {
            println("❌ ERROR: Usuario no está logueado")
            return
        }

        if (currentLatitude == 0.0 || currentLongitude == 0.0) {
            println("❌ ERROR: Ubicación no válida")
            return
        }

        println("✅ Guardando datos del mapa:")
        println("   - Latitud: $currentLatitude")
        println("   - Longitud: $currentLongitude")
        println("   - Dirección: $address")
        println("   - Ciudad: $city")
        println("   - País: $country")
        println("   - Tipo actividad: $activityType")
        println("   - Nota: $note")

        isLoading = true

        // Crear objeto MapData con data class
        val mapData = MapData(
            userId = usuario.uid,
            userName = usuario.displayName ?: "Usuario",
            location = MapLocation(
                latitude = currentLatitude,
                longitude = currentLongitude,
                address = address,
                city = city,
                country = country
            ),
            activityType = activityType,
            note = note,
            timestamp = Date()
        )

        // Guardar en Firestore
        firestore.collection("map_locations")
            .add(mapData)
            .addOnSuccessListener { documento ->
                isLoading = false
                showSuccessMessage = true
                println("✅✅✅ UBICACIÓN GUARDADA EXITOSAMENTE!")
                println("✅ ID: ${documento.id}")
                println("✅ Colección: map_locations")

                // Limpiar formulario (opcional)
                note = ""
            }
            .addOnFailureListener { error ->
                isLoading = false
                println("❌❌❌ ERROR AL GUARDAR: ${error.message}")
            }
    }

    // Obtener ubicación al iniciar
    LaunchedEffect(Unit) {
        obtenerUbicacionActual()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mi Mapa",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    // Botón de flecha hacia atrás
                    IconButton(
                        onClick = {
                            navController?.popBackStack() // Regresa a la pantalla anterior (HomeScreen)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mensaje de éxito
            if (showSuccessMessage) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅ Ubicación guardada exitosamente!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showSuccessMessage = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar mensaje",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Loading
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // NOTA: EL TÍTULO "MI MAPA" YA NO VA AQUÍ, ESTÁ EN EL TOPBAR

            // Información de ubicación
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ubicación",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ubicación Actual",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Coordenadas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Latitud:", fontWeight = FontWeight.Medium)
                        Text(String.format("%.6f", currentLatitude))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Longitud:", fontWeight = FontWeight.Medium)
                        Text(String.format("%.6f", currentLongitude))
                    }

                    // Dirección
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("Ciudad") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("País") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // Tipo de actividad
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tipo de Actividad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activityTypes.forEach { type ->
                            FilterChip(
                                selected = activityType == type,
                                onClick = { activityType = type },
                                label = { Text(type) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Nota adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Nota Adicional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Agrega una nota sobre esta ubicación...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        singleLine = false
                    )
                }
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón actualizar ubicación
                OutlinedButton(
                    onClick = { obtenerUbicacionActual() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Actualizar ubicación")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizar Ubicación")
                }

                // Botón guardar
                Button(
                    onClick = { guardarMapaEnFirestore() },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && currentLatitude != 0.0 && currentLongitude != 0.0
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Guardar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Ubicación")
                }
            }

            // Información adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ℹ️ Información",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Esta ubicación se guardará en Firebase y podrá ser vista por otros usuarios de la app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF424242)
                    )
                }
            }
        }
    }
}