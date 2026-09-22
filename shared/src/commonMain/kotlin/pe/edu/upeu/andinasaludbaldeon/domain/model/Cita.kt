package pe.edu.upeu.andinasaludbaldeon.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class Cita(
    val id: Long,
    val paciente: Paciente,
    val medico: Medico,
    val sede: Sede,
    val fecha: LocalDate,
    val hora: LocalTime,
    val motivo: String,
    val estado: EstadoCita,
    val modalidad: ModalidadAtencion = ModalidadAtencion.PRESENCIAL,
    val historialCambios: List<String> = emptyList()
) {
    // La especialidad se obtiene del medico para evitar datos contradictorios.
    val especialidad: Especialidad get() = medico.especialidad
}
