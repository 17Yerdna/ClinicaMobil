package pe.edu.upeu.clinicamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.clinicamobil.domain.model.Paciente
import pe.edu.upeu.clinicamobil.domain.repository.PacienteRepository
import kotlin.random.Random

/**
 * Implementación en memoria del repositorio de pacientes.
 * Asigna IDs de forma correlativa desde 1, simula latencia con delay(300..800)
 * y protege la lista contra condiciones de carrera mediante un Mutex.
 */
class PacienteRepositorioEnMemoria(
    datosIniciales: List<Paciente> = defaultPacientes
) : PacienteRepository {

    private val mutex = Mutex()
    private val padron = mutableListOf<Paciente>().apply {
        addAll(datosIniciales)
    }
    private var nextId = (datosIniciales.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun registrar(paciente: Paciente): Paciente {
        simularLatencia()
        return mutex.withLock {
            val pacienteConId = paciente.copy(id = nextId++)
            padron.add(pacienteConId)
            pacienteConId
        }
    }

    override suspend fun listar(): List<Paciente> {
        simularLatencia()
        return mutex.withLock {
            padron.toList()
        }
    }

    private suspend fun simularLatencia() {
        delay(Random.nextLong(300, 801))
    }

    companion object {
        val defaultPacientes = listOf(
            Paciente(id = 1L, nombre = "Carlos Mendoza", dni = "12345678", edad = 34, peso = 70.5),
            Paciente(id = 2L, nombre = "Ana Gómez", dni = "87654321", edad = 8, peso = 25.0)
        )
    }
}
