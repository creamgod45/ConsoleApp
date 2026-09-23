# 完整元件使用手冊

本文件涵蓋 ConsoleApp Design System 所有公開、非相容層元件。範例預設位於
Composable 內容中，並已套用 `MaterialTheme`。

本頁是可複製的 cookbook。要查每個參數、預設值與 state contract，請見
[元件 API 與參數參考](./component-reference.md)；要看 ViewModel、repository 與非同步流程如何
組合，請見[業務情境實作](./business-recipes.md)。

## 基本 import

```kotlin
import cg.creamgod.consoleapp.designsystem.components.*
import cg.creamgod.consoleapp.designsystem.components.form.*
import cg.creamgod.consoleapp.designsystem.content.*
import cg.creamgod.consoleapp.designsystem.patterns.article.*
```

互動範例通常還需要：

```kotlin
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
```

## 共用語意

`Tone` 控制語意而不是固定色碼。可用值為 `Primary`、`Secondary`、`Success`、`Danger`、
`Warning`、`Info`、`Light`、`Dark`。

```kotlin
var deleteRequested by remember { mutableStateOf(false) }

Alert("Saved", tone = Tone.Success)
Alert("Check the value", tone = Tone.Warning)
Button("Delete", tone = Tone.Danger, onClick = { deleteRequested = true })
Badge("Info", tone = Tone.Info)
```

`Variant` 提供 `Filled`、`Outline`、`Text`；`Placement` 提供 `Start`、`End`。

## Semantic content

### H1–H6、Paragraph、Lead、Caption、SmallText

這些 helper 對應 `MaterialTheme.typography`，用途類似 HTML tag：

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    H1("H1 / display")
    H2("H2 / page section")
    H3("H3 / subsection")
    H4("H4")
    H5("H5")
    H6("H6")
    Lead("較醒目的文章前言。")
    Paragraph("一般段落內容。")
    Caption("圖片或表格說明。")
    SmallText("補充標籤與 metadata。")
}
```

每個 helper 都接受 `modifier` 與選用的 `color`。

## Document 與 Markdown

### 強型別 Document DSL

```kotlin
val page = document {
    h1("Account security")
    paragraph(
        text("Use a "),
        strong("unique password"),
        text(" and enable "),
        link("two-factor authentication", "https://example.com/2fa"),
        text("."),
    )
    h2("Setup")
    orderedList("Open settings", "Scan the QR code", "Save recovery codes")
    quote { paragraph("Recovery codes should be stored offline.") }
    code("val enabled = true", language = "kotlin")
    divider()
}

DocumentRenderer(page)
```

DSL 支援 `heading()`、`h1()`–`h6()`、`paragraph()`、`code()`、`quote()`、
`bulletList()`、`orderedList()`、`divider()`；inline helper 包含 `text()`、`strong()`、
`emphasis()`、`inlineCode()`、`link()`。

### MarkdownDocument、MarkdownResource

```kotlin
MarkdownDocument(
    """
    # Release notes

    - Added **semantic typography**
    - Added [clickable links](https://example.com)

    | Feature | Status |
    | --- | --- |
    | Markdown | Ready |
    """.trimIndent(),
)
```

將檔案放在 `shared/src/commonMain/composeResources/files` 後可直接讀取：

```kotlin
MarkdownResource(
    path = "docs/getting-started.md",
    loading = { Spinner() },
    failure = { error -> Alert(error.message ?: "讀取失敗", tone = Tone.Danger) },
)
```

非 UI 程式可從 coroutine 使用：

```kotlin
val source = readTextResource("docs/getting-started.md")
val parsed = MarkdownParser.parse(source)
```

Markdown 支援標題、段落、粗斜體、inline/fenced code、link、image node、quote、list、divider
與 pipe table。Link 可點擊；image node 目前顯示替代文字，遠端圖片需另接 image loader。

## Actions

### Button、ButtonGroup

```kotlin
var saving by remember { mutableStateOf(false) }
var displayName by remember { mutableStateOf("Ada") }
val formIsValid = displayName.isNotBlank()

Button(
    text = "Save",
    onClick = { saving = true },
    tone = Tone.Primary,
    variant = Variant.Filled,
    enabled = formIsValid,
    leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) },
)

enum class ViewMode { List, Grid }
var mode by remember { mutableStateOf(ViewMode.List) }

