package pe.edu.upeu.andinasaludbaldeon.presentation.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.components.EstadoCarga
import pe.edu.upeu.andinasaludbaldeon.presentation.components.EstadoError

@Composable
fun PerfilScreen(abrirAjustes: () -> Unit, viewModel: PerfilViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineMedium)
        when (val s = state) {
            PerfilUiState.Cargando -> EstadoCarga()
            is PerfilUiState.Error -> EstadoError(s.mensaje, viewModel::cargar)
            is PerfilUiState.Contenido -> {
                Text(s.paciente.nombre, style = MaterialTheme.typography.titleLarge)
                DatoPerfil("Documento", s.paciente.documento)
                DatoPerfil("Correo", s.paciente.correo)
                DatoPerfil("Teléfono", s.paciente.telefono)
            }
        }
        Button(onClick = abrirAjustes, modifier = Modifier.fillMaxWidth()) { Text("Ajustes de apariencia") }
    }
}

@Composable
private fun DatoPerfil(titulo: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}
