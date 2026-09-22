package pe.edu.upeu.andinasaludbaldeon.domain.usecase

import pe.edu.upeu.andinasaludbaldeon.domain.model.Cita
import pe.edu.upeu.andinasaludbaldeon.domain.repository.CitaRepository

class ObtenerDetalleCitaUseCase(private val repository: CitaRepository) {
    suspend operator fun invoke(id: Long, pacienteId: String): Result<Cita> = resultadoDe {
        repository.obtenerPorId(id)?.takeIf { it.paciente.id == pacienteId }
            ?: throw CitaNoEncontradaException()
    }
}
