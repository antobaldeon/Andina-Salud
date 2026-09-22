package pe.edu.upeu.andinasaludbaldeon.presentation.citas

sealed interface FaseCitas {
    data object Cargando : FaseCitas
    data class Contenido(val citas: List<CitaUi>) : FaseCitas
    data class Vacio(val mensaje: String) : FaseCitas
    data class Error(val mensaje: String) : FaseCitas
}

data class CitasUiState(
    val fase: FaseCitas = FaseCitas.Cargando,
    val busqueda: String = "",
    val filtro: FiltroEstadoCita = FiltroEstadoCita.PROGRAMADA
)
