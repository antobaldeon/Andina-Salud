package pe.edu.upeu.andinasaludbaldeon.data.repository

import pe.edu.upeu.andinasaludbaldeon.data.local.CitasSimuladas
import pe.edu.upeu.andinasaludbaldeon.data.local.SimuladorCarga
import pe.edu.upeu.andinasaludbaldeon.domain.model.Paciente
import pe.edu.upeu.andinasaludbaldeon.domain.repository.PacienteRepository

class PacienteRepositoryFake(
    private val datos: CitasSimuladas,
    private val carga: SimuladorCarga
) : PacienteRepository {
    override suspend fun obtenerPaciente(): Paciente {
        carga.esperar()
        return datos.paciente
    }
}
