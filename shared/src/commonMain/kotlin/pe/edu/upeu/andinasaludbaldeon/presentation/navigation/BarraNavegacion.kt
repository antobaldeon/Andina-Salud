package pe.edu.upeu.andinasaludbaldeon.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BarraNavegacion(destino: String, navegar: (String) -> Unit) {
    NavigationBar {
        listOf(
            Triple(Destinos.INICIO, "Inicio", Icons.Filled.Home),
            Triple(Destinos.CITAS, "Citas", Icons.Filled.CalendarMonth),
            Triple(Destinos.PERFIL, "Perfil", Icons.Filled.Person)
        ).forEach { (ruta, etiqueta, icono) ->
            NavigationBarItem(
                selected = destino == ruta,
                onClick = { if (destino != ruta) navegar(ruta) },
                icon = { Icon(icono, contentDescription = etiqueta) },
                label = { Text(etiqueta) }
            )
        }
    }
}
