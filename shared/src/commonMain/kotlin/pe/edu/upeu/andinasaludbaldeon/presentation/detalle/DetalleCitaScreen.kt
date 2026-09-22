package pe.edu.upeu.andinasaludbaldeon.presentation.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.andinasaludbaldeon.domain.model.EstadoCita
import pe.edu.upeu.andinasaludbaldeon.domain.model.ModalidadAtencion
import pe.edu.upeu.andinasaludbaldeon.presentation.components.EstadoCarga
import pe.edu.upeu.andinasaludbaldeon.presentation.components.EstadoError

@Composable
fun DetalleCitaScreen(id: Long, volver: () -> Unit, viewModel: DetalleCitaViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var confirmar by remember(id) { mutableStateOf(false) }
    var editar by remember(id) { mutableStateOf(false) }
    var nuevaFecha by remember(id) { mutableStateOf("") }
    var nuevaHora by remember(id) { mutableStateOf("") }
    LaunchedEffect(id) { viewModel.cargar(id) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Detalle de cita", style = MaterialTheme.typography.headlineMedium)
        when (val actual = state) {
            DetalleCitaUiState.Cargando -> EstadoCarga()
            is DetalleCitaUiState.Error -> EstadoError(actual.mensaje) { viewModel.cargar(id) }
            is DetalleCitaUiState.Contenido -> {
                val cita = actual.cita
                Text(cita.especialidad.nombre, style = MaterialTheme.typography.titleLarge)
                FilaDetalle("Médico", cita.medico.nombre)
                FilaDetalle("Sede", cita.sede.nombre)
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(if (cita.modalidad == ModalidadAtencion.TELECONSULTA) Icons.Filled.Videocam else Icons.Filled.LocationOn, contentDescription = cita.modalidad.etiqueta, tint = MaterialTheme.colorScheme.primary)
                    FilaDetalle("Modalidad", cita.modalidad.etiqueta)
                }
                FilaDetalle("Fecha", cita.fecha.toString())
                FilaDetalle("Hora", cita.hora.toString())
                FilaDetalle("Estado", when (val e = cita.estado) {
                    is EstadoCita.Programada -> "Programada${if (e.recordatorioActivo) " · recordatorio activo" else ""}"
                    is EstadoCita.Atendida -> "Atendida"
                    is EstadoCita.Cancelada -> "Cancelada · ${e.motivo}"
                })
                when (val e = cita.estado) {
                    is EstadoCita.Atendida -> FilaDetalle("Indicaciones", e.indicaciones)
                    is EstadoCita.Cancelada -> FilaDetalle("Motivo de cancelación", e.motivo)
                    is EstadoCita.Programada -> FilaDetalle("Motivo", cita.motivo)
                }
                if (cita.historialCambios.isNotEmpty()) FilaDetalle("Historial de cambios", cita.historialCambios.joinToString("\n"))
                actual.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                if (cita.estado is EstadoCita.Programada) {
                    Button(onClick = { nuevaFecha = cita.fecha.toString(); nuevaHora = cita.hora.toString(); editar = true }, modifier = Modifier.fillMaxWidth()) { Text("Reprogramar cita") }
                    Button(onClick = { confirmar = true }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar cita") }
                }
                OutlinedButton(onClick = volver, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
            }
        }
    }
    if (confirmar) AlertDialog(
        onDismissRequest = { confirmar = false },
        title = { Text("¿Cancelar esta cita?") },
        text = { Text("Esta acción no se puede deshacer. Solo se permite cancelar con más de 24 horas de anticipación.") },
        confirmButton = { TextButton(onClick = { confirmar = false; viewModel.cancelarCita() }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Conservar cita") } }
    )
    if (editar) AlertDialog(
        onDismissRequest = { editar = false },
        title = { Text("Reprogramar cita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("La nueva fecha y hora deben estar disponibles y ser futuras.")
                OutlinedTextField(nuevaFecha, { nuevaFecha = it }, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true)
                OutlinedTextField(nuevaHora, { nuevaHora = it }, label = { Text("Hora (HH:MM)") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { editar = false; viewModel.reprogramar(nuevaFecha, nuevaHora) }) { Text("Guardar cambio") } },
        dismissButton = { TextButton(onClick = { editar = false }) { Text("Volver") } }
    )
}

@Composable
private fun FilaDetalle(titulo: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}
