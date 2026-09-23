package pe.edu.upeu.clinicamobil.presentation.paciente

import pe.edu.upeu.clinicamobil.domain.model.Paciente
import kotlin.math.abs

/**
 * Modelo de presentación adaptado para la vista de paciente.
 */
data class PacienteUi(
    val id: Long,
    val nombre: String,
    val dni: String,
    val edad: Int,
    val peso: Double,
    val lineaSecundaria: String,
    val esPediatrico: Boolean
)

/**
 * Formatea un valor numérico decimal a exactamente un decimal sin utilizar String.format
 * (garantizando compatibilidad multiplataforma en commonMain).
 */
fun formatearUnDecimal(valor: Double): String {
    val totalDecimas = (valor * 10.0 + if (valor >= 0) 0.5 else -0.5).toLong()
    val parteEntera = totalDecimas / 10
    val parteDecimal = abs(totalDecimas % 10)
    return "$parteEntera.$parteDecimal"
}

/**
 * Convierte un [Paciente] del dominio en su representación de presentación [PacienteUi].
 * Cumple con el Anexo B:
 * - Plural: 34 años · 70.5 kg
 * - Singular: 1 año · 9.2 kg
 * - Decimal siempre presente: 70.0 kg
 */
fun Paciente.aUi(): PacienteUi {
    val textoEdad = if (edad == 1) "1 año" else "$edad años"
    val lineaSecundaria = "$textoEdad · ${formatearUnDecimal(peso)} kg"
    return PacienteUi(
        id = id,
        nombre = nombre,
        dni = dni,
        edad = edad,
        peso = peso,
        lineaSecundaria = lineaSecundaria,
        esPediatrico = esPediatrico
    )
}
