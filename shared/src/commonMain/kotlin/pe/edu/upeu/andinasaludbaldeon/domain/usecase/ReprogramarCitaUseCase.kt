package pe.edu.upeu.andinasaludbaldeon.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.andinasaludbaldeon.domain.model.EstadoCita
import pe.edu.upeu.andinasaludbaldeon.domain.model.SolicitudCita
import pe.edu.upeu.andinasaludbaldeon.domain.repository.CitaRepository

class ReprogramarCitaUseCase(
    private val citas: CitaRepository,
    private val validar: ValidarSolicitudCitaUseCase
) {
    suspend operator fun invoke(id: Long, pacienteId: String, nuevaFecha: LocalDate, nuevaHora: LocalTime) = resultadoDe {
        citas.conAccesoExclusivo {
            val cita = citas.obtenerPorId(id)?.takeIf { it.paciente.id == pacienteId }
                ?: throw CitaNoEncontradaException()
            if (cita.estado !is EstadoCita.Programada) throw ReprogramacionNoPermitidaException()
            val otras = citas.obtenerPorPaciente(pacienteId).filterNot { it.id == id }
            val solicitud = SolicitudCita(
                pacienteId, cita.especialidad.id, cita.sede.id, nuevaFecha, nuevaHora,
                cita.motivo, cita.modalidad
            )
            val errores = validar(solicitud, otras)
            if (errores.hayErrores) throw SolicitudCitaInvalidaException(errores)
            val registro = "${cita.fecha} ${cita.hora} → $nuevaFecha $nuevaHora"
            citas.actualizar(cita.copy(
                fecha = nuevaFecha,
                hora = nuevaHora,
                historialCambios = cita.historialCambios + registro
            ))
        }
    }
}
