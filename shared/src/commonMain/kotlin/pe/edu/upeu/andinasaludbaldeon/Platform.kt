package pe.edu.upeu.andinasaludbaldeon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform