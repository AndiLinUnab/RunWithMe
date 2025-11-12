package me.andilin.runwithme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
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
fun Grupos(navController: NavHostController? = null) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var useCurrentLocation by remember { mutableStateOf(true) }
    var selectedExperience by remember { mutableStateOf("Principiante") }
    var selectedType by remember { mutableStateOf("Running") }
    var distance by remember { mutableStateOf(15f) }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var selectedHour by remember { mutableStateOf(18) } // Hora (0-23)
    var selectedMinute by remember { mutableStateOf(0) } // Minutos (0-59)
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val experienceLevels = listOf("Principiante", "Intermedio", "Avanzado")
    val sportTypes = listOf("Running", "Ciclismo")
    val hours = (0..23).toList()
    val minutes = listOf(0, 15, 30, 45) // Opciones de minutos: 00, 15, 30, 45

    // Definimos los colores para cada nivel
    val levelColors = mapOf(
        "Principiante" to Color(0xFF4CAF50), // Verde
        "Intermedio" to Color(0xFFFFC107),   // Amarillo
        "Avanzado" to Color(0xFF4CAF50)      // Verde
    )

    // Función para formatear la hora
    fun formatTime(hour: Int, minute: Int): String {
        val period = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return String.format("%d:%02d %s", displayHour, minute, period)
    }

    // Función para guardar en Firestore
    fun guardarGrupoEnFirestore() {
        val usuario = auth.currentUser
        if (usuario == null) {
            println("Error: Usuario no está logueado")
            return
        }

        if (groupName.isEmpty()) {
            println("Error: El nombre del grupo es requerido")
            return
        }

        isLoading = true

        // Formatear la hora para guardar (formato 24h)
        val meetingTime = String.format("%02d:%02d", selectedHour, selectedMinute)

        // Crear el mapa de datos para Firestore
        val datosGrupo = hashMapOf(
            "name" to groupName,
            "description" to groupDescription,
            "experienceLevel" to selectedExperience,
            "sportType" to selectedType,
            "distance" to distance,
            "trainingDays" to selectedDays.toList(),
            "meetingTime" to meetingTime, // Hora exacta seleccionada
            "location" to hashMapOf(
                "useCurrentLocation" to useCurrentLocation,
                "latitude" to 0.0,
                "longitude" to 0.0,
                "address" to ""
            ),
            "createdBy" to usuario.uid,
            "members" to listOf(usuario.uid),
            "memberCount" to 1,
            "createdAt" to Date(),
            "imageUrl" to ""
        )

        // Guardar en Firestore
        firestore.collection("grupos")
            .add(datosGrupo)
            .addOnSuccessListener { documento ->
                isLoading = false
                showSuccessMessage = true
                println("✅ Grupo guardado exitosamente! ID: ${documento.id}")
                println("🕐 Hora guardada: $meetingTime")

                // Limpiar el formulario
                groupName = ""
                groupDescription = ""
                selectedDays = emptySet()
                selectedHour = 18
                selectedMinute = 0
            }
            .addOnFailureListener { error ->
                isLoading = false
                println("❌ Error al guardar grupo: ${error.message}")
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Crear grupo",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController?.popBackStack()
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅ Grupo creado exitosamente!",
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

            // Campo nombre
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Nombre del grupo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Campo descripción
            OutlinedTextField(
                value = groupDescription,
                onValueChange = { groupDescription = it },
                label = { Text("Descripción del grupo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                singleLine = false
            )

            // Botón subir imagen
            OutlinedButton(
                onClick = { /* Acción para subir imagen */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Subir imagen")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Subir imagen")
            }

            // Sección Ubicación
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ubicación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Usar mi ubicación actual")
                        Switch(
                            checked = useCurrentLocation,
                            onCheckedChange = { useCurrentLocation = it }
                        )
                    }
                }
            }

            // Nivel de experiencia
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nivel de experiencia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        experienceLevels.forEach { level ->
                            val backgroundColor = levelColors[level] ?: MaterialTheme.colorScheme.primary

                            FilterChip(
                                selected = selectedExperience == level,
                                onClick = { selectedExperience = level },
                                label = {
                                    Text(
                                        level,
                                        color = if (selectedExperience == level) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = backgroundColor,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            // SECCIÓN SEPARADA: Días de entrenamiento CON SCROLL HORIZONTAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Días de entrenamiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrainingDaysSectionWithScroll(selectedDays) { nuevosDias ->
                        selectedDays = nuevosDias
                    }
                }
            }

            // SECCIÓN: Hora de salida
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
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Hora",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Hora de salida",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Hora seleccionada
                    Text(
                        text = "Hora seleccionada: ${formatTime(selectedHour, selectedMinute)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Selector de hora
                    Text(
                        text = "Hora:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    HourSelector(
                        hours = hours,
                        selectedHour = selectedHour,
                        onHourSelected = { selectedHour = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selector de minutos
                    Text(
                        text = "Minutos:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    MinuteSelector(
                        minutes = minutes,
                        selectedMinute = selectedMinute,
                        onMinuteSelected = { selectedMinute = it }
                    )
                }
            }

            // Distancia
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Distancia: ${distance.toInt()} km",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = distance,
                        onValueChange = { distance = it },
                        valueRange = 1f..50f,
                        steps = 49,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        text = "Tipo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        sportTypes.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Botón crear grupo
            Button(
                onClick = { guardarGrupoEnFirestore() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && groupName.isNotEmpty() && selectedDays.isNotEmpty(),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    "Crear grupo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// NUEVO: TrainingDaysSection con Scroll Horizontal
@Composable
fun TrainingDaysSectionWithScroll(
    selectedDays: Set<String>,
    onSelectedDaysChange: (Set<String>) -> Unit
) {
    val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
    val fullDays = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val scrollState = rememberScrollState()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Selecciona los días:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        // Contenedor con scroll horizontal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            daysOfWeek.forEachIndexed { index, day ->
                val isSelected = selectedDays.contains(fullDays[index])
                AssistChip(
                    onClick = {
                        val nuevosDias = if (isSelected) {
                            selectedDays - fullDays[index]
                        } else {
                            selectedDays + fullDays[index]
                        }
                        onSelectedDaysChange(nuevosDias)
                    },
                    label = {
                        Text(
                            text = when (day) {
                                "L" -> "Lun"
                                "M" -> "Mar"
                                "X" -> "Mié"
                                "J" -> "Jue"
                                "V" -> "Vie"
                                "S" -> "Sáb"
                                "D" -> "Dom"
                                else -> day
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = if (isSelected) "Seleccionado" else "No seleccionado"
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        leadingIconContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

// Selector de hora
@Composable
fun HourSelector(
    hours: List<Int>,
    selectedHour: Int,
    onHourSelected: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        hours.forEach { hour ->
            val isSelected = hour == selectedHour
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val period = if (hour < 12) "AM" else "PM"

            FilterChip(
                selected = isSelected,
                onClick = { onHourSelected(hour) },
                label = {
                    Text(
                        "$displayHour $period",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

// Selector de minutos
@Composable
fun MinuteSelector(
    minutes: List<Int>,
    selectedMinute: Int,
    onMinuteSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        minutes.forEach { minute ->
            val isSelected = minute == selectedMinute

            FilterChip(
                selected = isSelected,
                onClick = { onMinuteSelected(minute) },
                label = {
                    Text(
                        String.format("%02d", minute),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}