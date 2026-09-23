package pe.edu.upeu.clinicamobil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.clinicamobil.domain.model.Paciente
import pe.edu.upeu.clinicamobil.domain.usecase.ListarPacientesUseCase
import pe.edu.upeu.clinicamobil.domain.usecase.RegistrarPacienteUseCase
import pe.edu.upeu.clinicamobil.presentation.paciente.FasePacientes
import pe.edu.upeu.clinicamobil.presentation.paciente.PacienteViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PacienteViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun arranca_en_SinPacientes() {
        val fakeRepo = FakePacienteRepository(emptyList())
        val viewModel = PacienteViewModel(
            registrarPaciente = RegistrarPacienteUseCase(fakeRepo),
            listarPacientes = ListarPacientesUseCase(fakeRepo)
        )

        val fase = viewModel.uiState.value.fase
        assertTrue(fase is FasePacientes.SinPacientes, "Debe iniciar en SinPacientes si el repositorio está vacío")
    }

    @Test
    fun muestra_34_anos_70_5_kg() {
        val inicial = listOf(
            Paciente(id = 1L, nombre = "Carlos Mendoza", dni = "12345678", edad = 34, peso = 70.5)
        )
        val fakeRepo = FakePacienteRepository(inicial)
        val viewModel = PacienteViewModel(
            registrarPaciente = RegistrarPacienteUseCase(fakeRepo),
            listarPacientes = ListarPacientesUseCase(fakeRepo)
        )

        val fase = viewModel.uiState.value.fase
        assertTrue(fase is FasePacientes.ConPacientes)
        val pacienteUi = fase.pacientes.first()
        assertEquals("34 años · 70.5 kg", pacienteUi.lineaSecundaria)
    }

    @Test
    fun pasa_a_Error_si_el_repositorio_falla() {
        val fakeRepo = FakePacienteRepository().apply {
            debeFallar = true
            mensajeError = "Fallo de conexión en padrón"
        }
        val viewModel = PacienteViewModel(
            registrarPaciente = RegistrarPacienteUseCase(fakeRepo),
            listarPacientes = ListarPacientesUseCase(fakeRepo)
        )

        val fase = viewModel.uiState.value.fase
        assertTrue(fase is FasePacientes.Error)
        assertEquals("Fallo de conexión en padrón", fase.mensaje)
    }

    @Test
    fun errores_de_validacion_caen_en_el_formulario_no_en_la_fase() {
        val fakeRepo = FakePacienteRepository(emptyList())
        val viewModel = PacienteViewModel(
            registrarPaciente = RegistrarPacienteUseCase(fakeRepo),
            listarPacientes = ListarPacientesUseCase(fakeRepo)
        )

        // Intentar registrar con campos vacíos
        viewModel.registrar()

        val state = viewModel.uiState.value
        // La fase debe seguir siendo SinPacientes, no Error
        assertTrue(state.fase is FasePacientes.SinPacientes)

        // Los errores deben reflejarse en el formulario
        assertEquals("El nombre es obligatorio", state.formulario.errorNombre)
        assertEquals("El DNI es obligatorio", state.formulario.errorDni)
        assertEquals("La edad es obligatoria", state.formulario.errorEdad)
        assertEquals("El peso es obligatorio", state.formulario.errorPeso)
    }

    @Test
    fun registrar_limpia_el_formulario_y_recarga() {
        val fakeRepo = FakePacienteRepository(emptyList())
        val viewModel = PacienteViewModel(
            registrarPaciente = RegistrarPacienteUseCase(fakeRepo),
            listarPacientes = ListarPacientesUseCase(fakeRepo)
        )

        viewModel.onNombreChange("Ana Gómez")
        viewModel.onDniChange("87654321")
        viewModel.onEdadChange("8")
        viewModel.onPesoChange("25.0")

        viewModel.registrar()

        val state = viewModel.uiState.value
        // El formulario debe estar limpio
        assertEquals("", state.formulario.nombre)
        assertEquals("", state.formulario.dni)
        assertEquals("", state.formulario.edad)
        assertEquals("", state.formulario.peso)
        assertNull(state.formulario.errorNombre)

        // Mensaje de éxito visible
        assertNotNull(state.mensajeExito)
        assertEquals("Paciente \"Ana Gómez\" registrado correctamente", state.mensajeExito)

        // Debe haber recargado pasando a ConPacientes
        assertTrue(state.fase is FasePacientes.ConPacientes)
        assertEquals(1, state.fase.pacientes.size)
        assertEquals("Ana Gómez", state.fase.pacientes.first().nombre)
    }
}
