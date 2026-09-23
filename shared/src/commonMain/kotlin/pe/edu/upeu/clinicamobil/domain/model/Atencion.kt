package pe.edu.upeu.clinicamobil.domain.model

data class Atencion(
    val medico: Medico,
    val diagnostico: String,
    val duracionMinutos: Int
) {
    init {
        require(diagnostico.isNotBlank()) { "El diagnóstico no puede estar vacío" }
        require(duracionMinutos in 1..DURACION_MAXIMA) { "La atención debe durar entre 1 y 120 minutos" }
    }

    /** Costo de la atención según el tiempo de consultorio utilizado. */
    fun costo(): Double = duracionMinutos * TARIFA_POR_MINUTO

    companion object {
        const val DURACION_MAXIMA = 120
        const val TARIFA_POR_MINUTO = 2.50
    }
}
