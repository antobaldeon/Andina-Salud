package pe.edu.upeu.andinasaludbaldeon.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import pe.edu.upeu.andinasaludbaldeon.presentation.ajustes.AjustesScreen
import pe.edu.upeu.andinasaludbaldeon.presentation.citas.CitasScreen
import pe.edu.upeu.andinasaludbaldeon.presentation.citas.CitasViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.citas.FaseCitas
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.detalle.DetalleCitaScreen
import pe.edu.upeu.andinasaludbaldeon.presentation.inicio.InicioScreen
import pe.edu.upeu.andinasaludbaldeon.presentation.perfil.PerfilScreen
import pe.edu.upeu.andinasaludbaldeon.presentation.solicitud.SolicitudScreen

@Composable
fun AppNavHost(modoOscuro: Boolean, onModoOscuroChange: (Boolean) -> Unit) {
    val nav = rememberNavController()
    val citasViewModel: CitasViewModel = koinViewModel()
    val citasState by citasViewModel.uiState.collectAsState()
    var citaSeleccionada by remember { mutableStateOf(0L) }
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    var refreshKey by remember { mutableIntStateOf(0) }
    LaunchedEffect(route) {
        if (route == Destinos.INICIO || route == Destinos.CITAS) refreshKey++
    }
    val mainRoute = when {
        route == Destinos.CITAS -> Destinos.CITAS
        route == Destinos.PERFIL || route == "ajustes" -> Destinos.PERFIL
        else -> Destinos.INICIO
    }
    val mostrarBarra = route in setOf(Destinos.INICIO, Destinos.CITAS, Destinos.PERFIL)

    Scaffold(bottomBar = {
        if (mostrarBarra) BarraNavegacion(mainRoute, citasState.citasProgramadas) { destino ->
            nav.navigate(destino) {
                popUpTo(Destinos.INICIO) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }) { padding ->
        NavHost(navController = nav, startDestination = Destinos.INICIO, modifier = Modifier.padding(padding)) {
            composable(Destinos.INICIO) {
                InicioScreen(
                    refreshKey = refreshKey,
                    solicitarHabilitada = (citasState.fase is FaseCitas.Contenido || citasState.fase is FaseCitas.Vacio) && citasState.citasProgramadas < 3,
                    irACitas = { nav.navigate(Destinos.CITAS) },
                    irASolicitud = { nav.navigate(Destinos.SOLICITUD) },
                    abrirDetalle = { citaSeleccionada = it; nav.navigate(Destinos.DETALLE) }
                )
            }
            composable(Destinos.CITAS) {
                CitasScreen(
                    refreshKey = refreshKey,
                    viewModel = citasViewModel,
                    abrirDetalle = { citaSeleccionada = it; nav.navigate(Destinos.DETALLE) },
                    irASolicitud = { nav.navigate(Destinos.SOLICITUD) }
                )
            }
            composable(Destinos.PERFIL) { PerfilScreen(abrirAjustes = { nav.navigate("ajustes") }) }
            composable("ajustes") {
                AjustesScreen(modoOscuro, onModoOscuroChange) { nav.popBackStack() }
            }
            composable(Destinos.SOLICITUD) {
                SolicitudScreen(
                    volver = { if (!nav.popBackStack()) nav.navigate(Destinos.INICIO) },
                    irACitas = {
                        nav.navigate(Destinos.CITAS) {
                            popUpTo(Destinos.SOLICITUD) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Destinos.DETALLE) {
                DetalleCitaScreen(id = citaSeleccionada, volver = { nav.popBackStack() })
            }
        }
    }
}
