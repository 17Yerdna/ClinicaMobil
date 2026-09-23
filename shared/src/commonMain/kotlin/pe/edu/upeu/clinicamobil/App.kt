package pe.edu.upeu.clinicamobil

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        Text("ClinicaMobil")
    }
}
