package cg.creamgod.consoleapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Chip
import androidx.compose.material.ChipColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isBackPressed
import androidx.compose.ui.input.pointer.isForwardPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.swing.filechooser.FileSystemView

internal data class FileChooserEntry(
    val path: Path,
    val directory: Boolean,
    val sizeBytes: Long?,
    val mimeType: String? = null,
) {
    val name: String get() = path.fileName?.toString() ?: path.toString()
}

internal fun FileChooserEntry.toPickedFile(): PickedFile =
    PickedFile(name = name, path = path.toAbsolutePath().toString(), sizeBytes = sizeBytes ?: 0L, isDirectory = directory)

internal fun existingDirectoryOrAncestor(path: Path): Path? {
    // 歷史只接受完整路徑；舊版誤存的片段不可相對於程式工作目錄解析。
    if (!path.isAbsolute) return null
    var candidate: Path? = path.normalize()
    while (candidate != null) {
        if (Files.isDirectory(candidate)) return candidate
        candidate = candidate.parent
    }
    return null
}

internal data class DirectoryHistory(
    val current: Path,
    val backStack: List<Path> = emptyList(),
    val forwardStack: List<Path> = emptyList(),
) {
    fun visit(path: Path): DirectoryHistory {
        val target = existingDirectoryOrAncestor(path) ?: return this
        if (target == current) return this
        return copy(current = target, backStack = (backStack + listOf(current)).takeLast(100), forwardStack = emptyList())
    }

    fun back(): DirectoryHistory {
        var remaining = backStack
        while (remaining.isNotEmpty()) {
            val target = existingDirectoryOrAncestor(remaining.last())
            remaining = remaining.dropLast(1)
            if (target != null && target != current) {
                val returnTo = existingDirectoryOrAncestor(current)
                return copy(
                    current = target,
                    backStack = remaining,
                    forwardStack = if (returnTo == null || returnTo == target) forwardStack else forwardStack + listOf(returnTo),
                )
            }
        }
        return copy(backStack = emptyList())
    }

    fun forward(): DirectoryHistory {
        var remaining = forwardStack
        while (remaining.isNotEmpty()) {
            val target = existingDirectoryOrAncestor(remaining.last())
            remaining = remaining.dropLast(1)
            if (target != null && target != current) {
                val returnTo = existingDirectoryOrAncestor(current)
                return copy(
                    current = target,
                    backStack = if (returnTo == null || returnTo == target) backStack else (backStack + listOf(returnTo)).takeLast(100),
                    forwardStack = remaining,
                )
            }
        }
        return copy(forwardStack = emptyList())
    }
}

