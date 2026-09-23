package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator as MaterialLoadingIndicator
import androidx.compose.material3.Snackbar as MaterialSnackbar
import androidx.compose.material3.SnackbarHost as MaterialSnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

typealias SnackbarState = SnackbarHostState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
    progress: (() -> Float)? = null,
) {
    val color = toneColors(tone).color
    if (progress == null) {
        MaterialLoadingIndicator(modifier = modifier, color = color)
    } else {
        MaterialLoadingIndicator(progress = progress, modifier = modifier, color = color)
    }
}

@Composable
fun rememberSnackbarState(): SnackbarState = remember { SnackbarHostState() }

@Composable
fun SnackbarHost(
    state: SnackbarState,
    modifier: Modifier = Modifier,
) {
    MaterialSnackbarHost(hostState = state, modifier = modifier)
}

@Composable
fun Snackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    dismissible: Boolean = false,
    onDismiss: () -> Unit = {},
) {
    MaterialSnackbar(
        modifier = modifier,
        action = actionLabel?.let { label ->
            { TextButton(onClick = { onAction?.invoke() }) { Text(label) } }
        },
        dismissAction = if (dismissible) {
            { CloseButton(onClick = onDismiss, description = "關閉訊息") }
        } else {
            null
        },
    ) {
        Text(message)
    }
}