ButtonGroup(
    items = listOf(
        ButtonItem(ViewMode.List, "List"),
        ButtonItem(ViewMode.Grid, "Grid"),
    ),
    selected = mode,
    onSelect = { mode = it },
)
```

### CloseButton、Dropdown

```kotlin
var selectedExportFormat by remember { mutableStateOf<String?>(null) }

CloseButton(
    onClick = { visible = false },
    description = "關閉設定面板",
)

Dropdown(
    label = "Export",
    items = listOf(
        DropdownItem("csv", "CSV"),
        DropdownItem("json", "JSON"),
        DropdownItem("pdf", "PDF", enabled = false),
    ),
    onSelect = { format -> selectedExportFormat = format },
)
```

## Feedback

### Alert、Callout

```kotlin
var backupRequested by remember { mutableStateOf(false) }

Alert(
    message = "資料已同步",
    title = "完成",
    tone = Tone.Success,
    dismissible = true,
    onDismiss = { showAlert = false },
)

Callout(
    title = "Before continuing",
    message = "Create a backup first.",
    tone = Tone.Warning,
    icon = { Icon(Icons.Default.Warning, contentDescription = null) },
    action = {
        Button(
            "Backup",
            onClick = { backupRequested = true },
            variant = Variant.Text,
        )
    },
) {
    Text("This migration changes the stored data format.")
}
```

### Badge、Progress、Spinner、Placeholder

```kotlin
Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    Badge("New")
    Badge("42", tone = Tone.Danger, pill = true)
    Spinner(tone = Tone.Info)
}

Progress(value = 0.72f, tone = Tone.Success)
Placeholder(Modifier.fillMaxWidth().height(24.dp), animated = true)
```

`Progress.value` 使用 `0f..1f`，超出範圍會自動限制。

### Toast、ToastHost

```kotlin
Toast(
    message = "Saved",
    title = "Profile",
    visible = showToast,
    onDismiss = { showToast = false },
)

val toast = rememberToastState()
val scope = rememberCoroutineScope()

Scaffold(snackbarHost = { ToastHost(toast) }) { padding ->
    Button(
        text = "Save",
        modifier = Modifier.padding(padding),
        onClick = {
            scope.launch {
                val result = toast.show("Saved", actionLabel = "Undo")
                if (result == SnackbarResult.ActionPerformed) undo()
            }
        },
    )
}
```

使用 `toast.dismiss()` 可關閉目前 queue message。

## Disclosure 與 surfaces

### Collapse、Accordion

```kotlin
var expanded by remember { mutableStateOf(false) }

Button(if (expanded) "Hide" else "Show", onClick = { expanded = !expanded })
Collapse(expanded) { Paragraph("Only rendered while expanded.") }

Accordion(
    allowMultiple = true,
    items = listOf(
        AccordionItem("Profile", initiallyExpanded = true) { Text("Profile content") },
        AccordionItem("Billing") { Text("Billing content") },
        AccordionItem("Unavailable", enabled = false) { Text("Disabled content") },
    ),
)
```

`Accordion(flush = true)` 可移除外框與圓角。

### Card

```kotlin
@Composable
fun StorageCard(onManage: () -> Unit, onUpgrade: () -> Unit, onOpenDetails: () -> Unit) {
    Card(
        title = "Storage",
        subtitle = "7.2 GB of 10 GB used",
        header = { Placeholder(Modifier.fillMaxWidth().height(120.dp), animated = false) },
        footer = {
            Button("Manage", onClick = onManage, variant = Variant.Text)
            Button("Upgrade", onClick = onUpgrade)
        },
        onClick = onOpenDetails,
    ) {
        Progress(0.72f)
    }
}
```

### ListGroup、Carousel

```kotlin
var selected by remember { mutableStateOf("profile") }

ListGroup(
    items = listOf(
        ListItem("profile", "Profile", "Public information"),
        ListItem("security", "Security", "Password and 2FA"),
        ListItem("admin", "Administration", enabled = false),
    ),
    selected = selected,
    onSelect = { selected = it },
)

