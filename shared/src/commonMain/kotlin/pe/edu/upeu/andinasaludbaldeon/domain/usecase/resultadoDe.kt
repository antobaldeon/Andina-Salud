package pe.edu.upeu.andinasaludbaldeon.domain.usecase

import kotlinx.coroutines.CancellationException

internal suspend fun <T> resultadoDe(operacion: suspend () -> T): Result<T> =
    try {
        Result.success(operacion())
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (error: Exception) {
        Result.failure(error)
    }
