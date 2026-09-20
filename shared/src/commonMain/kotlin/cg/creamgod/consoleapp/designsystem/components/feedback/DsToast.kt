package cg.creamgod.consoleapp.designsystem.components.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Stable
class DsToastState internal constructor(
    internal val snackbarHostState: SnackbarHostState,
) {
    suspend fun show(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = actionLabel == null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ): SnackbarResult = snackbarHostState.showSnackbar(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = withDismissAction,
        duration = duration,
    )

    fun dismiss() {
        snackbarHostState.currentSnackbarData?.dismiss()
    }
}

@Composable
fun rememberDsToastState(): DsToastState = remember {
    DsToastState(SnackbarHostState())
}

@Composable
fun DsToastHost(
    state: DsToastState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = state.snackbarHostState,
        modifier = modifier,
    )
}
