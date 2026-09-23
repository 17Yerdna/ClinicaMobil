package pe.edu.upeu.clinicamobil

import pe.edu.upeu.clinicamobil.domain.model.Atencion
import pe.edu.upeu.clinicamobil.domain.model.Medico
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AtencionTest {

    private val medicoPrueba = Medico(
        id = 1L,
        nombre = "Dr. Silva",
        colegiatura = "12345",
        especialidad = "Medicina general"
    )

    @Test
    fun rechaza_0_minutos() {
        assertFailsWith<IllegalArgumentException> {
            Atencion(
                medico = medicoPrueba,
                diagnostico = "Consulta general",
                duracionMinutos = 0
            )
        }
    }

    @Test
    fun rechaza_121_minutos() {
        assertFailsWith<IllegalArgumentException> {
            Atencion(
                medico = medicoPrueba,
                diagnostico = "Consulta extensa",
                duracionMinutos = 121
            )
        }
    }

    @Test
    fun costo_de_30_minutos_es_75_0() {
        val atencion = Atencion(
            medico = medicoPrueba,
            diagnostico = "Chequeo preventivo",
            duracionMinutos = 30
        )
        // Tarifa por minuto es 2.50 -> 30 * 2.50 = 75.0
        assertEquals(75.0, atencion.costo())
    }
}
