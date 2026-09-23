package pe.edu.upeu.clinicamobil.domain.model

data class Medico(
    val id: Long = 0L,
    val nombre: String,
    val colegiatura: String,
    val especialidad: String? = null
) {
    init {
        require(nombre.isNotBlank()) { "El nombre es obligatorio" }
        require(colegiatura.isNotBlank()) { "La colegiatura es obligatoria" }
        require(colegiatura.matches(Regex("^[0-9]{5,6}$"))) { "La colegiatura debe tener entre 5 y 6 dígitos" }
        require(especialidad == null || (especialidad.isNotBlank() && especialidad.trim().length >= 3)) {
            "La especialidad debe tener al menos 3 caracteres"
        }
    }
}
