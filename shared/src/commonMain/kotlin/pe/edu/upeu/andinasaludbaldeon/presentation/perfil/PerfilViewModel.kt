package pe.edu.upeu.andinasaludbaldeon.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.ObtenerPacienteUseCase

class PerfilViewModel(private val obtenerPaciente: ObtenerPacienteUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Cargando)
    val uiState = _uiState.asStateFlow()
    init { cargar() }
    fun cargar() {
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Cargando
            obtenerPaciente().fold(
                onSuccess = { _uiState.value = PerfilUiState.Contenido(it) },
                onFailure = { _uiState.value = PerfilUiState.Error("No se pudo cargar el perfil. Inténtalo de nuevo.") }
            )
        }
    }
}
