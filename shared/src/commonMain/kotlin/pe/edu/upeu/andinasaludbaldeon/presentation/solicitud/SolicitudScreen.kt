package pe.edu.upeu.andinasaludbaldeon.presentation.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SolicitudScreen(volver: () -> Unit, irACitas: () -> Unit, viewModel: SolicitudViewModel = koinViewModel()) {
    val s by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Solicitar cita", style = MaterialTheme.typography.headlineMedium)
        if (s.cargando) CircularProgressIndicator()
        else {
            Selector("Especialidad", s.especialidades.map { it.id to it.nombre }, s.especialidadId, viewModel::elegirEspecialidad, s.errores.especialidad)
            Selector("Sede", s.sedes.map { it.id to it.nombre }, s.sedeId, viewModel::elegirSede, s.errores.sede)
            OutlinedTextField(s.fecha, viewModel::cambiarFecha, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), isError = s.errores.fecha != null, supportingText = { s.errores.fecha?.let { Text(it) } })
            OutlinedTextField(s.hora, viewModel::cambiarHora, label = { Text("Hora (HH:MM)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), isError = s.errores.hora != null, supportingText = { s.errores.hora?.let { Text(it) } })
            OutlinedTextField(s.motivo, viewModel::cambiarMotivo, label = { Text("Motivo de consulta") }, minLines = 3, maxLines = 5, modifier = Modifier.fillMaxWidth(), isError = s.errores.motivo != null, supportingText = { Text(s.errores.motivo ?: "Entre 10 y 200 caracteres · ${s.motivo.length}/200") })
            s.errores.general?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            s.mensaje?.let { Text(it, color = if (s.completada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
            Button(onClick = { if (!s.completada) viewModel.enviar() else irACitas() }, enabled = !s.enviando, modifier = Modifier.fillMaxWidth()) {
                Text(if (s.enviando) "Enviando…" else if (s.completada) "Volver a mis citas" else "Confirmar solicitud")
            }
        }
        OutlinedButton(onClick = volver, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun Selector(
    etiqueta: String, opciones: List<Pair<String, String>>, seleccionado: String,
    elegir: (String) -> Unit, error: String?
) {
    var expandido by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val nombre = opciones.firstOrNull { it.first == seleccionado }?.second.orEmpty()
    Column {
        androidx.compose.material3.OutlinedButton(onClick = { expandido = true }, modifier = Modifier.fillMaxWidth()) {
            Text(if (nombre.isBlank()) "Seleccionar $etiqueta" else nombre)
        }
        androidx.compose.material3.DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { (id, titulo) ->
                androidx.compose.material3.DropdownMenuItem(text = { Text(titulo) }, onClick = { elegir(id); expandido = false })
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    }
}
