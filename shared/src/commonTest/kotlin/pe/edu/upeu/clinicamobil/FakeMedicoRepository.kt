package pe.edu.upeu.clinicamobil

import pe.edu.upeu.clinicamobil.domain.model.Medico
import pe.edu.upeu.clinicamobil.domain.repository.MedicoRepository

/**
 * Repositorio de prueba para médicos sin retardos y con capacidad de fallar a voluntad.
 */
class FakeMedicoRepository(
    datosIniciales: List<Medico> = emptyList()
) : MedicoRepository {

    var debeFallar: Boolean = false
    var mensajeError: String = "Error simulado en repositorio"

    private val cuerpoMedico = mutableListOf<Medico>().apply {
        addAll(datosIniciales)
    }
    private var nextId = (datosIniciales.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun registrar(medico: Medico): Medico {
        if (debeFallar) throw RuntimeException(mensajeError)
        val conId = medico.copy(id = nextId++)
        cuerpoMedico.add(conId)
        return conId
    }

    override suspend fun listar(): List<Medico> {
        if (debeFallar) throw RuntimeException(mensajeError)
        return cuerpoMedico.toList()
    }
}
