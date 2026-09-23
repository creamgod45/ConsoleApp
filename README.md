# ConsoleApp Design System

以 Kotlin Multiplatform 與 Compose Multiplatform 建立的跨平台設計系統，同一套
`commonMain` 元件可用於 Desktop JVM、JavaScript 與 Wasm。

底層採用 Material 3 的 theme、互動與無障礙能力，公開 API 則保留接近
Bootstrap／HTML framework 的短名稱與語意，讓畫面能快速組裝：

```kotlin
var profileSaved by remember { mutableStateOf(false) }

Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    H1("Account settings")
    Lead("Manage profile, security, and notification preferences.")
    if (profileSaved) {
        Alert(
            message = "設定已更新",
            tone = Tone.Success,
            dismissible = true,
            onDismiss = { profileSaved = false },
        )
    }

    Card(title = "Profile", subtitle = "Public account information") {
        Paragraph("Changes are shared across all signed-in devices.")
    }

    Button("儲存", onClick = { profileSaved = true })
}
```

## 功能特色

- Compose-first：不是 HTML/CSS wrapper，Desktop 與 Web 使用相同 Composable。
- Material 3：顏色、排版、shape、surface 與互動狀態跟隨 `MaterialTheme`。
- Framework 式 API：`Button`、`Card`、`Navbar`、`Modal`、`H1`、`Paragraph` 等短名稱。
- Controlled state：表單、導覽、Dialog 與 Overlay 狀態由 screen 或 ViewModel 管理。
- 完整表單 helper：輸入、密碼、多行、選擇、switch、range、layout 與驗證。
- 文件系統：強型別 Document DSL、Markdown parser、Material renderer 與 resource loader。
- App 內元件目錄：`ComponentGuide()` 可即時預覽元件、文章與 Markdown。

## 快速開始

### Packaging the desktop app

`jpackage` (which Compose Desktop drives under the hood) cannot cross-compile: each installer
format can only be produced on its own OS. Building on Linux gives you `.deb` / `.rpm`, building
on Windows gives you `.msi`, building on macOS gives you `.dmg`. Incompatible formats are skipped
automatically on the current platform.

- Native installer for the current OS: `./gradlew :desktopApp:packageDistributionForCurrentOS`
- Linux specifically: `./gradlew :desktopApp:packageDeb` / `./gradlew :desktopApp:packageRpm`
  (needs `fakeroot` + `dpkg` for `.deb`, and `rpmbuild` for `.rpm`)
- Portable build with a bundled JRE — no installer, no Java required on the target machine:
  `./gradlew :desktopApp:createDistributable`, then run
  `desktopApp/build/compose/binaries/main/app/cg.creamgod.consoleapp/bin/cg.creamgod.consoleapp`
- Try the packaged build without installing it: `./gradlew :desktopApp:runDistributable`

Override the version with `-PappVersion=1.2.3`; it must be `x.y.z`, since `jpackage` rejects
anything else for `.msi` and `.dmg`.

### Releasing

`.github/workflows/release.yml` builds all three platforms in parallel (Linux x64, Windows x64,
macOS arm64 and x64) and uploads the installers plus portable archives to a GitHub Release.

- Push a tag: `git tag v1.0.0 && git push origin v1.0.0` — publishes the Release directly.
- Or run the **Release Desktop Packages** workflow manually from the Actions tab and type the
  version; it creates a draft Release by default so you can check the artifacts first.

Release notes come from `.github/release-notes-template.md` (`{{VERSION}}` is substituted).
The artifacts are unsigned, so Windows SmartScreen and macOS Gatekeeper will warn about them.

```kotlin
import cg.creamgod.consoleapp.designsystem.components.*
import cg.creamgod.consoleapp.designsystem.components.form.*
import cg.creamgod.consoleapp.designsystem.content.*
import cg.creamgod.consoleapp.designsystem.patterns.article.*
```

如果也匯入 Material 3 的同名元件，請使用 alias：

```kotlin
import cg.creamgod.consoleapp.designsystem.components.Button as AppButton
import androidx.compose.material3.Button as MaterialButton
```

### 表單與驗證

```kotlin
var email by remember { mutableStateOf("") }
val validation = FormValidators.all(
    FormValidators.required(),
    FormValidators.email(),
).validate(email)

FormTextInput(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    type = FormInputType.Email,
    required = true,
    errorText = validation.message,
    modifier = Modifier.fillMaxWidth(),
)
```

### 受控確認視窗

```kotlin
var showDelete by remember { mutableStateOf(false) }

Confirm(
    visible = showDelete,
    title = "刪除資料？",
    message = "這個動作無法復原。",
    destructive = true,
    onConfirm = {
        delete()
        showDelete = false
    },
    onDismiss = { showDelete = false },
)
```

### Markdown resource

把文件放在 `shared/src/commonMain/composeResources/files/docs`，然後直接渲染：

