package pe.edu.upeu.clinicamobil

import pe.edu.upeu.clinicamobil.domain.model.Paciente
import pe.edu.upeu.clinicamobil.domain.repository.PacienteRepository

/**
 * Repositorio de prueba para pacientes sin retardos y con capacidad de fallar a voluntad.
 */
class FakePacienteRepository(
    datosIniciales: List<Paciente> = emptyList()
) : PacienteRepository {

    var debeFallar: Boolean = false
    var mensajeError: String = "Error simulado en repositorio"

    private val padron = mutableListOf<Paciente>().apply {
        addAll(datosIniciales)
    }
    private var nextId = (datosIniciales.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun registrar(paciente: Paciente): Paciente {
        if (debeFallar) throw RuntimeException(mensajeError)
        val conId = paciente.copy(id = nextId++)
        padron.add(conId)
        return conId
    }

    override suspend fun listar(): List<Paciente> {
        if (debeFallar) throw RuntimeException(mensajeError)
        return padron.toList()
    }
}
