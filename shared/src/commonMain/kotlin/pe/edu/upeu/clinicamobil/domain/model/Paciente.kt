package pe.edu.upeu.clinicamobil.domain.model

data class Paciente(
    val id: Long = 0L,
    val nombre: String,
    val dni: String,
    val edad: Int,
    val peso: Double
) {
    init {
        require(nombre.isNotBlank()) { "El nombre es obligatorio" }
        require(dni.isNotBlank()) { "El DNI es obligatorio" }
        require(dni.matches(Regex("^[0-9]{8}$"))) { "El DNI debe tener 8 dígitos" }
        require(edad in 0..EDAD_MAXIMA) { "La edad debe estar entre 0 y 120" }
        require(!peso.isNaN() && !peso.isInfinite()) { "El peso debe ser un número válido" }
        require(peso > 0) { "El peso debe ser mayor a 0" }
    }

    /** Un paciente es pediátrico mientras no alcance la mayoría de edad. */
    val esPediatrico: Boolean
        get() = edad < EDAD_ADULTO

    companion object {
        const val EDAD_ADULTO = 18
        const val EDAD_MAXIMA = 120
    }
}
