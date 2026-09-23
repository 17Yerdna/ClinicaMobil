package pe.edu.upeu.clinicamobil.domain.model

sealed class EstadoHistoria {
    data object Abierta : EstadoHistoria()
    data object EnTratamiento : EstadoHistoria()
    data object Alta : EstadoHistoria()
    data class Derivada(val especialidad: String) : EstadoHistoria()
}
