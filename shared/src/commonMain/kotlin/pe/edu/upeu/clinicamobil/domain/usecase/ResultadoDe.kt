package pe.edu.upeu.clinicamobil.domain.usecase

import kotlin.coroutines.cancellation.CancellationException

/**
 * Helper de ejecución segura que captura excepciones generales retornando [Result.failure],
 * pero relanza explícitamente [CancellationException] para respetar la cancelación de corrutinas.
 */
inline fun <T> resultadoDe(bloque: () -> T): Result<T> = try {
    Result.success(bloque())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(e)
}
