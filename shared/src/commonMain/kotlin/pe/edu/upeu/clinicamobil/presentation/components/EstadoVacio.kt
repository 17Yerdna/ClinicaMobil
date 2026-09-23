package pe.edu.upeu.clinicamobil.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Componente reutilizable para representar estados vacíos, informativos o de error con botón de reintento.
 */
@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    descripcion: String? = null,
    modifier: Modifier = Modifier,
    esError: Boolean = false,
    onReintentar: (() -> Unit)? = null
) {
    val colorIcono = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val colorTexto = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val colorSecundario = if (esError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = colorIcono,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = colorTexto,
            textAlign = TextAlign.Center
        )
        if (descripcion != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = colorSecundario,
                textAlign = TextAlign.Center
            )
        }
        if (onReintentar != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onReintentar,
                colors = if (esError) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text("Reintentar")
            }
        }
    }
}
