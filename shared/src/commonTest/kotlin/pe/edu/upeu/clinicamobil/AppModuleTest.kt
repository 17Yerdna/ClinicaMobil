package pe.edu.upeu.clinicamobil

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import pe.edu.upeu.clinicamobil.data.repository.PacienteRepositorioEnMemoria
import pe.edu.upeu.clinicamobil.di.dataModule
import pe.edu.upeu.clinicamobil.di.domainModule
import pe.edu.upeu.clinicamobil.di.presentationModule
import pe.edu.upeu.clinicamobil.domain.repository.PacienteRepository
import pe.edu.upeu.clinicamobil.domain.usecase.ListarMedicosUseCase
import pe.edu.upeu.clinicamobil.domain.usecase.ListarPacientesUseCase
import pe.edu.upeu.clinicamobil.domain.usecase.RegistrarMedicoUseCase
import pe.edu.upeu.clinicamobil.domain.usecase.RegistrarPacienteUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AppModuleTest {

    @BeforeTest
    fun setUp() {
        stopKoin()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun resuelve_PacienteRepository_como_PacienteRepositorioEnMemoria() {
        val app = startKoin {
            modules(dataModule, domainModule, presentationModule)
        }
        val repo = app.koin.get<PacienteRepository>()
        assertNotNull(repo)
        assertTrue(
            repo is PacienteRepositorioEnMemoria,
            "El repositorio debe resolverse como PacienteRepositorioEnMemoria"
        )
    }

    @Test
    fun el_repositorio_es_unico() {
        val app = startKoin {
            modules(dataModule, domainModule, presentationModule)
        }
        val repo1 = app.koin.get<PacienteRepository>()
        val repo2 = app.koin.get<PacienteRepository>()

        // Verificar que sea singleton (single)
        assertSame(repo1, repo2, "El repositorio debe ser único (instancia singleton)")
    }

    @Test
    fun resuelve_los_cuatro_casos_de_uso() {
        val app = startKoin {
            modules(dataModule, domainModule, presentationModule)
        }
        val registrarPaciente = app.koin.get<RegistrarPacienteUseCase>()
        val registrarMedico = app.koin.get<RegistrarMedicoUseCase>()
        val listarPacientes = app.koin.get<ListarPacientesUseCase>()
        val listarMedicos = app.koin.get<ListarMedicosUseCase>()

        assertNotNull(registrarPaciente, "Debe resolver RegistrarPacienteUseCase")
        assertNotNull(registrarMedico, "Debe resolver RegistrarMedicoUseCase")
        assertNotNull(listarPacientes, "Debe resolver ListarPacientesUseCase")
        assertNotNull(listarMedicos, "Debe resolver ListarMedicosUseCase")
    }
}
