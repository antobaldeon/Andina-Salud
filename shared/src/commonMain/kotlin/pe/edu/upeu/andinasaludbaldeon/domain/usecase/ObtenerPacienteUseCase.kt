package pe.edu.upeu.andinasaludbaldeon.domain.usecase

import pe.edu.upeu.andinasaludbaldeon.domain.model.Paciente
import pe.edu.upeu.andinasaludbaldeon.domain.repository.PacienteRepository

class ObtenerPacienteUseCase(private val repository: PacienteRepository) {
    suspend operator fun invoke(): Result<Paciente> = resultadoDe { repository.obtenerPaciente() }
}
