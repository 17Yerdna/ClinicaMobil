package pe.edu.upeu.clinicamobil.domain.usecase

import pe.edu.upeu.clinicamobil.domain.model.Medico
import pe.edu.upeu.clinicamobil.domain.repository.MedicoRepository

/**
 * Caso de uso para obtener el listado de los médicos que integran el cuerpo médico.
 */
class ListarMedicosUseCase(
    private val repository: MedicoRepository
) {
    suspend operator fun invoke(): Result<List<Medico>> = resultadoDe {
        repository.listar()
    }
}
