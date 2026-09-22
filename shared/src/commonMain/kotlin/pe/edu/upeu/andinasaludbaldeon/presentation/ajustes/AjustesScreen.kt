package pe.edu.upeu.andinasaludbaldeon.presentation.ajustes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AjustesScreen(modoOscuro: Boolean, onModoOscuroChange: (Boolean) -> Unit, volver: () -> Unit) {
    androidx.compose.foundation.layout.Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
        Text("Personaliza la apariencia de AndinaSalud.")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (modoOscuro) "Tema oscuro" else "Tema claro")
            Switch(checked = modoOscuro, onCheckedChange = onModoOscuroChange)
        }
        androidx.compose.material3.OutlinedButton(onClick = volver) { Text("Volver al perfil") }
    }
}
