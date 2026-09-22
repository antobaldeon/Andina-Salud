package pe.edu.upeu.andinasaludbaldeon.data.local

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

class ErrorSimuladoException(mensaje: String) : IllegalStateException(mensaje)

// Permite demostrar carga y error sin usar red ni bloquear el hilo principal.
class SimuladorCarga(val retardoMillis: Long = 800L) {
    init { require(retardoMillis >= 0) }

    private val proximoError = MutableStateFlow<String?>(null)

    fun fallarProximaOperacion(mensaje: String = "No se pudieron cargar los datos. Inténtalo de nuevo.") {
        proximoError.value = mensaje
    }

    suspend fun esperar() {
        delay(retardoMillis)
        proximoError.getAndUpdate { null }?.let { throw ErrorSimuladoException(it) }
    }
}
