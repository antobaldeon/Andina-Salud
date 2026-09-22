package pe.edu.upeu.andinasaludbaldeon.presentation.citas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.components.CitaCard
import pe.edu.upeu.andinasaludbaldeon.presentation.components.EstadoCarga
import pe.edu.upeu.andinasaludbaldeon.presentation.components.EstadoError
import pe.edu.upeu.andinasaludbaldeon.presentation.components.EstadoVacio

@Composable
fun CitasScreen(
    refreshKey: Int,
    abrirDetalle: (Long) -> Unit,
    irASolicitud: () -> Unit,
    viewModel: CitasViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(refreshKey) { viewModel.cargar() }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("Mis citas", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 20.dp, bottom = 12.dp))
        OutlinedTextField(
            value = state.busqueda, onValueChange = viewModel::onBusquedaChange,
            label = { Text("Especialidad o médico") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroEstadoCita.entries.forEach { filtro ->
                FilterChip(selected = state.filtro == filtro, onClick = { viewModel.onFiltroChange(filtro) }, label = { Text(filtro.etiqueta) })
            }
            FilterChip(selected = state.soloHoy, onClick = { viewModel.onSoloHoyChange(!state.soloHoy) }, label = { Text("Hoy") })
        }
        when (val fase = state.fase) {
            FaseCitas.Cargando -> EstadoCarga()
            is FaseCitas.Error -> EstadoError(fase.mensaje, viewModel::reintentar)
            is FaseCitas.Vacio -> Column(Modifier.weight(1f)) {
                EstadoVacio(fase.mensaje)
            }
            is FaseCitas.Contenido -> LazyColumn(
                modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(fase.citas, key = { it.id }) { cita -> CitaCard(cita, { abrirDetalle(cita.id) }) }
            }
        }
        if (state.fase is FaseCitas.Contenido || state.fase is FaseCitas.Vacio) {
            Button(onClick = irASolicitud, enabled = state.citasProgramadas < 3, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(if (state.citasProgramadas >= 3) "Límite de 3 citas alcanzado" else "Solicitar cita")
            }
        }
    }
}
