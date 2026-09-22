package pe.edu.upeu.andinasaludbaldeon.domain.time

import kotlinx.datetime.TimeZone
import kotlin.time.Instant

interface Reloj {
    fun ahora(): Instant
    val zonaHoraria: TimeZone
}
