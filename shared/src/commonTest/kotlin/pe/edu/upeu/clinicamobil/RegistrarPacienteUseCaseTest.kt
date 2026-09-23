package pe.edu.upeu.clinicamobil

import kotlinx.coroutines.runBlocking
import pe.edu.upeu.clinicamobil.domain.usecase.PacienteInvalidoException
import pe.edu.upeu.clinicamobil.domain.usecase.RegistrarPacienteUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RegistrarPacienteUseCaseTest {

    private val fakeRepo = FakePacienteRepository()
    private val useCase = RegistrarPacienteUseCase(fakeRepo)

    @Test
    fun acepta_un_paciente_valido() = runBlocking {
        val resultado = useCase(
            nombre = "  Carlos Mendoza  ",
            dni = "  12345678  ",
            edad = " 34 ",
            peso = " 70.5 "
        )
        assertTrue(resultado.isSuccess)
        val paciente = resultado.getOrThrow()
        assertEquals("Carlos Mendoza", paciente.nombre)
        assertEquals("12345678", paciente.dni)
        assertEquals(34, paciente.edad)
        assertEquals(70.5, paciente.peso)
    }

    @Test
    fun mensaje_nombre_obligatorio() {
        val resultado = useCase.validar(nombre = "  ", dni = "12345678", edad = "20", peso = "60.0")
        assertTrue(resultado.isFailure)
        val exception = resultado.exceptionOrNull() as PacienteInvalidoException
        assertEquals("El nombre es obligatorio", exception.errores.nombre)
    }

    @Test
    fun mensaje_dni_obligatorio() {
        val resultado = useCase.validar(nombre = "Juan", dni = "", edad = "20", peso = "60.0")
        assertTrue(resultado.isFailure)
        val exception = resultado.exceptionOrNull() as PacienteInvalidoException
        assertEquals("El DNI es obligatorio", exception.errores.dni)
    }

    @Test
    fun mensaje_dni_debe_tener_8_digitos() {
        val resultado = useCase.validar(nombre = "Juan", dni = "12345", edad = "20", peso = "60.0")
        assertTrue(resultado.isFailure)
        val exception = resultado.exceptionOrNull() as PacienteInvalidoException
        assertEquals("El DNI debe tener 8 dígitos", exception.errores.dni)
    }

    @Test
    fun mensaje_edad_obligatoria_y_entero_y_rango() {
        // Obligatoria
        val resVacio = useCase.validar(nombre = "Juan", dni = "12345678", edad = "", peso = "60.0")
        assertEquals("La edad es obligatoria", (resVacio.exceptionOrNull() as PacienteInvalidoException).errores.edad)

        // No entero
        val resNoEntero = useCase.validar(nombre = "Juan", dni = "12345678", edad = "veinte", peso = "60.0")
        assertEquals("La edad debe ser un número entero", (resNoEntero.exceptionOrNull() as PacienteInvalidoException).errores.edad)

        // Rango
        val resRango = useCase.validar(nombre = "Juan", dni = "12345678", edad = "130", peso = "60.0")
        assertEquals("La edad debe estar entre 0 y 120", (resRango.exceptionOrNull() as PacienteInvalidoException).errores.edad)
    }

    @Test
    fun mensaje_peso_obligatorio_valido_y_mayor_a_cero() {
        // Obligatorio
        val resVacio = useCase.validar(nombre = "Juan", dni = "12345678", edad = "20", peso = "")
        assertEquals("El peso es obligatorio", (resVacio.exceptionOrNull() as PacienteInvalidoException).errores.peso)

        // No válido
        val resInvalido = useCase.validar(nombre = "Juan", dni = "12345678", edad = "20", peso = "pesado")
        assertEquals("El peso debe ser un número válido", (resInvalido.exceptionOrNull() as PacienteInvalidoException).errores.peso)

        // Menor o igual a cero
        val resCero = useCase.validar(nombre = "Juan", dni = "12345678", edad = "20", peso = "0.0")
        assertEquals("El peso debe ser mayor a 0", (resCero.exceptionOrNull() as PacienteInvalidoException).errores.peso)
    }

    @Test
    fun el_id_lo_asigna_el_repositorio() = runBlocking {
        val res1 = useCase(nombre = "Paciente Uno", dni = "11112222", edad = "20", peso = "55.0")
        val res2 = useCase(nombre = "Paciente Dos", dni = "33334444", edad = "22", peso = "65.0")
        assertTrue(res1.isSuccess)
        assertTrue(res2.isSuccess)
        assertEquals(1L, res1.getOrThrow().id)
        assertEquals(2L, res2.getOrThrow().id)
    }

    @Test
    fun el_fallo_del_repositorio_llega_como_Result_failure() = runBlocking {
        fakeRepo.debeFallar = true
        fakeRepo.mensajeError = "Conexión rechazada"

        val resultado = useCase(nombre = "Paciente Error", dni = "88887777", edad = "30", peso = "70.0")
        assertTrue(resultado.isFailure)
        assertNotNull(resultado.exceptionOrNull())
        assertEquals("Conexión rechazada", resultado.exceptionOrNull()?.message)
    }
}
