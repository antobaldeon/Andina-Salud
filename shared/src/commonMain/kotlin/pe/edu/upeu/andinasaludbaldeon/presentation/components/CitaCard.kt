package pe.edu.upeu.andinasaludbaldeon.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.andinasaludbaldeon.presentation.citas.CitaUi
import pe.edu.upeu.andinasaludbaldeon.domain.model.ModalidadAtencion

@Composable
fun CitaCard(cita: CitaUi, alAbrir: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().clickable(onClick = alAbrir)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(cita.especialidad, style = MaterialTheme.typography.titleMedium)
                Text(cita.estado.etiqueta, color = MaterialTheme.colorScheme.primary)
            }
            Text(cita.medico)
            Text("${cita.fecha} · ${cita.hora} · ${cita.sede}", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    if (cita.modalidad == ModalidadAtencion.TELECONSULTA) Icons.Filled.Videocam else Icons.Filled.LocationOn,
                    contentDescription = cita.modalidad.etiqueta,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(cita.modalidad.etiqueta, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
