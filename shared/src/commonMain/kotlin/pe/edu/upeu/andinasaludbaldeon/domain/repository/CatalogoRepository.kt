package pe.edu.upeu.andinasaludbaldeon.domain.repository

import pe.edu.upeu.andinasaludbaldeon.domain.model.*

interface CatalogoRepository {
    suspend fun obtenerEspecialidades(): List<Especialidad>
    suspend fun obtenerSedes(): List<Sede>
    suspend fun obtenerMedicos(): List<Medico>
}
