package pe.edu.upeu.andinasaludbaldeon.domain.repository

import pe.edu.upeu.andinasaludbaldeon.domain.model.Cita

interface CitaRepository {
    suspend fun obtenerPorPaciente(pacienteId: String): List<Cita>
    suspend fun obtenerPorId(id: Long): Cita?
    // Asigna un ID unico; el ID de entrada es provisional.
    suspend fun registrar(cita: Cita): Cita
    suspend fun actualizar(cita: Cita): Cita
    // Serializa las modificaciones del repositorio, incluyendo lectura y validacion.
    // Las operaciones anteriores no deben volver a adquirir este mismo bloqueo.
    suspend fun <T> conAccesoExclusivo(operacion: suspend () -> T): T
}
