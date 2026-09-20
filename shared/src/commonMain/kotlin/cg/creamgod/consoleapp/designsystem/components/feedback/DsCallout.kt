package cg.creamgod.consoleapp.designsystem.components.feedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.designsystem.foundation.ComponentTone
import cg.creamgod.consoleapp.designsystem.tokens.BootstrapTokens

@Composable
fun DsCallout(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    tone: ComponentTone = ComponentTone.Info,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable RowScope.() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    val colors = calloutColors(tone)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.container,
        contentColor = colors.content,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(
            modifier = Modifier.padding(BootstrapTokens.Spacing.three),
            horizontalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.two),
            verticalAlignment = Alignment.Top,
        ) {
            icon?.invoke()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.one),
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                content?.invoke()
            }
            action?.invoke(this)
        }
    }
}

private data class CalloutColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

@Composable
private fun calloutColors(tone: ComponentTone): CalloutColors = when (tone) {
    ComponentTone.Primary,
    ComponentTone.Info,
    -> CalloutColors(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer,
        MaterialTheme.colorScheme.primary,
    )

    ComponentTone.Secondary,
    ComponentTone.Warning,
    -> CalloutColors(
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.onSecondaryContainer,
        MaterialTheme.colorScheme.secondary,
    )

    ComponentTone.Success -> CalloutColors(
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.onTertiaryContainer,
        MaterialTheme.colorScheme.tertiary,
    )

    ComponentTone.Danger -> CalloutColors(
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.onErrorContainer,
        MaterialTheme.colorScheme.error,
    )

    ComponentTone.Light -> CalloutColors(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.outlineVariant,
    )

    ComponentTone.Dark -> CalloutColors(
        MaterialTheme.colorScheme.inverseSurface,
        MaterialTheme.colorScheme.inverseOnSurface,
        MaterialTheme.colorScheme.outline,
    )
}
