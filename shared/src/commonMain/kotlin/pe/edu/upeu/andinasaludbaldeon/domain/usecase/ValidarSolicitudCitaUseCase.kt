package pe.edu.upeu.andinasaludbaldeon.domain.usecase

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import pe.edu.upeu.andinasaludbaldeon.domain.model.*
import pe.edu.upeu.andinasaludbaldeon.domain.policy.PoliticasCita
import pe.edu.upeu.andinasaludbaldeon.domain.time.Reloj

class ValidarSolicitudCitaUseCase(private val reloj: Reloj) {
    operator fun invoke(solicitud: SolicitudCita, citas: List<Cita>): ErroresSolicitudCita {
        val ahora = reloj.ahora()
        val fechaPasada = solicitud.fecha < ahora.toLocalDateTime(reloj.zonaHoraria).date
        val momentoPasado = LocalDateTime(solicitud.fecha, solicitud.hora).toInstant(reloj.zonaHoraria) < ahora
        val duplicada = citas.any {
            it.paciente.id == solicitud.pacienteId && it.estado is EstadoCita.Programada &&
                it.fecha == solicitud.fecha && it.hora == solicitud.hora
        }
        return ErroresSolicitudCita(
            paciente = if (solicitud.pacienteId.isBlank()) "El paciente es obligatorio." else null,
            especialidad = if (solicitud.especialidadId.isBlank()) "Selecciona una especialidad." else null,
            sede = if (solicitud.sedeId.isBlank()) "Selecciona una sede." else null,
            fecha = if (fechaPasada) "La fecha no puede estar en el pasado." else null,
            hora = when {
                momentoPasado && !fechaPasada -> "La hora no puede estar en el pasado."
                duplicada -> "Ya tienes una cita Programada en esa fecha y hora."
                else -> null
            },
            motivo = if (solicitud.motivo.trim().length !in PoliticasCita.MOTIVO_MINIMO..PoliticasCita.MOTIVO_MAXIMO)
                "El motivo debe tener entre 10 y 200 caracteres." else null,
            general = if (!PoliticasCita.puedeSolicitar(citas, solicitud.pacienteId))
                "No puedes tener mas de tres citas Programadas." else null
        )
    }
}
