package pe.edu.upeu.andinasaludbaldeon

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}