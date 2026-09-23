package pe.edu.upeu.clinicamobil.presentation.navigation

import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destinos tipados de la aplicación ClinicaMobil.
 */
sealed class Screen(val ruta: String, val titulo: String, val icono: ImageVector) {
    data object Inicio : Screen("inicio", "Inicio", Icons.Default.Home)
    data object Pacientes : Screen("pacientes", "Pacientes", Icons.Default.Person)
    data object Medicos : Screen("medicos", "Médicos", Icons.Default.MedicalServices)
    data object Historias : Screen("historias", "Historias Clínicas", Icons.AutoMirrored.Filled.Assignment)

    companion object {
        /**
         * Fuente única de verdad que alimenta la barra superior y el menú lateral.
         */
        val DESTINOS: List<Screen> = listOf(Inicio, Pacientes, Medicos, Historias)

        fun desdeRuta(ruta: String?): Screen = when (ruta) {
            Pacientes.ruta -> Pacientes
            Medicos.ruta -> Medicos
            Historias.ruta -> Historias
            else -> Inicio
        }
    }
}

/**
 * Saver para que la pantalla actual sobreviva a cambios de configuración (rotación de pantalla).
 */
val ScreenSaver: Saver<Screen, String> = Saver(
    save = { it.ruta },
    restore = { Screen.desdeRuta(it) }
)
