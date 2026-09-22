package pe.edu.upeu.andinasaludbaldeon.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

// El medico se asignara en el caso de uso segun la especialidad y la sede.
data class SolicitudCita(
    val pacienteId: String,
    val especialidadId: String,
    val sedeId: String,
    val fecha: LocalDate,
    val hora: LocalTime,
    val motivo: String
)
