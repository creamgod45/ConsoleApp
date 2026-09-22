# Compose Desktop 檔案選擇器

Desktop 使用 Compose 自製選擇器，而非 `JFileChooser` 或 `FileDialog`。預設以 `Dialog`
顯示；指定 `FilePickerPresentation.SeparateWindow` 則以獨立、可調整大小的 Compose
`Window` 顯示。關閉任一模式都等同取消選取。資料夾瀏覽、搜尋、
單選／複選、副檔名篩選與確認都由 Compose 呈現；每個檔案的圖示預設透過 JDK 的
`FileSystemView.getSystemIcon()` 取得並快取；圖片檔會優先顯示內容縮圖。

## 在 Desktop 入口安裝

[`desktopApp/src/main/kotlin/cg/creamgod/consoleapp/main.kt`](../desktopApp/src/main/kotlin/cg/creamgod/consoleapp/main.kt)
的完整接線是：

```kotlin
fun main() = application {
    val filePicker = remember { DesktopFilePicker() }

    Window(onCloseRequest = ::exitApplication, title = "ConsoleApp") {
        App(
            filePicker = filePicker,
            dialogHost = { filePicker.Host() },
        )
    }
}
```

`Host()` 要掛在主畫面的 Compose 組合樹中，收到選檔請求時才建立選擇器；App 的
`dialogHost` slot 負責接線，選擇器視窗會沿用目前的 MaterialTheme。若只建立 `DesktopFilePicker()` 卻沒有
掛載 `Host()`，等待選取結果的 coroutine 不會完成。

若要以獨立視窗開啟，於每次請求指定顯示方式；省略時維持原本的 `Dialog`：

```kotlin
val picked = filePicker.pickFiles(
    FilePickerRequest(
        title = "選擇專案圖片",
        presentation = FilePickerPresentation.SeparateWindow,
        allowedMimeTypes = setOf("image/*"),
    ),
)
```

獨立視窗會有作業系統視窗標題列，可移動、調整大小；`Dialog` 則維持依附主視窗的
對話框體驗。Web 不使用此選項，仍由瀏覽器開啟原生檔案選擇視窗。

Desktop 的工具列有「返回上一頁」「前往下一頁」及「上一層資料夾」。前兩者沿用
資料夾瀏覽歷史，也可用滑鼠側鍵的返回／前進鍵觸發；前往新的資料夾後，原先的前進
歷史會清除。檔案清單可單擊選取、雙擊快速確認（複選時只加入選取），右鍵可開啟
選取／開啟資料夾選單。
「返回／前進」走瀏覽歷史，「上一層」才會移除目前路徑的最後一層。若歷史中的
資料夾已不存在，選擇器會退到仍存在的上層資料夾，不會停在失效路徑。

選檔模式預設為 `FilePickerSelectionMode.Files`。若要讓使用者選資料夾，改用：

```kotlin
val picked = filePicker.pickFiles(
    FilePickerRequest(
        title = "選擇匯出資料夾",
        selectionMode = FilePickerSelectionMode.Directories,
        presentation = FilePickerPresentation.SeparateWindow,
    ),
)
val directoryPath = picked.firstOrNull()?.takeIf { it.isDirectory }?.path
```

也可使用 `FilePickerSelectionMode.FilesAndDirectories` 同時選兩者。資料夾模式下單擊選取
資料夾、雙擊進入，底部「選取目前資料夾」可直接回傳目前位置。回傳結果的
`PickedFile.isDirectory` 用來區分資料夾與檔案；資料夾的 `sizeBytes` 為 0。
Web 的原生 `<input type="file">` 不支援回傳資料夾路徑：`Directories` 模式回傳空清單，
`FilesAndDirectories` 模式只會回傳檔案。

## 從表單開啟

```kotlin
@Composable
fun AttachmentField(filePicker: PlatformFilePicker) {
    var files by remember { mutableStateOf(emptyList<PickedFile>()) }
    val scope = rememberCoroutineScope()

    FormFilePicker(
        fileNames = files.map { it.name },
        onPick = {
            scope.launch {
                val picked = filePicker.pickFiles(
                    FilePickerRequest(
                        title = "選擇附件",
                        allowMultiple = true,
                        allowedExtensions = setOf("pdf", "png"),
                        allowedMimeTypes = setOf("application/pdf", "image/png"),
                        minFileSizeBytes = 1,
                        maxFileSizeBytes = 10L * 1024 * 1024,
                        initialViewMode = FilePickerViewMode.Grid,
                        showDetailsPanel = true,
                        imagePreviewSizeDp = 64,
                    ),
                )
                // 取消時回傳空清單；保留原本已選的檔案。
                if (picked.isNotEmpty()) files = picked
            }
        },
        onClear = { files = emptyList() },
        label = "附件",
        allowMultiple = true,
    )
}
```

