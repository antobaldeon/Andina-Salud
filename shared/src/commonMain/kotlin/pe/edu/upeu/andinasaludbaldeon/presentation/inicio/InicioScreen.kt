package pe.edu.upeu.andinasaludbaldeon.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.components.EstadoCarga
import pe.edu.upeu.andinasaludbaldeon.presentation.components.EstadoError

@Composable
fun InicioScreen(
    refreshKey: Int,
    solicitarHabilitada: Boolean,
    irACitas: () -> Unit,
    irASolicitud: () -> Unit,
    abrirDetalle: (Long) -> Unit,
    viewModel: InicioViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(refreshKey) { viewModel.cargar() }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("AndinaSalud", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 24.dp))
        when (val value = state) {
            InicioUiState.Cargando -> EstadoCarga()
            is InicioUiState.Error -> EstadoError(value.mensaje, viewModel::reintentar)
            is InicioUiState.SinCitas -> {
                Text("Hola, ${value.nombrePaciente}", style = MaterialTheme.typography.headlineMedium)
                Text("Todavía no tienes citas programadas.")
            }
            is InicioUiState.Contenido -> {
                Text("Hola, ${value.nombrePaciente}", style = MaterialTheme.typography.headlineMedium)
                Card(
                    modifier = Modifier.fillMaxWidth(), onClick = { abrirDetalle(value.proximaCita.id) },
                    shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tu próxima cita", style = MaterialTheme.typography.titleMedium)
                        Text(value.proximaCita.especialidad, style = MaterialTheme.typography.headlineSmall)
                        Text(value.proximaCita.medico)
                        Text("${value.proximaCita.fecha} · ${value.proximaCita.hora}")
                        Text(value.proximaCita.sede)
                        Text("Ver detalle", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        Button(onClick = irACitas, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) { Text("Mis citas") }
        OutlinedButton(onClick = irASolicitud, enabled = solicitarHabilitada, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            Text(if (solicitarHabilitada) "Solicitar cita" else "Límite de 3 citas alcanzado")
        }
    }
}