```kotlin
MarkdownResource("docs/getting-started.md")
```

或使用強型別 DSL：

```kotlin
val quickStart = document {
    h1("Quick start")
    paragraph(text("Use "), strong("semantic"), text(" content."))
    orderedList("Install", "Run", "Verify")
    code(
        "Button(text = \"Save\", onClick = { profileSaved = true })",
        language = "kotlin",
    )
}

DocumentRenderer(quickStart)
```

## 元件一覽

| 分類 | 公開 API |
| --- | --- |
| Semantic content | `H1`–`H6`, `Paragraph`, `Lead`, `Caption`, `SmallText` |
| Documents | `DocumentRenderer`, `MarkdownDocument`, `MarkdownResource`, `document` |
| Actions | `Button`, `ButtonGroup`, `IconButton`, `FloatingActionButton`, `ExtendedFloatingActionButton`, `FabMenu`, `SegmentedButtons`, `SplitButton`, `Dropdown` |
| Feedback | `Alert`, `Callout`, `Badge`, `Progress`, `Spinner`, `LoadingIndicator`, `Snackbar`, `SnackbarHost`, `Placeholder`, `Toast`, `ToastHost` |
| Disclosure | `Collapse`, `Accordion` |
| Surfaces | `Card`, `ListGroup`, `Carousel` |
| Navigation | `NavigationBar`, `NavigationRail`, `NavigationDrawer`, `TopAppBar`, `BottomAppBar`, `Toolbar`, `Breadcrumb`, `Navbar`, `Tabs`, `Nav`, `Pagination`, `ScrollSpy` |
| Forms | `DatePicker`, `DateRangePicker`, `TimePicker`, `Search`, `AssistChip`, `FilterChip`, `InputChip`, `SuggestionChip`, `FormTextInput`, `FormPasswordInput`, `FormTextArea`, `FormSelect`, `FormMultiSelect`, `FormCheckbox`, `FormRadioGroup`, `FormSwitch`, `FormRange`, `FormSection`, `FormInputGroup`, `FormFilePicker`, `FormActions` |
| Overlay | `BottomSheet`, `SideSheet`, `Popup`, `Modal`, `Confirm`, `Ask`, `Choice`, `Offcanvas`, `Popover`, `Tooltip` |
| Structure | `HorizontalDivider`, `VerticalDivider` |
| Articles | `GuideArticle`, `ArticleStep`, `ArticleCallout`, `CodeBlock` |
| Catalog | `ComponentGuide`, `bootstrapComponentCatalog` |

## 文件

- [完整元件使用手冊](./docs/components.md) — 所有公開元件、state model 與範例。
- [Material 3 元件與實務範例](./docs/material3-components.md) — 新增元件的參數、狀態與完整業務接法。
- [元件 API 與參數參考](./docs/component-reference.md) — 每個參數、預設值、狀態責任與適用情境。
- [業務情境實作](./docs/business-recipes.md) — 表單、刪除、搜尋分頁、附件、部署與說明中心。
- [帳號設定完整教學](./docs/tutorial-account-settings.md) — 從資料模型、驗證、helper 到 repository 與測試。
- [Component guide](./docs/component-guide.md) — 設計哲學、規範與索引。
- [Semantic content and Markdown](./docs/content-guide.md) — HTML-like 排版與資源載入。
- [Guided article authoring](./docs/article-guide.md) — 教學文章類型與結構。
- [Component library reference](./docs/component-library.md) — 架構與 rollout。
- [GitHub Wiki 首頁原稿](./wiki/Home.md) — 可同步到獨立 Wiki repository。
- App 內預覽：`cg.creamgod.consoleapp.designsystem.catalog.ComponentGuide()`。

## 執行專案

```shell
# Desktop hot reload / standard run
./gradlew :desktopApp:hotRun --auto
./gradlew :desktopApp:run

# Web
./gradlew :webApp:wasmJsBrowserDevelopmentRun
./gradlew :webApp:jsBrowserDevelopmentRun
```

Windows PowerShell 使用 `./gradlew.bat`。

## 測試

```shell
./gradlew :shared:jvmTest
./gradlew :shared:jsTest
./gradlew :shared:wasmJsTest
```

## 專案結構

```text
shared/src/commonMain/
├── composeResources/                 # 共用字型、圖片與 Markdown
└── kotlin/cg/creamgod/consoleapp/designsystem/
    ├── catalog/                      # App 內元件目錄與 coverage
    ├── components/                   # Actions、forms、feedback、navigation…
    ├── content/                      # Typography、Document、Markdown
    ├── foundation/                   # 共用語意與狀態 model
    ├── patterns/article/             # Guide article pattern
    └── tokens/                       # Spacing、radius、breakpoints
```

延伸閱讀：[Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)、
[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) 與
[Kotlin/Wasm](https://kotl.in/wasm)。
