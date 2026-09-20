package cg.creamgod.consoleapp.designsystem.components.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cg.creamgod.consoleapp.designsystem.tokens.BootstrapTokens

@Composable
fun DsModal(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    footer: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
        ),
    ) {
        Surface(
            modifier = modifier.widthIn(min = 280.dp, max = 560.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
        ) {
            Column {
                Text(
                    text = title,
                    modifier = Modifier.padding(BootstrapTokens.Spacing.three),
                    style = MaterialTheme.typography.titleLarge,
                )
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(BootstrapTokens.Spacing.three),
                    verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.two),
                    content = content,
                )
                footer?.let {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(BootstrapTokens.Spacing.three),
                        horizontalArrangement = Arrangement.spacedBy(
                            BootstrapTokens.Spacing.two,
                            androidx.compose.ui.Alignment.End,
                        ),
                        content = it,
                    )
                }
            }
        }
    }
}
