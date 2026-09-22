package pe.edu.upeu.andinasaludbaldeon.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.andinasaludbaldeon.data.local.SimuladorCarga
import pe.edu.upeu.andinasaludbaldeon.domain.model.Cita
import pe.edu.upeu.andinasaludbaldeon.domain.repository.CitaRepository

class CitaRepositoryFake(
    citasIniciales: List<Cita>,
    private val carga: SimuladorCarga
) : CitaRepository {
    private val citas = citasIniciales.toMutableList()
    // El primero protege lectura-validacion-escritura en los casos de uso.
    // El segundo protege la lista y los IDs sin adquirir de nuevo el primero.
    private val cambiosMutex = Mutex()
    private val datosMutex = Mutex()
    private var ultimoId = citas.maxOfOrNull { it.id } ?: 0L

    init {
        require(citas.all { it.id > 0 } && citas.map { it.id }.distinct().size == citas.size) {
            "Las citas iniciales deben tener IDs positivos y únicos."
        }
    }

    override suspend fun obtenerPorPaciente(pacienteId: String): List<Cita> {
        carga.esperar()
        return datosMutex.withLock { citas.filter { it.paciente.id == pacienteId } }
    }

    override suspend fun obtenerPorId(id: Long): Cita? {
        carga.esperar()
        return datosMutex.withLock { citas.find { it.id == id } }
    }

    override suspend fun registrar(cita: Cita): Cita {
        carga.esperar()
        return datosMutex.withLock {
            check(ultimoId < Long.MAX_VALUE) { "No hay identificadores disponibles." }
            cita.copy(id = ++ultimoId).also { citas.add(it) }
        }
    }

    override suspend fun actualizar(cita: Cita): Cita {
        carga.esperar()
        return datosMutex.withLock {
            val indice = citas.indexOfFirst { it.id == cita.id }
            check(indice >= 0) { "La cita no existe." }
            citas[indice] = cita
            cita
        }
    }

    override suspend fun <T> conAccesoExclusivo(operacion: suspend () -> T): T =
        cambiosMutex.withLock { operacion() }
}
