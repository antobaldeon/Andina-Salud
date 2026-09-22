package pe.edu.upeu.andinasaludbaldeon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.ajustes.AjustesViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.navigation.AppNavHost
import pe.edu.upeu.andinasaludbaldeon.presentation.theme.AndinaSaludTheme

@Composable
fun App() {
    val ajustes: AjustesViewModel = koinViewModel()
    val estado by ajustes.uiState.collectAsState()
    AndinaSaludTheme(darkTheme = estado.modoOscuro) {
        AppNavHost(modoOscuro = estado.modoOscuro, onModoOscuroChange = ajustes::cambiarTema)
    }
}
