package pe.edu.upeu.andinasaludbaldeon.presentation.detalle

import pe.edu.upeu.andinasaludbaldeon.domain.model.Cita

sealed interface DetalleCitaUiState {
    data object Cargando : DetalleCitaUiState
    data class Contenido(val cita: Cita, val mensaje: String? = null) : DetalleCitaUiState
    data class Error(val mensaje: String) : DetalleCitaUiState
}
