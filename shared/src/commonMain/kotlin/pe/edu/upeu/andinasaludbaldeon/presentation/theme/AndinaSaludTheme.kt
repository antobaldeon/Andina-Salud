package pe.edu.upeu.andinasaludbaldeon.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Claro = lightColorScheme(
    primary = VerdeAndino, onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = Turquesa, background = FondoClaro, surface = androidx.compose.ui.graphics.Color.White,
    onBackground = TextoOscuro, onSurface = TextoOscuro
)
private val Oscuro = darkColorScheme(
    primary = Turquesa, secondary = VerdeAndino,
    background = androidx.compose.ui.graphics.Color(0xFF101B19),
    surface = androidx.compose.ui.graphics.Color(0xFF182522),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE3F2EE),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE3F2EE)
)

@Composable
fun AndinaSaludTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Oscuro else Claro, content = content)
}
