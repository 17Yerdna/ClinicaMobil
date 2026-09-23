package pe.edu.upeu.clinicamobil.presentation.medico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.clinicamobil.domain.usecase.ListarMedicosUseCase
import pe.edu.upeu.clinicamobil.domain.usecase.MedicoInvalidoException
import pe.edu.upeu.clinicamobil.domain.usecase.RegistrarMedicoUseCase

/**
 * ViewModel encargado de la lógica de presentación del cuerpo médico.
 */
class MedicoViewModel(
    private val registrarMedico: RegistrarMedicoUseCase,
    private val listarMedicos: ListarMedicosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicoUiState())
    val uiState: StateFlow<MedicoUiState> = _uiState.asStateFlow()

    init {
        cargarMedicos()
    }

    fun cargarMedicos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = FaseMedicos.Cargando) }
            val resultado = listarMedicos()
            resultado.onSuccess { lista ->
                _uiState.update {
                    it.copy(
                        fase = if (lista.isEmpty()) {
                            FaseMedicos.SinMedicos
                        } else {
                            FaseMedicos.ConMedicos(lista.map { medico -> medico.aUi() })
                        }
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        fase = FaseMedicos.Error(
                            error.message ?: "No se pudo cargar el cuerpo médico"
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

    fun onColegiaturaChange(nuevo: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    colegiatura = nuevo,
                    errorColegiatura = null
                )
            )
        }
    }

    fun onEspecialidadChange(nuevo: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    especialidad = nuevo,
                    errorEspecialidad = null
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
            val resultado = registrarMedico(
                nombre = form.nombre,
                colegiatura = form.colegiatura,
                especialidad = form.especialidad
            )

            resultado.onSuccess { nuevo ->
                _uiState.update {
                    it.copy(
                        formulario = FormularioMedico(),
                        registrando = false,
                        mensajeExito = "Médico \"${nuevo.nombre}\" registrado correctamente"
                    )
                }
                cargarMedicos()
            }.onFailure { error ->
                if (error is MedicoInvalidoException) {
                    _uiState.update {
                        it.copy(
                            registrando = false,
                            formulario = it.formulario.copy(
                                errorNombre = error.errores.nombre,
                                errorColegiatura = error.errores.colegiatura,
                                errorEspecialidad = error.errores.especialidad
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            registrando = false,
                            fase = FaseMedicos.Error(
                                error.message ?: "No se pudo registrar el médico"
                            )
                        )
                    }
                }
            }
        }
    }
}
