package pe.edu.upeu.clinicamobil.presentation.medico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
 * Pantalla principal para el registro y consulta del cuerpo médico (RF-04).
 */
@Composable
fun MedicoScreen(
    viewModel: MedicoViewModel,
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
                        text = "Registrar Médico",
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
                        value = uiState.formulario.colegiatura,
                        onValueChange = { viewModel.onColegiaturaChange(it) },
                        label = "Número de Colegiatura (CMP)",
                        error = uiState.formulario.errorColegiatura,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !uiState.registrando
                    )

                    ValidatedTextField(
                        value = uiState.formulario.especialidad,
                        onValueChange = { viewModel.onEspecialidadChange(it) },
                        label = "Especialidad (opcional)",
                        placeholder = "Ej. Pediatría (vacío para Medicina general)",
                        error = uiState.formulario.errorEspecialidad,
                        enabled = !uiState.registrando
                    )

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
            is FaseMedicos.Cargando -> {
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
                            text = "Cargando cuerpo médico...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is FaseMedicos.SinMedicos -> {
                item {
                    EstadoVacio(
                        icono = Icons.Default.MedicalServices,
                        titulo = "Sin médicos en el cuerpo médico",
                        descripcion = "Utilice el formulario superior para registrar al primer médico."
                    )
                }
            }
            is FaseMedicos.ConMedicos -> {
                val total = fase.medicos.size
                val textoConteo = if (total == 1) "1 médico" else "$total médicos"

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cuerpo Médico",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = textoConteo,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                items(fase.medicos, key = { it.id }) { medico ->
                    TarjetaMedico(medico = medico)
                }
            }
            is FaseMedicos.Error -> {
                item {
                    EstadoVacio(
                        icono = Icons.Default.Error,
                        titulo = fase.mensaje,
                        descripcion = "Ocurrió un error al cargar los datos médicos. Intente nuevamente.",
                        esError = true,
                        onReintentar = { viewModel.cargarMedicos() }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaMedico(
    medico: MedicoUi,
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
            Text(
                text = medico.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = medico.lineaSecundaria,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
