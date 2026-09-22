package pe.edu.upeu.pharmamobil.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// MODIFICADO (Reto 02): Paleta corporativa clara con roles semánticos de Material 3.
private val PharmaLightColors = lightColorScheme(
    primary = Color(0xFF006C4C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8DF8C7),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF386A5A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB9F2DC),
    onSecondaryContainer = Color(0xFF002018),
    surface = Color(0xFFF8FBF6),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDDE5DE),
    onSurfaceVariant = Color(0xFF414942)
)

// MODIFICADO (Reto 02): Paleta oscura calibrada para conservar contraste y jerarquía visual.
private val PharmaDarkColors = darkColorScheme(
    primary = Color(0xFF71DBAC),
    onPrimary = Color(0xFF003825),
    primaryContainer = Color(0xFF005137),
    onPrimaryContainer = Color(0xFF8DF8C7),
    secondary = Color(0xFF9DD5C0),
    onSecondary = Color(0xFF00382A),
    secondaryContainer = Color(0xFF1F5141),
    onSecondaryContainer = Color(0xFFB9F2DC),
    surface = Color(0xFF101411),
    onSurface = Color(0xFFE0E4DE),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC1C9C1)
)

// AGREGADO (Reto 02): Formas coherentes para campos, botones y contenedores de la app.
private val PharmaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

// AGREGADO (Reto 02): Punto central para personalizar la tipografía corporativa.
private val PharmaTypography = Typography()

// MODIFICADO (Reto 02): Propaga paleta, tipografía y formas a todas las pantallas.
@Composable
fun PharmaMobilTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) PharmaDarkColors else PharmaLightColors,
        typography = PharmaTypography,
        shapes = PharmaShapes,
        content = content
    )
}
