package pe.edu.upeu.clinicamobil.presentation.paciente

/**
 * Fases excluyentes del estado del padrón de pacientes.
 */
sealed interface FasePacientes {
    data object Cargando : FasePacientes
    data object SinPacientes : FasePacientes
    data class ConPacientes(val pacientes: List<PacienteUi>) : FasePacientes
    data class Error(val mensaje: String) : FasePacientes
}

/**
 * Estado reactivo del formulario de registro de paciente con sus 4 valores y 4 errores.
 */
data class FormularioPaciente(
    val nombre: String = "",
    val dni: String = "",
    val edad: String = "",
    val peso: String = "",
    val errorNombre: String? = null,
    val errorDni: String? = null,
    val errorEdad: String? = null,
    val errorPeso: String? = null
)

/**
 * Estado global de la interfaz de usuario para el módulo de pacientes.
 */
data class PacienteUiState(
    val fase: FasePacientes = FasePacientes.Cargando,
    val formulario: FormularioPaciente = FormularioPaciente(),
    val registrando: Boolean = false,
    val mensajeExito: String? = null
)
