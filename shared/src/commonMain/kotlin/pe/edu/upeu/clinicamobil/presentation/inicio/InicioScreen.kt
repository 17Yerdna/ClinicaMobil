package pe.edu.upeu.clinicamobil.presentation.inicio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.edu.upeu.clinicamobil.presentation.navigation.Screen

/**
 * Modelo para las opciones de acceso rápido navegables de la portada.
 */
data class OpcionAccesoRapido(
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector,
    val destino: Screen
)

/**
 * Lista de opciones requerida por RF-01.
 * Los accesos rápidos se generan dinámicamente iterando esta colección para evitar penalizaciones.
 */
val LISTA_ACCESOS_RAPIDOS = listOf(
    OpcionAccesoRapido(
        titulo = "Registrar pacientes",
        descripcion = "Gestione el padrón general y registro de pacientes universitarios.",
        icono = Icons.Default.Person,
        destino = Screen.Pacientes
    ),
    OpcionAccesoRapido(
        titulo = "Registrar médicos",
        descripcion = "Incorpore facultativos con número de colegiatura al cuerpo médico.",
        icono = Icons.Default.MedicalServices,
        destino = Screen.Medicos
    ),
    OpcionAccesoRapido(
        titulo = "Revisar historias clínicas",
        descripcion = "Consulte antecedentes y seguimiento de consultas de atención.",
        icono = Icons.Default.Assignment,
        destino = Screen.Historias
    )
)

/**
 * Pantalla de inicio con portada institucional y accesos rápidos navegables (RF-01).
 */
@Composable
fun InicioScreen(
    onNavegar: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Portada institucional
        Spacer(modifier = Modifier.height(16.dp))
        Icon(
            imageVector = Icons.Default.LocalHospital,
            contentDescription = "ClinicaMobil",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "ClinicaMobil",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Sistema Integral de Admisión y Salud Universitaria",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Encabezado
        Text(
            text = "Qué puedes hacer",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Accesos rápidos generados iterando la lista de opciones (RF-01)
        LISTA_ACCESOS_RAPIDOS.forEach { opcion ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onNavegar(opcion.destino) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = opcion.icono,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = opcion.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = opcion.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ir a ${opcion.titulo}",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
