package pe.edu.upeu.clinicamobil.presentation.paciente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.clinicamobil.domain.usecase.ListarPacientesUseCase
import pe.edu.upeu.clinicamobil.domain.usecase.PacienteInvalidoException
import pe.edu.upeu.clinicamobil.domain.usecase.RegistrarPacienteUseCase

/**
 * ViewModel encargado de la lógica de presentación del padrón de pacientes.
 */
class PacienteViewModel(
    private val registrarPaciente: RegistrarPacienteUseCase,
    private val listarPacientes: ListarPacientesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PacienteUiState())
    val uiState: StateFlow<PacienteUiState> = _uiState.asStateFlow()

    init {
        cargarPacientes()
    }

    fun cargarPacientes() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = FasePacientes.Cargando) }
            val resultado = listarPacientes()
            resultado.onSuccess { lista ->
                _uiState.update {
                    it.copy(
                        fase = if (lista.isEmpty()) {
                            FasePacientes.SinPacientes
                        } else {
                            FasePacientes.ConPacientes(lista.map { paciente -> paciente.aUi() })
                        }
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        fase = FasePacientes.Error(
                            error.message ?: "No se pudo cargar el padrón de pacientes"
                        )
                    )
                }
            }
        }
    }

    fun onNombreChange(nuevo: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    nombre = nuevo,
                    errorNombre = null
                )
            )
        }
    }

    fun onDniChange(nuevo: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    dni = nuevo,
                    errorDni = null
                )
            )
        }
    }

    fun onEdadChange(nuevo: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    edad = nuevo,
                    errorEdad = null
                )
            )
        }
    }

    fun onPesoChange(nuevo: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    peso = nuevo,
                    errorPeso = null
                )
            )
        }
    }

    fun registrar() {
        // Prevención estricta de doble toque
        if (_uiState.value.registrando) return

        val form = _uiState.value.formulario
        viewModelScope.launch {
            _uiState.update { it.copy(registrando = true, mensajeExito = null) }
            val resultado = registrarPaciente(
                nombre = form.nombre,
                dni = form.dni,
                edad = form.edad,
                peso = form.peso
            )

            resultado.onSuccess { nuevo ->
                _uiState.update {
                    it.copy(
                        formulario = FormularioPaciente(),
                        registrando = false,
                        mensajeExito = "Paciente \"${nuevo.nombre}\" registrado correctamente"
                    )
                }
                cargarPacientes()
            }.onFailure { error ->
                if (error is PacienteInvalidoException) {
                    // Los errores de validación caen en el formulario sin volver a validar en el ViewModel
                    _uiState.update {
                        it.copy(
                            registrando = false,
                            formulario = it.formulario.copy(
                                errorNombre = error.errores.nombre,
                                errorDni = error.errores.dni,
                                errorEdad = error.errores.edad,
                                errorPeso = error.errores.peso
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            registrando = false,
                            fase = FasePacientes.Error(
                                error.message ?: "No se pudo registrar el paciente"
                            )
                        )
                    }
                }
            }
        }
    }
}