Carousel(
    items = listOf(
        CarouselItem("First") { Card { Text("First slide") } },
        CarouselItem("Second") { Card { Text("Second slide") } },
    ),
    initialIndex = 0,
    showControls = true,
    showIndicators = true,
)
```

## Navigation

### Breadcrumb、Navbar

```kotlin
@Composable
fun AppNavigation(onSignOut: () -> Unit) {
    var destination by remember { mutableStateOf("home") }

Breadcrumb(
    items = listOf(
        BreadcrumbItem("Home", onClick = { destination = "home" }),
        BreadcrumbItem("Settings", onClick = { destination = "settings" }),
        BreadcrumbItem("Security"),
    ),
)

val navigation = listOf(
    NavItem("home", "Home"),
    NavItem("reports", "Reports"),
    NavItem("admin", "Admin", enabled = false),
)

Navbar(
    brand = "ConsoleApp",
    items = navigation,
    selected = destination,
    onSelect = { destination = it },
    actions = { Button("Sign out", onClick = onSignOut, variant = Variant.Text) },
)
}
```

### Tabs、Nav、Pagination、ScrollSpy

```kotlin
var activeSection by remember { mutableStateOf("intro") }

Tabs(navigation, destination, { destination = it })
Nav(navigation, destination, { destination = it }, vertical = true)

var page by remember { mutableIntStateOf(1) }
Pagination(
    currentPage = page,
    pageCount = 25,
    siblingCount = 2,
    onPageChange = { page = it },
)

ScrollSpy(
    items = listOf(
        NavItem("intro", "Introduction"),
        NavItem("install", "Installation"),
        NavItem("usage", "Usage"),
    ),
    active = activeSection,
    onNavigate = { section -> activeSection = section },
)
```

頁碼採 1-based。`ScrollSpy` 不存取 DOM；screen 依 scroll state 傳入 `active`。

## Forms

表單元件使用 controlled value；業務資料保留在 screen／ViewModel，元件只回報改變。

### FormTextInput、FormPasswordInput、FormTextArea

```kotlin
var email by remember { mutableStateOf("") }
var password by remember { mutableStateOf("") }
var notes by remember { mutableStateOf("") }

FormTextInput(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    type = FormInputType.Email,
    placeholder = "name@example.com",
    helperText = "We never display this publicly.",
    required = true,
)

FormPasswordInput(
    value = password,
    onValueChange = { password = it },
    label = "Password",
    required = true,
)

FormTextArea(
    value = notes,
    onValueChange = { notes = it },
    label = "Notes",
    minLines = 4,
    maxLines = 10,
)
```

`FormInputType` 支援 `Text`、`Email`、`Password`、`Number`、`Decimal`、`Phone`、`Url`、
`Search`、`Date`、`Time`。需要視覺化選擇時使用跨平台 `DatePicker`、`DateRangePicker`、
`TimePicker`；若產品明確要求作業系統原生 picker，再由平台 adapter 實作。

新一批 Material 3 actions、navigation、sheets、chips、search、Snackbar 與 picker 的完整實務
範例集中在 [Material 3 元件與實務範例](./material3-components.md)。

### FormSelect、FormMultiSelect

```kotlin
val roles = listOf(
    FormOption("reader", "Reader"),
    FormOption("editor", "Editor"),
    FormOption("owner", "Owner", enabled = false),
)
var role by remember { mutableStateOf<String?>(null) }
var selectedRoles by remember { mutableStateOf(emptySet<String>()) }

FormSelect(
    value = role,
    options = roles,
    onValueChange = { role = it },
    label = "Primary role",
    required = true,
)

FormMultiSelect(
    values = selectedRoles,
    options = roles,
    onValuesChange = { selectedRoles = it },
    label = "Additional roles",
)
```

### FormCheckbox、FormRadioGroup、FormSwitch、FormRange

```kotlin
var accepted by remember { mutableStateOf(false) }
var plan by remember { mutableStateOf<String?>(null) }
var notifications by remember { mutableStateOf(true) }
var volume by remember { mutableFloatStateOf(50f) }

FormCheckbox(accepted, { accepted = it }, label = "I accept the terms")

FormRadioGroup(
    value = plan,
    options = listOf(FormOption("free", "Free"), FormOption("pro", "Pro")),
    onValueChange = { plan = it },
    label = "Plan",
)

FormSwitch(notifications, { notifications = it }, label = "Notifications")

