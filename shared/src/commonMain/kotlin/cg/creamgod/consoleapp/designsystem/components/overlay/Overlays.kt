package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cg.creamgod.consoleapp.designsystem.components.form.FormOption
import cg.creamgod.consoleapp.designsystem.components.overlay.DsAskDialog
import cg.creamgod.consoleapp.designsystem.components.overlay.DsChoiceDialog
import cg.creamgod.consoleapp.designsystem.components.overlay.DsConfirmDialog
import cg.creamgod.consoleapp.designsystem.components.overlay.DsModal
import cg.creamgod.consoleapp.designsystem.components.overlay.DsPopup
import cg.creamgod.consoleapp.designsystem.tokens.BootstrapTokens

@Composable
fun Popup(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopStart,
    offset: IntOffset = IntOffset.Zero,
    content: @Composable BoxScope.() -> Unit,
) = DsPopup(
    visible = visible,
    onDismissRequest = onDismiss,
    modifier = modifier,
    alignment = alignment,
    offset = offset,
    content = content,
)

@Composable
fun Modal(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    footer: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) = DsModal(
    visible = visible,
    onDismissRequest = onDismiss,
    title = title,
    modifier = modifier,
    footer = footer,
    content = content,
)

@Composable
fun Confirm(
    visible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "確認",
    dismissText: String = "取消",
    destructive: Boolean = false,
) = DsConfirmDialog(
    visible = visible,
    onConfirm = onConfirm,
    onDismissRequest = onDismiss,
    title = title,
    message = message,
    confirmText = confirmText,
    dismissText = dismissText,
    destructive = destructive,
)

@Composable
fun Ask(
    visible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String,
    label: String,
    message: String? = null,
    errorText: String? = null,
) = DsAskDialog(
    visible = visible,
    value = value,
    onValueChange = onValueChange,
    onConfirm = onConfirm,
    onDismissRequest = onDismiss,
    title = title,
    label = label,
    message = message,
    errorText = errorText,
)

@Composable
fun <T> Choice(
    visible: Boolean,
    value: T?,
    options: List<FormOption<T>>,
    onValueChange: (T) -> Unit,
    onConfirm: (T) -> Unit,
    onDismiss: () -> Unit,
    title: String,
    message: String? = null,
) = DsChoiceDialog(
    visible = visible,
    value = value,
    options = options,
    onValueChange = onValueChange,
    onConfirm = onConfirm,
    onDismissRequest = onDismiss,
    title = title,
    message = message,
)

@Composable
fun Offcanvas(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    placement: Placement = Placement.End,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(onClick = onDismiss),
            )
            Surface(
                modifier = modifier
                    .align(if (placement == Placement.Start) Alignment.CenterStart else Alignment.CenterEnd)
                    .fillMaxHeight()
                    .widthIn(min = 280.dp, max = 420.dp),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 12.dp,
            ) {
                Column(modifier = Modifier.padding(BootstrapTokens.Spacing.three)) {
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                        CloseButton(onClick = onDismiss)
                    }
                    content()
                }
            }
        }
    }
}

@Composable
fun Popover(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.BottomCenter,
    anchor: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        anchor()
        Popup(visible = visible, onDismiss = onDismiss, alignment = alignment) {
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun Tooltip(
    visible: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
        if (visible) {
            androidx.compose.ui.window.Popup(alignment = alignment) {
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = MaterialTheme.shapes.small,
                    shadowElevation = 4.dp,
                ) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
