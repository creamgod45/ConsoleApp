package cg.creamgod.consoleapp

import kotlinx.coroutines.suspendCancellableCoroutine
import web.dom.document
import web.events.EventHandler
import web.html.HtmlTagName
import web.html.InputType
import web.html.file
import kotlin.coroutines.resume
import web.file.File

private val browserFiles = mutableListOf<Pair<PickedFile, File>>()
/** Opens the browser's native file chooser for both Kotlin/JS and Kotlin/Wasm. */
class BrowserFilePicker : PlatformFilePicker {

    fun browserFileOf(picked: PickedFile): File? =
        browserFiles.firstOrNull { (item, _) -> item === picked }?.second

    override suspend fun pickFiles(allowMultiple: Boolean): List<PickedFile> =
        pickFiles(FilePickerRequest(allowMultiple = allowMultiple))

    override suspend fun pickFiles(request: FilePickerRequest): List<PickedFile> =
        if (request.selectionMode == FilePickerSelectionMode.Directories) emptyList() else suspendCancellableCoroutine { continuation ->
            val input = document.createElement(HtmlTagName.input).apply {
                type = InputType.file
                multiple = request.allowMultiple
                val accepted = request.allowedExtensions.map { ".${it.removePrefix(".")}" } + request.allowedMimeTypes
                if (accepted.isNotEmpty()) {
                    accept = accepted.joinToString(",")
                }
                setAttribute("hidden", "")
            }

            fun cleanup() {
                input.onchange = null
                input.oncancel = null
                input.remove()
            }

            fun complete(files: List<PickedFile>) {
                if (!continuation.isActive) return
                cleanup()
                continuation.resume(files)
            }

            input.onchange = EventHandler {
                val retained = mutableListOf<Pair<PickedFile, File>>()
                val selected = input.files
                val files = buildList {
                    if (selected != null) {
                        for (index in 0 until selected.length) {
                            val file = selected.item(index) ?: continue
                            val matchesExtension = request.allowedExtensions.isEmpty() ||
                                request.allowedExtensions.any { extension ->
                                    file.name.endsWith(".${extension.removePrefix(".")}", ignoreCase = true)
                                }
                            val matchesMime = request.allowedMimeTypes.isEmpty() || request.allowedMimeTypes.any { allowed ->
                                allowed.equals(file.type, ignoreCase = true) ||
                                    (allowed.endsWith("/*") && file.type.startsWith(allowed.removeSuffix("*"), ignoreCase = true))
                            }
                            if (!matchesExtension || !matchesMime ||
                                (request.minFileSizeBytes != null && file.size < request.minFileSizeBytes!!.toDouble()) ||
                                (request.maxFileSizeBytes != null && file.size > request.maxFileSizeBytes!!.toDouble())) {
                                continue
                            }
                            val picked = PickedFile(
                                name = file.name,
                                path = file.webkitRelativePath.ifBlank { file.name },
                                sizeBytes = file.size.toLong(),
                            )
                            add(picked)
                            retained += picked to file
                        }
                    }
                }
                browserFiles.clear()
                browserFiles.addAll(retained)
                complete(files)
            }
            input.oncancel = EventHandler { complete(emptyList()) }
            continuation.invokeOnCancellation { cleanup() }

            document.body.appendChild(input)
            input.click()
        }
}
