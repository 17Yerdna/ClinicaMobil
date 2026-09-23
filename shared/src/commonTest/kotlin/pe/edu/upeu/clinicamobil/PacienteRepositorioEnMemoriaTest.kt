package pe.edu.upeu.clinicamobil

import kotlinx.coroutines.runBlocking
import pe.edu.upeu.clinicamobil.data.repository.PacienteRepositorioEnMemoria
import pe.edu.upeu.clinicamobil.domain.model.Paciente
import kotlin.test.Test
import kotlin.test.assertEquals

class PacienteRepositorioEnMemoriaTest {

    @Test
    fun ids_correlativos() = runBlocking {
        val repo = PacienteRepositorioEnMemoria(emptyList())

        val p1 = repo.registrar(Paciente(nombre = "P1", dni = "11111111", edad = 20, peso = 60.0))
        val p2 = repo.registrar(Paciente(nombre = "P2", dni = "22222222", edad = 30, peso = 70.0))
        val p3 = repo.registrar(Paciente(nombre = "P3", dni = "33333333", edad = 40, peso = 80.0))

        assertEquals(1L, p1.id)
        assertEquals(2L, p2.id)
        assertEquals(3L, p3.id)
    }

    @Test
    fun listar_en_orden_de_registro() = runBlocking {
        val repo = PacienteRepositorioEnMemoria(emptyList())

        val p1 = repo.registrar(Paciente(nombre = "Primero", dni = "10000000", edad = 10, peso = 30.0))
        val p2 = repo.registrar(Paciente(nombre = "Segundo", dni = "20000000", edad = 20, peso = 50.0))
        val p3 = repo.registrar(Paciente(nombre = "Tercero", dni = "30000000", edad = 30, peso = 70.0))

        val lista = repo.listar()
        assertEquals(3, lista.size)
        assertEquals("Primero", lista[0].nombre)
        assertEquals("Segundo", lista[1].nombre)
        assertEquals("Tercero", lista[2].nombre)
    }
}
