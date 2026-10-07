package com.bivora.karaliste.ui.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Bivora marka renkleri (res/values/colors.xml ile aynı)
private val BivoraMor = Color(0xFF4B2AE8)
private val BivoraMorAcik = Color(0xFFB9A8FF)
private val BivoraPembe = Color(0xFFFF2E93)
private val BivoraPembeAcik = Color(0xFFFF8CC2)
private val BivoraSari = Color(0xFFFFC93C)

private val AcikRenkler = lightColorScheme(
    primary = BivoraMor,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E0FF),
    onPrimaryContainer = Color(0xFF16006E),
    secondary = BivoraPembe,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E7),
    onSecondaryContainer = Color(0xFF3E0021),
    tertiary = BivoraSari,
    onTertiary = Color(0xFF261A00)
)

private val KoyuRenkler = darkColorScheme(
    primary = BivoraMorAcik,
    onPrimary = Color(0xFF26009B),
    primaryContainer = Color(0xFF3A16C9),
    onPrimaryContainer = Color(0xFFE6E0FF),
    secondary = BivoraPembeAcik,
    onSecondary = Color(0xFF5E0036),
    secondaryContainer = Color(0xFF85004F),
    onSecondaryContainer = Color(0xFFFFD9E7),
    tertiary = BivoraSari,
    onTertiary = Color(0xFF261A00)
)

// Marka rengi her cihazda aynı kalsın diye Android 12+ dinamik renkler kullanılmaz
@Composable
fun KaralisteTema(icerik: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) KoyuRenkler else AcikRenkler,
        content = icerik
    )
}
