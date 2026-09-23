package pe.edu.upeu.clinicamobil.presentation.paciente

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.clinicamobil.presentation.components.EstadoVacio
import pe.edu.upeu.clinicamobil.presentation.components.MensajeExito
import pe.edu.upeu.clinicamobil.presentation.components.ValidatedTextField

/**
 * Pantalla principal para el registro y consulta de pacientes del padrón universitario (RF-03).
 */
@Composable
fun PacienteScreen(
    viewModel: PacienteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Formulario en Tarjeta
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Registrar Paciente",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ValidatedTextField(
                        value = uiState.formulario.nombre,
                        onValueChange = { viewModel.onNombreChange(it) },
                        label = "Nombre completo",
                        error = uiState.formulario.errorNombre,
                        enabled = !uiState.registrando
                    )

                    ValidatedTextField(
                        value = uiState.formulario.dni,
                        onValueChange = { viewModel.onDniChange(it) },
                        label = "DNI",
                        error = uiState.formulario.errorDni,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !uiState.registrando
                    )

                    // Edad y Peso lado a lado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ValidatedTextField(
                            value = uiState.formulario.edad,
                            onValueChange = { viewModel.onEdadChange(it) },
                            label = "Edad",
                            error = uiState.formulario.errorEdad,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !uiState.registrando,
                            modifier = Modifier.weight(1f)
                        )

                        ValidatedTextField(
                            value = uiState.formulario.peso,
                            onValueChange = { viewModel.onPesoChange(it) },
                            label = "Peso (kg)",
                            error = uiState.formulario.errorPeso,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            enabled = !uiState.registrando,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = { viewModel.registrar() },
                        enabled = !uiState.registrando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.registrando) "Registrando…" else "Registrar")
                    }
                }
            }
        }

        // Mensaje de éxito si existe
        if (uiState.mensajeExito != null) {
            item {
                MensajeExito(mensaje = uiState.mensajeExito!!)
            }
        }

        // Renderizado exhaustivo de las 4 fases
        when (val fase = uiState.fase) {
            is FasePacientes.Cargando -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando padrón de pacientes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is FasePacientes.SinPacientes -> {
                item {
                    EstadoVacio(
                        icono = Icons.Default.Person,
                        titulo = "Sin pacientes en el padrón",
                        descripcion = "Utilice el formulario superior para registrar el primer paciente."
                    )
                }
            }
            is FasePacientes.ConPacientes -> {
                val total = fase.pacientes.size
                val textoConteo = if (total == 1) "1 paciente" else "$total pacientes"

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Padrón de Pacientes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = textoConteo,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                items(fase.pacientes, key = { it.id }) { paciente ->
                    TarjetaPaciente(paciente = paciente)
                }
            }
            is FasePacientes.Error -> {
                item {
                    EstadoVacio(
                        icono = Icons.Default.Error,
                        titulo = fase.mensaje,
                        descripcion = "Ocurrió un error al cargar la información. Intente nuevamente.",
                        esError = true,
                        onReintentar = { viewModel.cargarPacientes() }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaPaciente(
    paciente: PacienteUi,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = paciente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (paciente.esPediatrico) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Pediátrico") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "DNI: ${paciente.dni}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = paciente.lineaSecundaria,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
