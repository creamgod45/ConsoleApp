package cg.creamgod.consoleapp.designsystem.content

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import consoleapp.shared.generated.resources.Res

sealed interface MarkdownResourceState {
    data object Loading : MarkdownResourceState
    data class Ready(val content: String) : MarkdownResourceState
    data class Failed(val error: Throwable) : MarkdownResourceState
}

suspend fun readTextResource(path: String): String {
    val resourcePath = if (path.startsWith("files/")) path else "files/$path"
    return Res.readBytes(resourcePath).decodeToString()
}

@Composable
fun MarkdownResource(
    path: String,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = { CircularProgressIndicator() },
    failure: @Composable (Throwable) -> Unit = { error ->
        Text(
            text = error.message ?: "Unable to load Markdown resource.",
            color = MaterialTheme.colorScheme.error,
        )
    },
) {
    val state by produceState<MarkdownResourceState>(MarkdownResourceState.Loading, path) {
        value = try {
            MarkdownResourceState.Ready(readTextResource(path))
        } catch (error: Throwable) {
            MarkdownResourceState.Failed(error)
        }
    }

    when (val current = state) {
        MarkdownResourceState.Loading -> loading()
        is MarkdownResourceState.Ready -> MarkdownDocument(current.content, modifier)
        is MarkdownResourceState.Failed -> failure(current.error)
    }
}
