package pe.edu.upeu.clinicamobil.domain.usecase

import pe.edu.upeu.clinicamobil.domain.model.Medico
import pe.edu.upeu.clinicamobil.domain.repository.MedicoRepository

/**
 * Errores de validación estructurados para el formulario de médico.
 */
data class ErroresDeMedico(
    val nombre: String? = null,
    val colegiatura: String? = null,
    val especialidad: String? = null
) {
    val hayErrores: Boolean
        get() = nombre != null || colegiatura != null || especialidad != null
}

/**
 * Excepción lanzada cuando los datos del médico no cumplen las reglas del negocio.
 */
class MedicoInvalidoException(val errores: ErroresDeMedico) : IllegalArgumentException("Datos de médico inválidos")

/**
 * Caso de uso para validar y registrar un médico en el cuerpo médico del centro de salud.
 */
class RegistrarMedicoUseCase(
    private val repository: MedicoRepository
) {
    /**
     * Valida los campos según las reglas del Anexo A.2.
     * Convierte la especialidad en blanco a null.
     */
    fun validar(nombre: String, colegiatura: String, especialidad: String?): Result<Medico> {
        val nombreTrim = nombre.trim()
        val colegiaturaTrim = colegiatura.trim()
        val espTrim = especialidad?.trim()
        val especialidadFinal: String? = if (espTrim.isNullOrBlank()) null else espTrim

        var errorNombre: String? = null
        var errorColegiatura: String? = null
        var errorEspecialidad: String? = null

        // Validación de Nombre
        if (nombreTrim.isBlank()) {
            errorNombre = "El nombre es obligatorio"
        }

        // Validación de Colegiatura
        if (colegiaturaTrim.isBlank()) {
            errorColegiatura = "La colegiatura es obligatoria"
        } else if (!colegiaturaTrim.matches(Regex("^[0-9]{5,6}$"))) {
            errorColegiatura = "La colegiatura debe tener entre 5 y 6 dígitos"
        }

        // Validación de Especialidad
        if (especialidadFinal != null && especialidadFinal.length < 3) {
            errorEspecialidad = "La especialidad debe tener al menos 3 caracteres"
        }

        val errores = ErroresDeMedico(
            nombre = errorNombre,
            colegiatura = errorColegiatura,
            especialidad = errorEspecialidad
        )

        if (errores.hayErrores) {
            return Result.failure(MedicoInvalidoException(errores))
        }

        val medico = Medico(
            id = 0L,
            nombre = nombreTrim,
            colegiatura = colegiaturaTrim,
            especialidad = especialidadFinal
        )

        return Result.success(medico)
    }

    suspend operator fun invoke(nombre: String, colegiatura: String, especialidad: String?): Result<Medico> {
        val validacion = validar(nombre, colegiatura, especialidad)
        if (validacion.isFailure) {
            return validacion
        }
        val medicoParaGuardar = validacion.getOrThrow()
        return resultadoDe {
            repository.registrar(medicoParaGuardar)
        }
    }
}
