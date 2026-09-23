package pe.edu.upeu.clinicamobil.domain.model

data class HistoriaClinica(
    val id: Long = 0L,
    val paciente: Paciente,
    val atenciones: List<Atencion> = emptyList(),
    val estado: EstadoHistoria = EstadoHistoria.Abierta
)
