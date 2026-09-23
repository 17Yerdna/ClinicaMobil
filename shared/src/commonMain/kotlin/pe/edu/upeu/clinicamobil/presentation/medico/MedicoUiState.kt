package pe.edu.upeu.clinicamobil.presentation.medico

/**
 * Fases excluyentes del estado del cuerpo médico.
 */
sealed interface FaseMedicos {
    data object Cargando : FaseMedicos
    data object SinMedicos : FaseMedicos
    data class ConMedicos(val medicos: List<MedicoUi>) : FaseMedicos
    data class Error(val mensaje: String) : FaseMedicos
}

/**
 * Estado reactivo del formulario de médico con sus valores y errores.
 */
data class FormularioMedico(
    val nombre: String = "",
    val colegiatura: String = "",
    val especialidad: String = "",
    val errorNombre: String? = null,
    val errorColegiatura: String? = null,
    val errorEspecialidad: String? = null
)

/**
 * Estado global de la interfaz de usuario para el módulo de médicos.
 */
data class MedicoUiState(
    val fase: FaseMedicos = FaseMedicos.Cargando,
    val formulario: FormularioMedico = FormularioMedico(),
    val registrando: Boolean = false,
    val mensajeExito: String? = null
)
