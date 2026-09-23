package pe.edu.upeu.clinicamobil

import kotlinx.coroutines.runBlocking
import pe.edu.upeu.clinicamobil.domain.usecase.MedicoInvalidoException
import pe.edu.upeu.clinicamobil.domain.usecase.RegistrarMedicoUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegistrarMedicoUseCaseTest {

    private val fakeRepo = FakeMedicoRepository()
    private val useCase = RegistrarMedicoUseCase(fakeRepo)

    @Test
    fun colegiatura_de_4_digitos() {
        val resultado = useCase.validar(
            nombre = "Dr. Carlos",
            colegiatura = "1234",
            especialidad = "Pediatría"
        )
        assertTrue(resultado.isFailure)
        val exception = resultado.exceptionOrNull() as MedicoInvalidoException
        assertEquals("La colegiatura debe tener entre 5 y 6 dígitos", exception.errores.colegiatura)
    }

    @Test
    fun especialidad_de_2_caracteres() {
        val resultado = useCase.validar(
            nombre = "Dra. Elena",
            colegiatura = "12345",
            especialidad = "MG"
        )
        assertTrue(resultado.isFailure)
        val exception = resultado.exceptionOrNull() as MedicoInvalidoException
        assertEquals("La especialidad debe tener al menos 3 caracteres", exception.errores.especialidad)
    }

    @Test
    fun especialidad_en_blanco_se_guarda_como_null() = runBlocking {
        val resultado = useCase(
            nombre = "Dr. Roberto",
            colegiatura = "123456",
            especialidad = "   "
        )
        assertTrue(resultado.isSuccess)
        val medico = resultado.getOrThrow()
        assertNull(medico.especialidad, "La especialidad con espacios debe normalizarse a null")
    }

    @Test
    fun acepta_medico_valido_con_especialidad() = runBlocking {
        val resultado = useCase(
            nombre = "Dra. Sofia Ramos",
            colegiatura = "54321",
            especialidad = "Cardiología"
        )
        assertTrue(resultado.isSuccess)
        val medico = resultado.getOrThrow()
        assertEquals("Dra. Sofia Ramos", medico.nombre)
        assertEquals("54321", medico.colegiatura)
        assertEquals("Cardiología", medico.especialidad)
    }
}
