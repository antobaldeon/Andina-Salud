package pe.edu.upeu.andinasaludbaldeon.presentation.inicio

import pe.edu.upeu.andinasaludbaldeon.presentation.citas.CitaUi

sealed interface InicioUiState {
    data object Cargando : InicioUiState
    data class Contenido(val nombrePaciente: String, val proximaCita: CitaUi) : InicioUiState
    data class SinCitas(val nombrePaciente: String) : InicioUiState
    data class Error(val mensaje: String) : InicioUiState
}