`FormFilePicker` 只負責表單外觀；Desktop 的 `DesktopFilePicker` 負責顯示對話框與讀取
檔案系統。選取檔名或路徑不等於上傳檔案；真正上傳仍需要讀取檔案內容並交給 repository。

`allowedExtensions` 與 `allowedMimeTypes` 都有設定時，檔案須同時符合兩者；MIME 可寫
`image/*` 等類別萬用字元。無法辨識 MIME 的檔案不通過 MIME 限制。大小上下限以 bytes
計算；資料夾仍可進入，但不符合的檔案不會出現在清單。Desktop 可在對話框中切換
清單／格狀檢視，`initialViewMode` 決定開啟時模式；`showDetailsPanel` 控制右側檔名、
大小、MIME 與路徑欄的初始顯示狀態；使用者也可在對話框中切換側欄。
`imagePreviewSizeDp` 設定圖片檔內容縮圖的初始尺寸，使用者可用 `−`／`+` 調整，範圍為
32–128 dp。圖片縮圖直接從檔案內容讀取，於背景取樣解碼；非圖片檔仍使用固定 24 dp
的系統圖示。圖片無法解碼時也會退回系統圖示。
選擇器底部會直接顯示可選數量、副檔名、MIME 與大小條件；多種限制須同時符合，
不符合條件的檔案不會出現在清單，但資料夾仍可瀏覽。
Web 只有瀏覽器原生檔案視窗，不支援檢視模式、詳細資訊欄或縮圖大小設定。

## 側邊欄捷徑與自訂按鈕

側邊欄會顯示存在的個人資料夾、桌面、文件、下載、影片、音樂、圖片及磁碟根目錄。
資料夾不存在時不顯示；同一路徑只顯示一次。捷徑過多時可向下捲動。
每次開啟對話框都能用 `FilePickerRequest.quickPaths: List<String>` 加入業務資料夾：

```kotlin
val picked = filePicker.pickFiles(
    FilePickerRequest(
        title = "匯入素材",
        quickPaths = listOf(
            projectDirectory.toString(),
            archiveDirectory.toString(),
        ),
    ),
)
```

此處的 `projectDirectory`、`archiveDirectory` 是呼叫端已有的 `Path`。自訂捷徑以資料夾名稱顯示；
空字串、無效路徑及不存在的資料夾會略過。相對路徑會以程式的工作目錄為基準，建議使用絕對路徑。
`quickPaths` 只影響 Desktop 側邊欄，Web 仍由瀏覽器的檔案視窗處理。

若需要自己的按鈕樣式，在建立 `DesktopFilePicker` 時提供 `shortcutButton`。它會套用到內建和
自訂捷徑；第三個參數 `onClick` 必須交給按鈕，才能切換目前資料夾：

```kotlin
val filePicker = remember {
    DesktopFilePicker(
        shortcutButton = { label, path, onClick ->
            TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
    )
}
```

`path` 是捷徑對應的 `java.nio.file.Path`，可用於顯示路徑提示或決定圖示。上例需要
`TextButton`、`Text`、`Modifier` 與 `TextOverflow` 的 Compose import；若不傳
`shortcutButton`，框架會使用內建的 `TextButton`。

## 圖示策略

預設 `DesktopFilePicker()` 對非圖片檔使用作業系統圖示。若特定業務檔案需要自己的圖示，可以注入
`iconProvider`；回傳 `null` 時清單使用內建的通用檔案／資料夾圖示。

```kotlin
fun createFilePicker(myFormatImage: ImageBitmap): DesktopFilePicker =
    DesktopFilePicker(
        iconProvider = { file ->
            if (file.extension.equals("myformat", ignoreCase = true)) myFormatImage else null
        },
    )
```

此處的 `myFormatImage` 是函式參數，必須由呼叫端提供真正的 `ImageBitmap`。
系統圖示載入發生在背景，清單先顯示通用圖示；已載入結果在同一個對話框中快取。

## Web 差異

Web 仍使用瀏覽器原生 `<input type="file">` 授權視窗。瀏覽器不允許頁面自行列出本機
資料夾，也不提供作業系統檔案關聯圖示。`allowedExtensions` 會設定 `accept`，但仍應在
業務驗證與伺服器端再次檢查副檔名、MIME、檔案大小與實際內容；副檔名及瀏覽器
提供的 MIME 都不能作為安全驗證的唯一依據。
