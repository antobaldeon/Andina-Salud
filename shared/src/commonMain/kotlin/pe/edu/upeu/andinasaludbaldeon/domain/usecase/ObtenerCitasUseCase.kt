package pe.edu.upeu.andinasaludbaldeon.domain.usecase

import pe.edu.upeu.andinasaludbaldeon.domain.model.Cita
import pe.edu.upeu.andinasaludbaldeon.domain.repository.CitaRepository

class ObtenerCitasUseCase(private val repository: CitaRepository) {
    suspend operator fun invoke(pacienteId: String): Result<List<Cita>> = resultadoDe {
        repository.obtenerPorPaciente(pacienteId).sortedWith(compareBy<Cita> { it.fecha }.thenBy { it.hora })
    }
}
