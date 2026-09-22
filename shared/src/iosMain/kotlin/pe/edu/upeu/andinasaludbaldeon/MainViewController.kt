package pe.edu.upeu.andinasaludbaldeon

import androidx.compose.ui.window.ComposeUIViewController
import pe.edu.upeu.andinasaludbaldeon.di.initKoinForIos

fun MainViewController() = run {
    initKoinForIos()
    ComposeUIViewController { App() }
}
