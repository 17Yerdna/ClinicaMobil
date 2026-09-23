package pe.edu.upeu.clinicamobil.domain.usecase

import pe.edu.upeu.clinicamobil.domain.model.Paciente
import pe.edu.upeu.clinicamobil.domain.repository.PacienteRepository

/**
 * Errores de validación estructurados para los cuatro campos del formulario de paciente.
 */
data class ErroresDePaciente(
    val nombre: String? = null,
    val dni: String? = null,
    val edad: String? = null,
    val peso: String? = null
) {
    val hayErrores: Boolean
        get() = nombre != null || dni != null || edad != null || peso != null
}

/**
 * Excepción lanzada cuando los datos del paciente no cumplen las reglas del negocio.
 */
class PacienteInvalidoException(val errores: ErroresDePaciente) : IllegalArgumentException("Datos de paciente inválidos")

/**
 * Caso de uso para validar y registrar un paciente en el padrón del centro de salud.
 */
class RegistrarPacienteUseCase(
    private val repository: PacienteRepository
) {
    /**
     * Valida los cuatro campos según las reglas del Anexo A.
     */
    fun validar(nombre: String, dni: String, edad: String, peso: String): Result<Paciente> {
        val nombreTrim = nombre.trim()
        val dniTrim = dni.trim()
        val edadTrim = edad.trim()
        val pesoTrim = peso.trim()

        var errorNombre: String? = null
        var errorDni: String? = null
        var errorEdad: String? = null
        var errorPeso: String? = null

        // Validación de Nombre
        if (nombreTrim.isBlank()) {
            errorNombre = "El nombre es obligatorio"
        }

        // Validación de DNI
        if (dniTrim.isBlank()) {
            errorDni = "El DNI es obligatorio"
        } else if (!dniTrim.matches(Regex("^[0-9]{8}$"))) {
            errorDni = "El DNI debe tener 8 dígitos"
        }

        // Validación de Edad
        var edadEntero = 0
        if (edadTrim.isBlank()) {
            errorEdad = "La edad es obligatoria"
        } else {
            val parseado = edadTrim.toIntOrNull()
            if (parseado == null) {
                errorEdad = "La edad debe ser un número entero"
            } else if (parseado !in 0..Paciente.EDAD_MAXIMA) {
                errorEdad = "La edad debe estar entre 0 y 120"
            } else {
                edadEntero = parseado
            }
        }

        // Validación de Peso
        var pesoNumero = 0.0
        if (pesoTrim.isBlank()) {
            errorPeso = "El peso es obligatorio"
        } else {
            val parseado = pesoTrim.toDoubleOrNull()
            if (parseado == null || parseado.isNaN() || parseado.isInfinite()) {
                errorPeso = "El peso debe ser un número válido"
            } else if (parseado <= 0.0) {
                errorPeso = "El peso debe ser mayor a 0"
            } else {
                pesoNumero = parseado
            }
        }

        val errores = ErroresDePaciente(
            nombre = errorNombre,
            dni = errorDni,
            edad = errorEdad,
            peso = errorPeso
        )

        if (errores.hayErrores) {
            return Result.failure(PacienteInvalidoException(errores))
        }

        val paciente = Paciente(
            id = 0L,
            nombre = nombreTrim,
            dni = dniTrim,
            edad = edadEntero,
            peso = pesoNumero
        )

        return Result.success(paciente)
    }

    suspend operator fun invoke(nombre: String, dni: String, edad: String, peso: String): Result<Paciente> {
        val validacion = validar(nombre, dni, edad, peso)
        if (validacion.isFailure) {
            return validacion
        }
        val pacienteParaGuardar = validacion.getOrThrow()
        return resultadoDe {
            repository.registrar(pacienteParaGuardar)
        }
    }
}
