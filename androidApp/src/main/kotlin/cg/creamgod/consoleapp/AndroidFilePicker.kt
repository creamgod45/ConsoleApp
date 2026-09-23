package cg.creamgod.consoleapp

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidFilePicker(
    private val activity: ComponentActivity,
) : PlatformFilePicker {
    private val contentResolver: ContentResolver = activity.contentResolver
    private var pending: PendingRequest? = null

    private val singleFileLauncher = activity.registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        complete(uri?.let(::listOf).orEmpty(), isDirectory = false)
    }

    private val multipleFileLauncher = activity.registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        complete(uris, isDirectory = false)
    }

    private val directoryLauncher = activity.registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        complete(uri?.let(::listOf).orEmpty(), isDirectory = true)
    }

    override suspend fun pickFiles(allowMultiple: Boolean): List<PickedFile> =
        pickFiles(FilePickerRequest(allowMultiple = allowMultiple))

    override suspend fun pickFiles(request: FilePickerRequest): List<PickedFile> =
        suspendCancellableCoroutine { continuation ->
            if (pending != null) {
                continuation.resumeWith(
                    Result.failure(IllegalStateException("已有檔案選擇視窗開啟中")),
                )
                return@suspendCancellableCoroutine
            }

            if (request.selectionMode == FilePickerSelectionMode.FilesAndDirectories) {
                continuation.resumeWith(
                    Result.failure(
                        UnsupportedOperationException("Android 系統選擇器無法同時選擇檔案與資料夾"),
                    ),
                )
                return@suspendCancellableCoroutine
            }

            pending = PendingRequest(request, continuation)
            continuation.invokeOnCancellation {
                if (pending?.continuation === continuation) pending = null
            }

            try {
                when (request.selectionMode) {
                    FilePickerSelectionMode.Directories -> directoryLauncher.launch(null)
                    FilePickerSelectionMode.Files -> {
                        val mimeTypes = request.toMimeTypes()
                        if (request.allowMultiple) {
                            multipleFileLauncher.launch(mimeTypes)
                        } else {
                            singleFileLauncher.launch(mimeTypes)
                        }
                    }
                    FilePickerSelectionMode.FilesAndDirectories -> Unit
                }
            } catch (error: Throwable) {
                pending = null
                continuation.resumeWith(Result.failure(error))
            }
        }

    private fun complete(uris: List<Uri>, isDirectory: Boolean) {
        val current = pending ?: return
        pending = null
        if (!current.continuation.isActive) return

        val files = uris.mapNotNull { uri ->
            persistReadPermission(uri)
            uri.toPickedFile(isDirectory)?.takeIf { it.matches(current.request) }
        }
        current.continuation.resume(files)
    }

    private fun persistReadPermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: SecurityException) {
            // 某些 provider 只提供目前 Activity/process 的暫時讀取權限，仍可立即上傳。
        }
    }

    private fun Uri.toPickedFile(isDirectory: Boolean): PickedFile? {
        var displayName: String? = null
        var sizeBytes: Long? = null
        contentResolver.query(
            this,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                    displayName = cursor.getString(nameIndex)
                }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        }

        val name = displayName
            ?: lastPathSegment?.substringAfterLast('/')
            ?: return null
        return PickedFile(
            name = name,
            path = toString(),
            sizeBytes = sizeBytes ?: 0L,
            isDirectory = isDirectory,
        )
    }

    private fun PickedFile.matches(request: FilePickerRequest): Boolean {
        if (isDirectory) return request.selectionMode != FilePickerSelectionMode.Files

        val extensionMatches = request.allowedExtensions.isEmpty() ||
            name.substringAfterLast('.', "").lowercase() in
            request.allowedExtensions.map { it.removePrefix(".").lowercase() }.toSet()
        if (!extensionMatches) return false

        val actualMime = contentResolver.getType(Uri.parse(path))
        val mimeMatches = request.allowedMimeTypes.isEmpty() ||
            actualMime != null && request.allowedMimeTypes.any { expected ->
                expected == "*/*" ||
                    expected.equals(actualMime, ignoreCase = true) ||
                    expected.endsWith("/*") &&
                    expected.substringBefore('/') == actualMime.substringBefore('/')
            }
        if (!mimeMatches) return false

        if (request.minFileSizeBytes?.let { sizeBytes < it } == true) return false
        if (request.maxFileSizeBytes?.let { sizeBytes > it } == true) return false
        return true
    }

    private fun FilePickerRequest.toMimeTypes(): Array<String> {
        val configured = allowedMimeTypes.filter { it.isNotBlank() }
        if (configured.isNotEmpty()) return configured.toTypedArray()

        val inferred = allowedExtensions.mapNotNull { extension ->
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                extension.removePrefix(".").lowercase(),
            )
        }.distinct()
        return inferred.ifEmpty { listOf("*/*") }.toTypedArray()
    }

    private data class PendingRequest(
        val request: FilePickerRequest,
        val continuation: CancellableContinuation<List<PickedFile>>,
    )
}
