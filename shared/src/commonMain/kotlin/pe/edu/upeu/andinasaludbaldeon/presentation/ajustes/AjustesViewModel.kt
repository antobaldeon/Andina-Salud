package pe.edu.upeu.andinasaludbaldeon.presentation.ajustes

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AjustesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AjustesUiState())
    val uiState = _uiState.asStateFlow()
    fun cambiarTema(modoOscuro: Boolean) { _uiState.value = AjustesUiState(modoOscuro) }
}
