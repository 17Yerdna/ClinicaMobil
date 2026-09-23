package pe.edu.upeu.clinicamobil.domain.repository

import pe.edu.upeu.clinicamobil.domain.model.Paciente

/**
 * Contrato de repositorio para la gestión del padrón de pacientes en el centro de salud universitario.
 */
interface PacienteRepository {
    /**
     * Registra un nuevo paciente en el padrón universitario.
     */
    suspend fun registrar(paciente: Paciente): Paciente

    /**
     * Obtiene el listado completo de pacientes registrados en el padrón.
     */
    suspend fun listar(): List<Paciente>
}
