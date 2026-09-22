package pe.edu.upeu.andinasaludbaldeon.data.time

import kotlinx.datetime.TimeZone
import pe.edu.upeu.andinasaludbaldeon.domain.time.Reloj
import kotlin.time.Clock
import kotlin.time.Instant

class RelojSistema(
    override val zonaHoraria: TimeZone = TimeZone.of("America/Lima")
) : Reloj {
    override fun ahora(): Instant = Clock.System.now()
}
