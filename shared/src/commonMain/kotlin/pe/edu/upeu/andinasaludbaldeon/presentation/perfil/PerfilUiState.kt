package pe.edu.upeu.andinasaludbaldeon.presentation.perfil

import pe.edu.upeu.andinasaludbaldeon.domain.model.Paciente

sealed interface PerfilUiState {
    data object Cargando : PerfilUiState
    data class Contenido(val paciente: Paciente) : PerfilUiState
    data class Error(val mensaje: String) : PerfilUiState
}
