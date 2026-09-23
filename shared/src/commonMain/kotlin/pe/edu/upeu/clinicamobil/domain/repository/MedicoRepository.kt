package pe.edu.upeu.clinicamobil.domain.repository

import pe.edu.upeu.clinicamobil.domain.model.Medico

/**
 * Contrato de repositorio para la gestión del cuerpo médico del centro de salud universitario.
 */
interface MedicoRepository {
    /**
     * Registra un nuevo profesional médico en el cuerpo médico del centro de salud.
     */
    suspend fun registrar(medico: Medico): Medico

    /**
     * Obtiene el listado de todos los facultativos que integran el cuerpo médico.
     */
    suspend fun listar(): List<Medico>
}
