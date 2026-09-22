package pe.edu.upeu.andinasaludbaldeon.domain.policy

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import pe.edu.upeu.andinasaludbaldeon.domain.model.Cita
import pe.edu.upeu.andinasaludbaldeon.domain.model.EstadoCita
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

object PoliticasCita {
    const val MAXIMO_PROGRAMADAS = 3
    const val MOTIVO_MINIMO = 10
    const val MOTIVO_MAXIMO = 200

    fun puedeSolicitar(citas: List<Cita>, pacienteId: String): Boolean =
        citas.count { it.paciente.id == pacienteId && it.estado is EstadoCita.Programada } < MAXIMO_PROGRAMADAS

    fun puedeCancelar(cita: Cita, ahora: Instant, zona: TimeZone): Boolean =
        cita.estado is EstadoCita.Programada &&
            LocalDateTime(cita.fecha, cita.hora).toInstant(zona) - ahora > 24.hours
}
