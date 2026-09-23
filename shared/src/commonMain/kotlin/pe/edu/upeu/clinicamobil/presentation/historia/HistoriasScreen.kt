package pe.edu.upeu.clinicamobil.presentation.historia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.clinicamobil.presentation.components.EstadoVacio

/**
 * Pantalla de historias clínicas que presenta un estado de construcción (RF-05).
 */
@Composable
fun HistoriasScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        EstadoVacio(
            icono = Icons.AutoMirrored.Filled.Assignment,
            titulo = "Historias clínicas en construcción",
            descripcion = "El módulo de historias clínicas se integrará con el servidor central en el próximo semestre académico."
        )
    }
}