FormRange(
    value = volume,
    onValueChange = { volume = it },
    label = "Volume",
    valueRange = 0f..100f,
    steps = 9,
    valueText = { "${it.toInt()}%" },
)
```

### FormSection、FormInputGroup、FormFilePicker、FormActions

```kotlin
@Composable
fun BillingForm(
    selectedFiles: List<String>,
    onPickFiles: () -> Unit,
    onClearFiles: () -> Unit,
    onSubmit: (String) -> Unit,
) {
var amount by remember { mutableStateOf("") }
val formIsValid = amount.toDoubleOrNull()?.let { it > 0.0 } == true

FormSection(title = "Billing", description = "Invoice and payment settings.") {
    FormInputGroup(prefix = "$", suffix = "USD") {
        FormTextInput(
            value = amount,
            onValueChange = { amount = it },
            label = "Amount",
            type = FormInputType.Decimal,
            modifier = Modifier.weight(1f),
        )
    }

    FormFilePicker(
        fileNames = selectedFiles,
        onPick = onPickFiles,
        onClear = onClearFiles,
        label = "Attachments",
        helperText = "PNG or PDF, up to 10 MB.",
        allowMultiple = true,
    )

    FormActions(
        onSubmit = { onSubmit(amount) },
        submitText = "Save",
        submitEnabled = formIsValid,
        onReset = { amount = "" },
    )
}
}
```

`FormFilePicker` 只提供顯示與事件；實際 picker 由 JVM、JS 或 Wasm adapter 實作。

### FormValidators

```kotlin
val validator = FormValidators.all(
    FormValidators.required(),
    FormValidators.minLength(3),
    FormValidators.maxLength(30),
    FormValidators.pattern(Regex("[a-z0-9_-]+"), "Invalid username"),
)

val result: FormValidation = validator.validate(username)
FormTextInput(
    value = username,
    onValueChange = { username = it },
    label = "Username",
    errorText = result.message,
)
```

另有 `email()`。它允許空值，必填 Email 要與 `required()` 組合。Validator 不依賴 Compose，
可在 ViewModel 與 common test 使用。

## Overlay

Overlay 使用 controlled `visible`。所有確認、取消、外部點擊與返回路徑都應更新呼叫端狀態。

### Popup、Modal

```kotlin
@Composable
fun ProfileEditor(onSave: (String) -> Unit) {
var name by remember { mutableStateOf("") }
var editorVisible by remember { mutableStateOf(false) }
var popupVisible by remember { mutableStateOf(false) }

Popup(
    visible = popupVisible,
    onDismiss = { popupVisible = false },
    alignment = Alignment.BottomEnd,
    offset = IntOffset(0, 8),
) {
    Card { Text("Popup content") }
}

Modal(
    visible = editorVisible,
    onDismiss = { editorVisible = false },
    title = "Edit profile",
    footer = {
        Button("Cancel", onClick = { editorVisible = false }, variant = Variant.Text)
        Button("Save", onClick = { onSave(name); editorVisible = false })
    },
) {
    FormTextInput(name, { name = it }, label = "Name")
}
}
```

### Confirm、Ask、Choice

```kotlin
@Composable
fun ItemDialogs(onDelete: () -> Unit, onRename: (String) -> Unit, onDeploy: (String) -> Unit) {
var deleteVisible by remember { mutableStateOf(false) }
var renameVisible by remember { mutableStateOf(false) }
var choiceVisible by remember { mutableStateOf(false) }
var newName by remember { mutableStateOf("") }
var selectedEnvironment by remember { mutableStateOf<String?>(null) }
val nameError = if (newName.isBlank()) "Name is required" else null

Confirm(
    visible = deleteVisible,
    onConfirm = {
        onDelete()
        deleteVisible = false
    },
    onDismiss = { deleteVisible = false },
    title = "Delete item?",
    message = "This cannot be undone.",
    destructive = true,
)

Ask(
    visible = renameVisible,
    value = newName,
    onValueChange = { newName = it },
    onConfirm = { value -> onRename(value); renameVisible = false },
    onDismiss = { renameVisible = false },
    title = "Rename project",
    label = "New name",
    errorText = nameError,
)

Choice(
    visible = choiceVisible,
    value = selectedEnvironment,
    options = listOf(
        FormOption("dev", "Development"),
        FormOption("prod", "Production"),
    ),
    onValueChange = { selectedEnvironment = it },
    onConfirm = { environment -> onDeploy(environment); choiceVisible = false },
    onDismiss = { choiceVisible = false },
    title = "Deploy to",
)
}
```

### Offcanvas、Popover、Tooltip

```kotlin
@Composable
fun NavigationHelp(
    navigation: List<NavItem<String>>,
    destination: String,
    onDestinationChange: (String) -> Unit,
    onCopyShortcut: () -> Unit,
) {
var menuVisible by remember { mutableStateOf(false) }
var popoverVisible by remember { mutableStateOf(false) }
var tooltipVisible by remember { mutableStateOf(false) }

Offcanvas(
    visible = menuVisible,
    onDismiss = { menuVisible = false },
    title = "Menu",
    placement = Placement.Start,
) {
    Nav(navigation, destination, onDestinationChange, vertical = true)
}

Popover(
    visible = popoverVisible,
    onDismiss = { popoverVisible = false },
    title = "Keyboard shortcut",
    message = "Press Ctrl+K to open search.",
    anchor = {
        Button("Help", onClick = { popoverVisible = true }, variant = Variant.Text)
    },
)

Tooltip(visible = tooltipVisible, text = "Copy to clipboard") {
    IconButton(onClick = onCopyShortcut) {
        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
    }
}
}
```

Tooltip 的 hover／focus state 由呼叫端依平台事件提供。

## Guide articles

### GuideArticle、ArticleStep、ArticleCallout、CodeBlock

```kotlin
GuideArticle(
    meta = ArticleMeta(
        title = "Create your first form",
        summary = "Build and validate a controlled email field.",
        type = ArticleType.Tutorial,
        readingMinutes = 5,
        tags = listOf("Forms", "Validation"),
    ),
    sections = listOf(
        ArticleSection("field", "Create the field") {
            ArticleStep(1, "Own the state") {
                Paragraph("Store the value in the screen or ViewModel.")
            }
            CodeBlock("var email by remember { mutableStateOf(\"\") }", language = "kotlin")
            ArticleCallout(
                title = "Validation",
                message = "Validate business state outside the visual component.",
                tone = Tone.Info,
            )
        },
    ),
    previous = ArticleLink("Installation") { openInstallation() },
    next = ArticleLink("Selection controls") { openSelections() },
)
```

`ArticleType`：`GettingStarted`、`Tutorial`、`HowTo`、`Concept`、`Reference`、
`Troubleshooting`、`Migration`。

## Component catalog

```kotlin
import cg.creamgod.consoleapp.designsystem.catalog.ComponentGuide

