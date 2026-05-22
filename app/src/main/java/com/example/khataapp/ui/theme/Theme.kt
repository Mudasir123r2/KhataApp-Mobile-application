package com.example.khataapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KhataColorScheme = lightColorScheme(
    primary                = Green40,
    onPrimary              = Green99,
    primaryContainer       = Green95,
    onPrimaryContainer     = Green10,
    secondary              = Teal40,
    onSecondary            = Green99,
    secondaryContainer     = Teal90,
    onSecondaryContainer   = Green10,
    tertiary               = GoldAccent,
    onTertiary             = Neutral10,
    background             = Neutral99,
    onBackground           = Neutral10,
    surface                = SurfaceCard,
    onSurface              = Neutral10,
    surfaceVariant         = Neutral95,
    onSurfaceVariant       = Neutral20,
    error                  = Red40,
    onError                = Red90,
    errorContainer         = Red90,
    onErrorContainer       = Red10,
    outline                = Neutral90,
)

@Composable
fun KhataAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KhataColorScheme,
        typography  = KhataTypography,
        content     = content
    )
}
