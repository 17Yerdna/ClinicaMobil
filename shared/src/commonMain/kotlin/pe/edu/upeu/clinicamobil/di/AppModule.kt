package pe.edu.upeu.clinicamobil.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import pe.edu.upeu.clinicamobil.data.repository.MedicoRepositorioEnMemoria
import pe.edu.upeu.clinicamobil.data.repository.PacienteRepositorioEnMemoria
import pe.edu.upeu.clinicamobil.domain.repository.MedicoRepository
import pe.edu.upeu.clinicamobil.domain.repository.PacienteRepository
import pe.edu.upeu.clinicamobil.domain.usecase.ListarMedicosUseCase
import pe.edu.upeu.clinicamobil.domain.usecase.ListarPacientesUseCase
import pe.edu.upeu.clinicamobil.domain.usecase.RegistrarMedicoUseCase
import pe.edu.upeu.clinicamobil.domain.usecase.RegistrarPacienteUseCase
import pe.edu.upeu.clinicamobil.presentation.medico.MedicoViewModel
import pe.edu.upeu.clinicamobil.presentation.paciente.PacienteViewModel

/**
 * Módulo de datos: Registra los repositorios enlazados a su interfaz de dominio como 'single'.
 */
val dataModule = module {
    single<PacienteRepository> { PacienteRepositorioEnMemoria() }
    single<MedicoRepository> { MedicoRepositorioEnMemoria() }
}

/**
 * Módulo de dominio: Registra los casos de uso como 'factory'.
 */
val domainModule = module {
    factoryOf(::RegistrarPacienteUseCase)
    factoryOf(::RegistrarMedicoUseCase)
    factoryOf(::ListarPacientesUseCase)
    factoryOf(::ListarMedicosUseCase)
}

/**
 * Módulo de presentación: Registra los ViewModels de la aplicación.
 */
val presentationModule = module {
    viewModelOf(::PacienteViewModel)
    viewModelOf(::MedicoViewModel)
}

/**
 * Inicialización de Koin para todas las plataformas.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(platformModule, dataModule, domainModule, presentationModule)
}
