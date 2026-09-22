package cg.creamgod.consoleapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * Compose UI file picker. [Host] must be present inside the same Window as the calling screen.
 * The optional icon provider can replace the operating-system icon for application-specific files.
 */
class DesktopFilePicker(
    private val iconProvider: suspend (File) -> ImageBitmap? = ::loadSystemFileIcon,
    /** 所有側邊欄捷徑的自訂 Composable；呼叫 onClick 才會切換資料夾。 */
    private val shortcutButton: @Composable (label: String, path: Path, onClick: () -> Unit) -> Unit =
        { label, _, onClick -> DefaultShortcutButton(label, onClick) },
) : PlatformFilePicker {
    private data class Pending(
        val request: FilePickerRequest,
        val complete: (List<PickedFile>) -> Unit,
    )

    private var pending by mutableStateOf<Pending?>(null)

    override suspend fun pickFiles(allowMultiple: Boolean): List<PickedFile> =
        pickFiles(FilePickerRequest(allowMultiple = allowMultiple))

    override suspend fun pickFiles(request: FilePickerRequest): List<PickedFile> =
        withContext(Dispatchers.Swing) {
            check(pending == null) { "A file chooser is already open" }
            suspendCancellableCoroutine { continuation ->
                val pendingRequest = Pending(request) { files ->
                    if (continuation.isActive) continuation.resume(files)
                }
                pending = pendingRequest
                continuation.invokeOnCancellation {
                    if (pending === pendingRequest) pending = null
                }
            }
        }

    @Composable
    fun Host() {
        val request = pending ?: return
        DesktopFileChooser(
            request = request.request,
            iconProvider = iconProvider,
            shortcutButton = shortcutButton,
            onResult = { files ->
                if (pending === request) {
                    pending = null
                    request.complete(files)
                }
            },
        )
    }
}
