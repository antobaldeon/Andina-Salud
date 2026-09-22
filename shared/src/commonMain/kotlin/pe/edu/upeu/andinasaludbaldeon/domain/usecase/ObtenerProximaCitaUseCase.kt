package pe.edu.upeu.andinasaludbaldeon.domain.usecase

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import pe.edu.upeu.andinasaludbaldeon.domain.model.*
import pe.edu.upeu.andinasaludbaldeon.domain.repository.CitaRepository
import pe.edu.upeu.andinasaludbaldeon.domain.time.Reloj

class ObtenerProximaCitaUseCase(private val repository: CitaRepository, private val reloj: Reloj) {
    suspend operator fun invoke(pacienteId: String): Result<Cita?> = resultadoDe {
        val citas = repository.obtenerPorPaciente(pacienteId)
        val ahora = reloj.ahora()
        citas.filter {
            it.estado is EstadoCita.Programada &&
                LocalDateTime(it.fecha, it.hora).toInstant(reloj.zonaHoraria) >= ahora
        }.minWithOrNull(compareBy<Cita> { it.fecha }.thenBy { it.hora })
    }
}