ComponentGuide(Modifier.fillMaxSize())
```

`ComponentGuide` 提供理念、文章、Markdown resource、互動範例與 rollout 索引。需要機器可讀
coverage 時可讀取 `bootstrapComponentCatalog`。

## Supporting models

| Model | 用途 |
| --- | --- |
| `Tone` / `ComponentTone` | 元件語意色 |
| `Size` / `ComponentSize` | 共用尺寸語意 |
| `Variant` | Filled、Outline、Text |
| `Placement` | Start、End |
| `ButtonItem`, `DropdownItem` | Action collections |
| `AccordionItem`, `CarouselItem`, `ListItem` | Disclosure／surface collections |
| `BreadcrumbItem`, `NavItem` | Navigation collections |
| `FormOption`, `FormValidation`, `StringValidator` | Form selections and validation |
| `Document`, `DocumentBlock`, `DocumentBuilder` | 強型別 block document |
| `InlineContent` | Text、strong、emphasis、code、link、image、line break |
| `MarkdownResourceState` | Loading、Ready、Failed resource state |
| `ArticleMeta`, `ArticleSection`, `ArticleLink`, `ArticleType` | Guide article structure |
| `ComponentStatus`, `BootstrapComponentSpec` | Catalog rollout metadata |

## 相容層與使用原則

`DsCallout`、`DsModal`、`DsPopup`、`DsToastHost`、`DsConfirmDialog` 等舊 API 保留給既有
程式；新畫面優先使用短名稱。

1. 業務狀態保留在 screen 或 ViewModel。
2. 使用 `Tone` 與 `MaterialTheme`，不要散落色碼。
3. Icon action 必須有明確 `contentDescription`。
4. 表單使用可見 label，錯誤同時使用文字。
5. Modal、Popup 與 Tooltip 必須有明確開關流程。
6. 檔案、日期、時間與遠端圖片等平台功能由 adapter 負責。
