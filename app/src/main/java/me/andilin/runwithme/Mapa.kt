package me.andilin.runwithme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mapa() {
    var selectedDistance by remember { mutableStateOf("") }
    var scheduleStart by remember { mutableStateOf("") }
    var scheduleEnd by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("") }

    val distances = listOf("0-1km", "1-5km", "6-10km", "10-20km")
    val levels = listOf("Principiante", "Medio", "Avanzado")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Mapa",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Lugares destacados
        Text(
            text = "Lugares:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
        ) {
            listOf(
                "Cairera 3.33",
                "Calle 37",
                "Calle 38",
                "Calle 41",
                "Calle 42",
                "Calle 44",
                "Parque Las Palmas",
                "Parque San Pío",
                "Hotel Dann Carlton Bucaramanga"
            ).forEach { lugar ->
                Text(
                    text = "• $lugar",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        // Filtros
        Text(
            text = "filtros:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Distancia
        Text(
            text = "Distancia:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            distances.forEach { distance ->
                FilterChip(
                    selected = selectedDistance == distance,
                    onClick = {
                        selectedDistance = if (selectedDistance == distance) "" else distance
                    },
                    label = { Text(distance) }
                )
            }
        }

        // Horario
        Text(
            text = "Horario:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = scheduleStart,
                onValueChange = { scheduleStart = it },
                placeholder = { Text("Inicio") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                singleLine = true
            )

            Text(" - ", modifier = Modifier.padding(horizontal = 4.dp))

            OutlinedTextField(
                value = scheduleEnd,
                onValueChange = { scheduleEnd = it },
                placeholder = { Text("Fin") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                singleLine = true
            )
        }

        // Nivel
        Text(
            text = "Nivel:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            levels.forEach { level ->
                FilterChip(
                    selected = selectedLevel == level,
                    onClick = {
                        selectedLevel = if (selectedLevel == level) "" else level
                    },
                    label = { Text(level) }
                )
            }
        }

        // Botón de aplicar filtros
        Button(
            onClick = {
                // Aquí iría la lógica para aplicar los filtros
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Aplicar Filtros")
        }
    }
}

// Versión alternativa con Checkbox para horario si prefieres
@Composable
fun HorarioCheckboxAlternative() {
    var horarioChecked by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Checkbox(
            checked = horarioChecked,
            onCheckedChange = { horarioChecked = it }
        )
        Text(
            text = "Horario específico",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}