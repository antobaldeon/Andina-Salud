package pe.edu.upeu.andinasaludbaldeon.domain.repository

import pe.edu.upeu.andinasaludbaldeon.domain.model.Paciente

interface PacienteRepository {
    suspend fun obtenerPaciente(): Paciente
}
