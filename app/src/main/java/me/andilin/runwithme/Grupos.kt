package me.andilin.runwithme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Grupos() {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var useCurrentLocation by remember { mutableStateOf(true) }
    var selectedExperience by remember { mutableStateOf("Principiante") }
    var selectedType by remember { mutableStateOf("Running") }
    var distance by remember { mutableStateOf(15f) }

    val experienceLevels = listOf("Principiante", "Intermedio", "Avanzado")
    val sportTypes = listOf("Running", "Ciclismo")

    // Definimos los colores para cada nivel
    val levelColors = mapOf(
        "Principiante" to Color(0xFF4CAF50), // Verde
        "Intermedio" to Color(0xFFFFC107),   // Amarillo
        "Avanzado" to Color(0xFF4CAF50)      // Verde
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Text(
            text = "Crear grupo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

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

        // Nivel de experiencia - ACTUALIZADO CON COLORES
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

        // Días de entrenamiento
        TrainingDaysSection()

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
            onClick = { /* Lógica para crear grupo */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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

@Composable
fun TrainingDaysSection() {
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
    val fullDays = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Días de entrenamiento | Hora de salida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    val isSelected = selectedDays.contains(fullDays[index])
                    AssistChip(
                        onClick = {
                            selectedDays = if (isSelected) {
                                selectedDays - fullDays[index]
                            } else {
                                selectedDays + fullDays[index]
                            }
                        },
                        label = { Text(day) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = if (isSelected) "Seleccionado" else "No seleccionado"
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            leadingIconContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}