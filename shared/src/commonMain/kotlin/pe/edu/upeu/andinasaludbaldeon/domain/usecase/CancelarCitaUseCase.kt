package pe.edu.upeu.andinasaludbaldeon.domain.usecase

import pe.edu.upeu.andinasaludbaldeon.domain.model.*
import pe.edu.upeu.andinasaludbaldeon.domain.policy.PoliticasCita
import pe.edu.upeu.andinasaludbaldeon.domain.repository.CitaRepository
import pe.edu.upeu.andinasaludbaldeon.domain.time.Reloj

class CancelarCitaUseCase(private val repository: CitaRepository, private val reloj: Reloj) {
    suspend operator fun invoke(
        id: Long,
        pacienteId: String,
        motivo: String = "Cancelada por el paciente"
    ): Result<Cita> = resultadoDe {
        repository.conAccesoExclusivo {
            val cita = repository.obtenerPorId(id)?.takeIf { it.paciente.id == pacienteId }
                ?: throw CitaNoEncontradaException()
            if (!PoliticasCita.puedeCancelar(cita, reloj.ahora(), reloj.zonaHoraria))
                throw CancelacionNoPermitidaException()
            repository.actualizar(
                cita.copy(estado = EstadoCita.Cancelada(
                    motivo.trim().ifBlank { "Cancelada por el paciente" },
                    canceladaPorPaciente = true
                ))
            )
        }
    }
}
