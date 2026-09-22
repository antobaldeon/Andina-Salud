package pe.edu.upeu.andinasaludbaldeon.data.repository

import pe.edu.upeu.andinasaludbaldeon.data.local.CitasSimuladas
import pe.edu.upeu.andinasaludbaldeon.data.local.SimuladorCarga
import pe.edu.upeu.andinasaludbaldeon.domain.model.*
import pe.edu.upeu.andinasaludbaldeon.domain.repository.CatalogoRepository

class CatalogoRepositoryFake(
    private val datos: CitasSimuladas,
    private val carga: SimuladorCarga
) : CatalogoRepository {
    override suspend fun obtenerEspecialidades(): List<Especialidad> {
        carga.esperar()
        return datos.especialidades.toList()
    }

    override suspend fun obtenerSedes(): List<Sede> {
        carga.esperar()
        return datos.sedes.toList()
    }

    override suspend fun obtenerMedicos(): List<Medico> {
        carga.esperar()
        return datos.medicos.toList()
    }
}
