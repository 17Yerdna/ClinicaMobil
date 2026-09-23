package pe.edu.upeu.clinicamobil.presentation.medico

import pe.edu.upeu.clinicamobil.domain.model.Medico

/**
 * Modelo de presentación adaptado para la vista de médico.
 */
data class MedicoUi(
    val id: Long,
    val nombre: String,
    val colegiatura: String,
    val especialidad: String?,
    val lineaSecundaria: String
)

/**
 * Convierte un [Medico] del dominio en su representación de presentación [MedicoUi].
 * Cumple con el Anexo B:
 * CMP <colegiatura> · <especialidad> -> CMP 12345 · Cardiología
 * Si la especialidad está ausente, se muestra "Medicina general".
 */
fun Medico.aUi(): MedicoUi {
    val esp = especialidad ?: "Medicina general"
    val lineaSecundaria = "CMP $colegiatura · $esp"
    return MedicoUi(
        id = id,
        nombre = nombre,
        colegiatura = colegiatura,
        especialidad = especialidad,
        lineaSecundaria = lineaSecundaria
    )
}
