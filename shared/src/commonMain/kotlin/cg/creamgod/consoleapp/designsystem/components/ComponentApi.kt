package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cg.creamgod.consoleapp.designsystem.foundation.ComponentSize
import cg.creamgod.consoleapp.designsystem.foundation.ComponentTone

typealias Tone = ComponentTone
typealias Size = ComponentSize

enum class Variant {
    Filled,
    Outline,
    Text,
}

enum class Placement {
    Start,
    End,
}

internal data class ToneColors(
    val color: Color,
    val onColor: Color,
    val container: Color,
    val onContainer: Color,
)

@Composable
internal fun toneColors(tone: Tone): ToneColors = when (tone) {
    Tone.Primary,
    Tone.Info,
    -> ToneColors(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.onPrimary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer,
    )

    Tone.Secondary,
    Tone.Warning,
    -> ToneColors(
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.onSecondary,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.onSecondaryContainer,
    )

    Tone.Success -> ToneColors(
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.onTertiary,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.onTertiaryContainer,
    )

    Tone.Danger -> ToneColors(
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.onError,
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.onErrorContainer,
    )

    Tone.Light -> ToneColors(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.surfaceContainer,
        MaterialTheme.colorScheme.onSurface,
    )

    Tone.Dark -> ToneColors(
        MaterialTheme.colorScheme.inverseSurface,
        MaterialTheme.colorScheme.inverseOnSurface,
        MaterialTheme.colorScheme.inverseSurface,
        MaterialTheme.colorScheme.inverseOnSurface,
    )
}
