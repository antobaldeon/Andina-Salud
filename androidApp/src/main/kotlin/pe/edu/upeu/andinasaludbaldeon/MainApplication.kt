package pe.edu.upeu.andinasaludbaldeon

import android.app.Application
import org.koin.android.ext.koin.androidContext
import pe.edu.upeu.andinasaludbaldeon.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@MainApplication) }
    }
}
