package pe.edu.upeu.andinasaludbaldeon.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.andinasaludbaldeon.domain.model.EstadoCita
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.CancelarCitaUseCase
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.ObtenerDetalleCitaUseCase
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.ObtenerPacienteUseCase
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.ReprogramarCitaUseCase

class DetalleCitaViewModel(
    private val paciente: ObtenerPacienteUseCase,
    private val detalle: ObtenerDetalleCitaUseCase,
    private val cancelar: CancelarCitaUseCase,
    private val reprogramar: ReprogramarCitaUseCase
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

    fun puedeCancelar(): Boolean = (_uiState.value as? DetalleCitaUiState.Contenido)?.cita?.estado is EstadoCita.Programada

    fun reprogramar(fecha: String, hora: String) {
        val cita = (_uiState.value as? DetalleCitaUiState.Contenido)?.cita ?: return
        val nuevaFecha = runCatching { LocalDate.parse(fecha) }.getOrNull()
        val nuevaHora = runCatching { LocalTime.parse(hora) }.getOrNull()
        if (nuevaFecha == null || nuevaHora == null) {
            _uiState.value = DetalleCitaUiState.Contenido(cita, "Ingresa fecha AAAA-MM-DD y hora HH:MM válidas.")
            return
        }
        viewModelScope.launch {
            reprogramar(cita.id, cita.paciente.id, nuevaFecha, nuevaHora).fold(
                onSuccess = { _uiState.value = DetalleCitaUiState.Contenido(it, "Cita reprogramada correctamente.") },
                onFailure = { _uiState.value = DetalleCitaUiState.Contenido(cita, it.message ?: "No se pudo reprogramar.") }
            )
        }
    }

    private fun error() { _uiState.value = DetalleCitaUiState.Error("No se pudo cargar el detalle. Inténtalo de nuevo.") }
}