internal fun listDirectory(path: Path): List<FileChooserEntry> =
    Files.newDirectoryStream(path).use { stream ->
        stream.mapNotNull { child ->
            runCatching {
                val isDirectory = Files.isDirectory(child)
                FileChooserEntry(
                    path = child,
                    directory = isDirectory,
                    sizeBytes = if (isDirectory) null else Files.size(child),
                    mimeType = if (isDirectory) null else runCatching { Files.probeContentType(child) }.getOrNull(),
                )
            }.getOrNull()
        }.sortedWith(compareBy<FileChooserEntry> { !it.directory }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            .toList()
    }

internal fun FileChooserEntry.matches(request: FilePickerRequest): Boolean {
    if (directory) return true
    if (request.selectionMode == FilePickerSelectionMode.Directories) return false
    val matchesExtension = request.allowedExtensions.isEmpty() || request.allowedExtensions.any { extension ->
        name.endsWith(".${extension.removePrefix(".")}", ignoreCase = true)
    }
    val matchesMime = request.allowedMimeTypes.isEmpty() || request.allowedMimeTypes.any { allowed ->
        allowed.equals(mimeType, ignoreCase = true) ||
            (allowed.endsWith("/*") && mimeType?.startsWith(allowed.removeSuffix("*"), ignoreCase = true) == true)
    }
    val matchesSize = (request.minFileSizeBytes == null || (sizeBytes ?: 0L) >= request.minFileSizeBytes!!) &&
        (request.maxFileSizeBytes == null || (sizeBytes ?: Long.MAX_VALUE) <= request.maxFileSizeBytes!!)
    return matchesExtension && matchesMime && matchesSize
}

internal fun describeFileSelection(request: FilePickerRequest): String {
    fun formatSize(bytes: Long): String = when {
        bytes >= 1024L * 1024 && bytes % (1024L * 1024) == 0L -> "${bytes / (1024L * 1024)} MB"
        bytes >= 1024L && bytes % 1024L == 0L -> "${bytes / 1024L} KB"
        else -> "$bytes B"
    }
    val extensions = request.allowedExtensions
        .map { ".${it.removePrefix(".")}" }
        .sorted()
        .joinToString("、")
        .ifEmpty { "不限" }
    val mimeTypes = request.allowedMimeTypes.sorted().joinToString("、").ifEmpty { "不限" }
    val size = when {
        request.minFileSizeBytes != null && request.maxFileSizeBytes != null ->
            "${formatSize(request.minFileSizeBytes!!)}～${formatSize(request.maxFileSizeBytes!!)}"
        request.minFileSizeBytes != null -> "至少 ${formatSize(request.minFileSizeBytes!!)}"
        request.maxFileSizeBytes != null -> "至多 ${formatSize(request.maxFileSizeBytes!!)}"
        else -> "不限"
    }
    val count = "數量：${if (request.allowMultiple) "可選多個" else "只能選一個"}"
    if (request.selectionMode == FilePickerSelectionMode.Directories) return "可選項目：資料夾；$count；可直接選取目前資料夾"
    val kind = if (request.selectionMode == FilePickerSelectionMode.FilesAndDirectories) "檔案與資料夾" else "檔案"
    return "可選項目：$kind；$count；檔案副檔名：$extensions；MIME：$mimeTypes；檔案大小：$size"
}

internal enum class ShortcutSection { Desktop, Personal, Roots, Custom }

internal data class ShortcutLocation(val label: String, val path: Path, val section: ShortcutSection)

internal fun buildShortcutLocations(
    home: Path,
    desktop: Path,
    documents: Path,
    roots: List<Path>,
    quickPaths: List<String>,
): List<ShortcutLocation> {
    val locations = linkedMapOf<Path, ShortcutLocation>()
    fun add(label: String, path: Path, section: ShortcutSection) {
        val normalized = path.toAbsolutePath().normalize()
        if (Files.isDirectory(normalized)) locations.putIfAbsent(normalized, ShortcutLocation(label, normalized, section))
    }

    add("桌面", desktop, ShortcutSection.Desktop)
    add("個人資料夾", home, ShortcutSection.Personal)
    add("文件", documents, ShortcutSection.Personal)
    add("下載", home.resolve("Downloads"), ShortcutSection.Personal)
    add("影片", home.resolve("Videos"), ShortcutSection.Personal)
    add("音樂", home.resolve("Music"), ShortcutSection.Personal)
    add("圖片", home.resolve("Pictures"), ShortcutSection.Personal)
    roots.forEach { add(it.toString(), it, ShortcutSection.Roots) }
    quickPaths.forEach { raw ->
        if (raw.isNotBlank()) runCatching { Path.of(raw) }.getOrNull()?.let { path ->
            add(path.fileName?.toString() ?: path.toString(), path, ShortcutSection.Custom)
        }
    }
    return locations.values.toList()
}

internal suspend fun loadSystemFileIcon(file: File): ImageBitmap? = withContext(Dispatchers.IO) {
    runCatching {
        val icon = FileSystemView.getFileSystemView().getSystemIcon(file) ?: return@runCatching null
        val image = BufferedImage(icon.iconWidth.coerceAtLeast(1), icon.iconHeight.coerceAtLeast(1), BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            icon.paintIcon(null, graphics, 0, 0)
        } finally {
            graphics.dispose()
        }
        image.toComposeImageBitmap()
    }.getOrNull()
}

internal val FileChooserEntry.canPreviewImage: Boolean
    get() = !directory && (
        mimeType?.startsWith("image/", ignoreCase = true) == true ||
            path.fileName?.toString()?.substringAfterLast('.', "")?.lowercase() in setOf("png", "jpg", "jpeg", "gif", "bmp", "wbmp")
        )

/** 解碼時先取樣，避免為了小縮圖將大型原圖完整載入記憶體。 */
internal fun readImageThumbnail(file: File, maxEdgePixels: Int = 256): BufferedImage? = runCatching {
    require(maxEdgePixels > 0)
    ImageIO.createImageInputStream(file)?.use { input ->
        val readers = ImageIO.getImageReaders(input)
        if (!readers.hasNext()) return@use null
        val reader = readers.next()
        try {
            reader.input = input
            val longestEdge = maxOf(reader.getWidth(0), reader.getHeight(0)).toLong()
            val sample = ((longestEdge + maxEdgePixels - 1) / maxEdgePixels).coerceAtLeast(1).toInt()
            reader.read(0, reader.defaultReadParam.apply { setSourceSubsampling(sample, sample, 0, 0) })
        } finally {
            reader.dispose()
        }
    }
}.getOrNull()

private suspend fun loadImageThumbnail(file: File): ImageBitmap? = withContext(Dispatchers.IO) {
    readImageThumbnail(file)?.toComposeImageBitmap()
}

@ExperimentalMaterialApi
class ChipColorBasic(
    val bgColor: State<Color> = mutableStateOf(Color.Gray),
    val contColor: State<Color> = mutableStateOf(Color.Black),
    val leadColor: State<Color> = mutableStateOf(Color.Green),
) : ChipColors {

    @Composable
    override fun backgroundColor(enabled: Boolean): State<Color> {
        return bgColor
    }

    @Composable
    override fun contentColor(enabled: Boolean): State<Color> {
        return contColor
    }

    @Composable
    override fun leadingIconContentColor(enabled: Boolean): State<Color> {
        return leadColor
    }

}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
internal fun DesktopFileChooser(
    request: FilePickerRequest,
    iconProvider: suspend (File) -> ImageBitmap?,
    shortcutButton: @Composable (label: String, path: Path, onClick: () -> Unit) -> Unit,
    onResult: (List<PickedFile>) -> Unit,
) {
    var history by remember { mutableStateOf(DirectoryHistory(Path.of(System.getProperty("user.home")))) }
    val directory = history.current
    var entries by remember { mutableStateOf<List<FileChooserEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Set<Path>>(emptySet()) }
    var viewMode by remember(request) { mutableStateOf(request.initialViewMode) }
    var detailsVisible by remember(request) { mutableStateOf(request.showDetailsPanel) }
    var imagePreviewSizeDp by remember(request) { mutableIntStateOf(request.imagePreviewSizeDp.coerceIn(32, 128)) }
    val imagePreviewSize = imagePreviewSizeDp.dp
    val roots = remember { File.listRoots().map { it.toPath() } }
    val shortcuts = remember(request.quickPaths, roots) {
        val fileSystemView = FileSystemView.getFileSystemView()
        val home = Path.of(System.getProperty("user.home"))
        val defaultDocuments = fileSystemView.defaultDirectory.toPath()
        val homeDocuments = home.resolve("Documents")
        val documents = if (Files.isDirectory(homeDocuments)) homeDocuments else defaultDocuments
        buildShortcutLocations(home, fileSystemView.homeDirectory.toPath(), documents, roots, request.quickPaths)
    }
    val iconCache = remember { mutableMapOf<Path, ImageBitmap?>() }
    val thumbnailCache = remember { linkedMapOf<Path, ImageBitmap?>() }

    fun visit(path: Path) {
        history = history.visit(path)
        query = ""
    }

    fun goBack() {
        history = history.back()
        query = ""
    }

    fun goForward() {
        history = history.forward()
        query = ""
    }

    LaunchedEffect(directory) {
        loading = true
        error = null
        selected = emptySet()
        runCatching { withContext(Dispatchers.IO) { listDirectory(directory) } }
            .onSuccess { entries = it }
            .onFailure { cause ->
                entries = emptyList()
                error = cause.message ?: "無法讀取資料夾"
            }
        loading = false
    }

    val chooserContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxSize()
                .onPointerEvent(PointerEventType.Press) { event ->
                    when {
                        event.buttons.isBackPressed -> goBack()
                        event.buttons.isForwardPressed -> goForward()
                    }
                }
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(request.title, style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = ::goBack, enabled = history.backStack.isNotEmpty()) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回上一頁")
                }
                IconButton(onClick = ::goForward, enabled = history.forwardStack.isNotEmpty()) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "前往下一頁")
                }
                IconButton(onClick = { directory.parent?.let(::visit) }, enabled = directory.parent != null) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "上一層資料夾")
                }
                Text(directory.toString(), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = { visit(Path.of(System.getProperty("user.home"))) }) {
                    Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = "個人資料夾")
                }
                IconButton(onClick = { visit(Path.of(FileSystemView.getFileSystemView().homeDirectory.absolutePath)) }) {
                    Icon(imageVector = if (OSDetector.isWindows) Icons.Filled.DesktopWindows else Icons.Filled.DesktopMac, contentDescription = "桌面")
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("搜尋目前資料夾") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("檢視：", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(onClick = { viewMode = FilePickerViewMode.List }, colors = ChipColorBasic(bgColor = mutableStateOf(Color(0xFF1CA531)))) { Text(if (viewMode == FilePickerViewMode.List) "✓ 清單" else "清單") }
                    Chip(onClick = { viewMode = FilePickerViewMode.Grid }, colors = ChipColorBasic(bgColor = mutableStateOf(Color(0xFF1CA531)))) { Text(if (viewMode == FilePickerViewMode.Grid) "✓ 格狀" else "格狀") }
                    TextButton(onClick = { detailsVisible = !detailsVisible }) { Text(if (detailsVisible) "✓ 詳細資訊" else "詳細資訊") }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { imagePreviewSizeDp = (imagePreviewSizeDp - 8).coerceAtLeast(32) }, enabled = imagePreviewSizeDp > 32) { Text("−") }
                Text("圖片預覽 $imagePreviewSizeDp dp", style = MaterialTheme.typography.labelSmall)
                TextButton(onClick = { imagePreviewSizeDp = (imagePreviewSizeDp + 8).coerceAtMost(128) }, enabled = imagePreviewSizeDp < 128) { Text("+") }
            }
            HorizontalDivider()
            Row(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.width(160.dp).fillMaxHeight().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("位置", style = MaterialTheme.typography.labelLarge)
                    for ((index, location) in shortcuts.withIndex()) {
                        if (index > 0 && shortcuts[index - 1].section != location.section) HorizontalDivider()
                        shortcutButton(location.label, location.path) { visit(location.path) }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when {
                        loading -> Text("載入中…", modifier = Modifier.align(Alignment.Center))
                        error != null -> Text(error ?: "", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                        else -> {
                            val visible = entries.filter {
                                it.matches(request) && it.name.contains(query, ignoreCase = true)
                            }
                            if (visible.isEmpty()) {
                                Text(
                                    if (request.selectionMode == FilePickerSelectionMode.Directories) "目前資料夾沒有子資料夾，可選取目前資料夾"
                                    else "目前資料夾沒有符合選擇條件的檔案",
                                    modifier = Modifier.align(Alignment.Center),
                                )
                            } else {
                                val onEntryClick: (FileChooserEntry) -> Unit = { entry ->
                                    if (entry.directory && request.selectionMode == FilePickerSelectionMode.Files) {
                                        visit(entry.path)
                                    } else if (request.allowMultiple) {
                                        selected = if (entry.path in selected) selected - entry.path else selected + entry.path
                                    } else {
                                        selected = setOf(entry.path)
                                    }
                                }
                                val onEntryDoubleClick: (FileChooserEntry) -> Unit = { entry ->
                                    if (entry.directory) {
                                        visit(entry.path)
                                    } else if (request.allowMultiple) {
                                        selected = selected + entry.path
                                    } else {
                                        onResult(listOf(entry.toPickedFile()))
                                    }
                                }
                                if (viewMode == FilePickerViewMode.List) {
                                    LazyColumn {
                                        items(visible, key = { it.path.toString() }) { entry ->
                                            FileEntryRow(
                                                entry, entry.path in selected, imagePreviewSize, iconProvider, iconCache, thumbnailCache,
                                                canSelectDirectory = request.selectionMode != FilePickerSelectionMode.Files,
                                                onClick = { onEntryClick(entry) },
                                                onDoubleClick = { onEntryDoubleClick(entry) },
                                            )
                                        }
                                    }
                                } else {
                                    LazyVerticalGrid(columns = GridCells.Adaptive((imagePreviewSize.value + 100).dp)) {
                                        items(visible.size, key = { visible[it].path.toString() }) { index ->
                                            val entry = visible[index]
                                            FileEntryTile(
                                                entry, entry.path in selected, imagePreviewSize, iconProvider, iconCache, thumbnailCache,
                                                canSelectDirectory = request.selectionMode != FilePickerSelectionMode.Files,
                                                onClick = { onEntryClick(entry) },
                                                onDoubleClick = { onEntryDoubleClick(entry) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (detailsVisible) {
                    Spacer(Modifier.width(12.dp))
                    val current = entries.firstOrNull { it.path == selected.firstOrNull() }
                    Column(modifier = Modifier.width(180.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("檔案詳細資訊", style = MaterialTheme.typography.titleSmall)
                        if (current == null) {
                            Text("選取檔案以檢視詳細資訊", style = MaterialTheme.typography.bodySmall)
                        } else {
                            FileEntryVisual(current, imagePreviewSize, iconProvider, iconCache, thumbnailCache)
                            Text(current.name, style = MaterialTheme.typography.bodyMedium)
                            Text("大小：${current.sizeBytes ?: 0L} bytes", style = MaterialTheme.typography.bodySmall)
                            Text("類型：${current.mimeType ?: "未知"}", style = MaterialTheme.typography.bodySmall)
                            Text(current.path.toString(), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            HorizontalDivider()
            Text("可選擇條件（檔案須同時符合類型與大小限制；資料夾可瀏覽）", style = MaterialTheme.typography.labelMedium)
            Text(describeFileSelection(request), style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("已選擇 ${selected.size} 個項目", modifier = Modifier.weight(1f))
                if (request.selectionMode != FilePickerSelectionMode.Files) {
                    TextButton(onClick = { onResult(listOf(FileChooserEntry(directory, true, null).toPickedFile())) }) {
                        Text("選取目前資料夾")
                    }
                }
                OutlinedButton(onClick = { onResult(emptyList()) }) { Text("取消") }
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = selected.isNotEmpty() && selected.all { path ->
                        request.maxFileSizeBytes == null || entries.firstOrNull { it.path == path }?.let { entry ->
                            entry.directory || (entry.sizeBytes ?: Long.MAX_VALUE) <= request.maxFileSizeBytes!!
                        } == true
                    },
                    onClick = {
                        onResult(selected.mapNotNull { path -> entries.firstOrNull { it.path == path }?.toPickedFile() })
                    },
                ) { Text("選擇") }
            }
            if (selected.isNotEmpty() && request.maxFileSizeBytes != null && selected.any { path ->
                    entries.firstOrNull { it.path == path }?.let { entry ->
                        !entry.directory && (entry.sizeBytes ?: Long.MAX_VALUE) > request.maxFileSizeBytes!!
                    } == true
                }) {
                Text("選取的檔案超過大小限制", color = MaterialTheme.colorScheme.error)
            }
        }
    }
    if (request.presentation == FilePickerPresentation.SeparateWindow) {
        val colorScheme = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography
        val shapes = MaterialTheme.shapes
        Window(
            onCloseRequest = { onResult(emptyList()) },
            title = request.title,
            state = rememberWindowState(width = 860.dp, height = 620.dp),
        ) {
            MaterialTheme(colorScheme = colorScheme, typography = typography, shapes = shapes) {
                Surface(modifier = Modifier.fillMaxSize()) { chooserContent() }
            }
        }
    } else {
        Dialog(
            onDismissRequest = { onResult(emptyList()) },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                modifier = Modifier.width(860.dp).height(620.dp),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp,
            ) { chooserContent() }
        }
    }
}

@Composable
internal fun DefaultShortcutButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun FileEntryRow(
    entry: FileChooserEntry,
    checked: Boolean,
    imagePreviewSize: Dp,
    iconProvider: suspend (File) -> ImageBitmap?,
    iconCache: MutableMap<Path, ImageBitmap?>,
    thumbnailCache: MutableMap<Path, ImageBitmap?>,
    canSelectDirectory: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
) {
    FileEntryMouseArea(entry, checked, canSelectDirectory, onClick, onDoubleClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FileEntryVisual(entry, imagePreviewSize, iconProvider, iconCache, thumbnailCache)
            Spacer(Modifier.width(12.dp))
            Text(entry.name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!entry.directory) {
                Text("${(entry.sizeBytes ?: 0L) / 1024} KB", style = MaterialTheme.typography.bodySmall)
                Text(if (checked) "✓" else "○", modifier = Modifier.padding(start = 12.dp))
            }
        }
    }
}

@Composable
private fun FileEntryTile(
    entry: FileChooserEntry,
    checked: Boolean,
    imagePreviewSize: Dp,
    iconProvider: suspend (File) -> ImageBitmap?,
    iconCache: MutableMap<Path, ImageBitmap?>,
    thumbnailCache: MutableMap<Path, ImageBitmap?>,
    canSelectDirectory: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
) {
    FileEntryMouseArea(entry, checked, canSelectDirectory, onClick, onDoubleClick) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FileEntryVisual(entry, imagePreviewSize, iconProvider, iconCache, thumbnailCache)
            Text(entry.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (checked) Text("✓", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun FileEntryMouseArea(
    entry: FileChooserEntry,
    checked: Boolean,
    canSelectDirectory: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    var menuOpen by remember(entry.path) { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxWidth()
            .onPointerEvent(PointerEventType.Press) { event ->
                if (event.buttons.isSecondaryPressed) menuOpen = true
            }
            .combinedClickable(onClick = onClick, onDoubleClick = onDoubleClick),
    ) {
        content()
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            if (entry.directory) {
                DropdownMenuItem(text = { Text("開啟資料夾") }, onClick = {
                    menuOpen = false
                    onDoubleClick()
                })
            }
            if (!entry.directory || canSelectDirectory) {
                DropdownMenuItem(
                    text = { Text(if (checked) "取消選取" else if (entry.directory) "選取資料夾" else "選取檔案") },
                    onClick = {
                        menuOpen = false
                        onClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun FileEntryVisual(
    entry: FileChooserEntry,
    imagePreviewSize: Dp,
    iconProvider: suspend (File) -> ImageBitmap?,
    iconCache: MutableMap<Path, ImageBitmap?>,
    thumbnailCache: MutableMap<Path, ImageBitmap?>,
) {
    if (entry.canPreviewImage) {
        val thumbnail by produceState<ImageBitmap?>(null, entry.path) {
            value = if (thumbnailCache.containsKey(entry.path)) {
                thumbnailCache[entry.path]
            } else {
                loadImageThumbnail(entry.path.toFile()).also { loaded ->
                    if (thumbnailCache.size >= 128) thumbnailCache.remove(thumbnailCache.keys.first())
                    thumbnailCache[entry.path] = loaded
                }
            }
        }
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail!!,
                contentDescription = "${entry.name} 預覽",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(imagePreviewSize),
            )
            return
        }
    }
    val icon by produceState<ImageBitmap?>(null, entry.path, iconProvider) {
        value = if (iconCache.containsKey(entry.path)) {
            iconCache[entry.path]
        } else {
            iconProvider(entry.path.toFile()).also { iconCache[entry.path] = it }
        }
    }
    if (icon != null) {
        Image(bitmap = icon!!, contentDescription = null, modifier = Modifier.size(24.dp))
    } else {
        Icon(
            imageVector = if (entry.directory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}
