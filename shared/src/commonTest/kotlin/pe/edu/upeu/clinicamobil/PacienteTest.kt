package pe.edu.upeu.clinicamobil

import pe.edu.upeu.clinicamobil.domain.model.Paciente
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PacienteTest {

    @Test
    fun rechaza_nombre_vacio() {
        assertFailsWith<IllegalArgumentException> {
            Paciente(
                id = 1L,
                nombre = "   ",
                dni = "12345678",
                edad = 25,
                peso = 65.0
            )
        }
    }

    @Test
    fun rechaza_edad_fuera_de_rango() {
        // Menor a 0
        assertFailsWith<IllegalArgumentException> {
            Paciente(
                id = 1L,
                nombre = "Juan Pérez",
                dni = "12345678",
                edad = -1,
                peso = 65.0
            )
        }

        // Mayor a EDAD_MAXIMA (120)
        assertFailsWith<IllegalArgumentException> {
            Paciente(
                id = 1L,
                nombre = "Juan Pérez",
                dni = "12345678",
                edad = 121,
                peso = 65.0
            )
        }
    }

    @Test
    fun esPediatrico_cierto_con_17_y_falso_con_18() {
        val pediatrico = Paciente(
            id = 1L,
            nombre = "Niño Menor",
            dni = "87654321",
            edad = 17,
            peso = 50.0
        )
        assertTrue(pediatrico.esPediatrico, "Con 17 años debe considerarse pediátrico")

        val adulto = Paciente(
            id = 2L,
            nombre = "Mayor de Edad",
            dni = "87654322",
            edad = 18,
            peso = 60.0
        )
        assertFalse(adulto.esPediatrico, "Con 18 años ya no debe considerarse pediátrico")
    }

    @Test
    fun crea_paciente_valido_correctamente() {
        val paciente = Paciente(
            id = 10L,
            nombre = "Carlos Mendoza",
            dni = "72345678",
            edad = 34,
            peso = 70.5
        )
        assertTrue(paciente.nombre == "Carlos Mendoza")
        assertTrue(paciente.dni == "72345678")
        assertTrue(paciente.edad == 34)
        assertTrue(paciente.peso == 70.5)
    }
}
