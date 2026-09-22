package cg.creamgod.consoleapp

data class PickedFile(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val isDirectory: Boolean = false,
)

enum class FilePickerViewMode { List, Grid }

enum class FilePickerSelectionMode { Files, Directories, FilesAndDirectories }

/** Desktop 顯示方式；Web 一律使用瀏覽器原生檔案視窗。 */
enum class FilePickerPresentation { Dialog, SeparateWindow }

data class FilePickerRequest(
    val title: String = "選擇檔案",
    val allowMultiple: Boolean = false,
    val allowedExtensions: Set<String> = emptySet(),
    val maxFileSizeBytes: Long? = null,
    /** Desktop 側邊欄的額外資料夾路徑；其他平台會忽略。 */
    val quickPaths: List<String> = emptyList(),
    /** 可填完整 MIME 或類別萬用字元；未知 MIME 類型不會通過篩選。 */
    val allowedMimeTypes: Set<String> = emptySet(),
    val minFileSizeBytes: Long? = null,
    val initialViewMode: FilePickerViewMode = FilePickerViewMode.List,
    val showDetailsPanel: Boolean = true,
    /** Desktop 圖片檔內容縮圖的初始邊長（dp）；不影響一般檔案的系統圖示。 */
    val imagePreviewSizeDp: Int = 64,
    val presentation: FilePickerPresentation = FilePickerPresentation.Dialog,
    /** Desktop 可選檔案、資料夾，或兩者皆可；Web 只能選檔案。 */
    val selectionMode: FilePickerSelectionMode = FilePickerSelectionMode.Files,
)

interface PlatformFilePicker {
    suspend fun pickFiles(allowMultiple: Boolean): List<PickedFile>

    suspend fun pickFiles(request: FilePickerRequest): List<PickedFile> =
        pickFiles(request.allowMultiple)
}

/**
 * Safe fallback for previews and platforms that have not installed a native picker adapter.
 */
object UnsupportedPlatformFilePicker : PlatformFilePicker {
    override suspend fun pickFiles(allowMultiple: Boolean): List<PickedFile> = emptyList()
}
