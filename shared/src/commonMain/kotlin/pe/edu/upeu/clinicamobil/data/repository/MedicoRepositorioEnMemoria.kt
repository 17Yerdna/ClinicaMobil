package pe.edu.upeu.clinicamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.clinicamobil.domain.model.Medico
import pe.edu.upeu.clinicamobil.domain.repository.MedicoRepository
import kotlin.random.Random

/**
 * Implementación en memoria del repositorio de médicos.
 * Asigna IDs de forma correlativa desde 1, simula latencia con delay(300..800)
 * y protege la lista contra condiciones de carrera mediante un Mutex.
 */
class MedicoRepositorioEnMemoria(
    datosIniciales: List<Medico> = defaultMedicos
) : MedicoRepository {

    private val mutex = Mutex()
    private val cuerpoMedico = mutableListOf<Medico>().apply {
        addAll(datosIniciales)
    }
    private var nextId = (datosIniciales.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun registrar(medico: Medico): Medico {
        simularLatencia()
        return mutex.withLock {
            val medicoConId = medico.copy(id = nextId++)
            cuerpoMedico.add(medicoConId)
            medicoConId
        }
    }

    override suspend fun listar(): List<Medico> {
        simularLatencia()
        return mutex.withLock {
            cuerpoMedico.toList()
        }
    }

    private suspend fun simularLatencia() {
        delay(Random.nextLong(300, 801))
    }

    companion object {
        val defaultMedicos = listOf(
            Medico(id = 1L, nombre = "Dr. Roberto Silva", colegiatura = "12345", especialidad = "Cardiología"),
            Medico(id = 2L, nombre = "Dra. Elena Ramos", colegiatura = "67890", especialidad = null)
        )
    }
}
