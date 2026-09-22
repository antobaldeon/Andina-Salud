package pe.edu.upeu.andinasaludbaldeon.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.andinasaludbaldeon.domain.model.EstadoCita
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.CancelarCitaUseCase
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.ObtenerDetalleCitaUseCase
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.ObtenerPacienteUseCase

class DetalleCitaViewModel(
    private val paciente: ObtenerPacienteUseCase,
    private val detalle: ObtenerDetalleCitaUseCase,
    private val cancelar: CancelarCitaUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetalleCitaUiState>(DetalleCitaUiState.Cargando)
    val uiState = _uiState.asStateFlow()

    fun cargar(id: Long) {
        viewModelScope.launch {
            _uiState.value = DetalleCitaUiState.Cargando
            val p = paciente()
            if (p.isFailure) return@launch error()
            detalle(id, p.getOrThrow().id).fold(
                onSuccess = { _uiState.value = DetalleCitaUiState.Contenido(it) },
                onFailure = { error() }
            )
        }
    }

    fun cancelarCita() {
        val actual = (_uiState.value as? DetalleCitaUiState.Contenido)?.cita ?: return
        viewModelScope.launch {
            val idPaciente = actual.paciente.id
            cancelar(actual.id, idPaciente).fold(
                onSuccess = { _uiState.value = DetalleCitaUiState.Contenido(it, "La cita fue cancelada.") },
                onFailure = { _uiState.value = DetalleCitaUiState.Contenido(actual, it.message ?: "No se pudo cancelar la cita.") }
            )
        }
    }

    fun puedeCancelar(): Boolean = (_uiState.value as? DetalleCitaUiState.Contenido)
        ?.cita?.estado.let { it is EstadoCita.Programada }

    private fun error() { _uiState.value = DetalleCitaUiState.Error("No se pudo cargar el detalle. Inténtalo de nuevo.") }
}
