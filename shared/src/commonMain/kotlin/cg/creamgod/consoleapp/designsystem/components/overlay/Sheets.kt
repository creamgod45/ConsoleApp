package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        content = content,
    )
}

/** Material side-sheet API backed by the framework's responsive off-canvas surface. */
@Composable
fun SideSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    placement: Placement = Placement.End,
    content: @Composable ColumnScope.() -> Unit,
) = Offcanvas(
    visible = visible,
    onDismiss = onDismiss,
    title = title,
    modifier = modifier,
    placement = placement,
    content = content,
)
