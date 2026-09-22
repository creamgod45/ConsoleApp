# Getting Started

## Imports

```kotlin
import cg.creamgod.consoleapp.designsystem.components.*
import cg.creamgod.consoleapp.designsystem.components.form.*
import cg.creamgod.consoleapp.designsystem.content.*
import cg.creamgod.consoleapp.designsystem.patterns.article.*
```

如果與 Material 3 元件同名，使用 alias：

```kotlin
import cg.creamgod.consoleapp.designsystem.components.Button as AppButton
```

## Callback syntax

`::save` 是 Kotlin 函式參考，不是元件庫內建 action。專案中必須存在相同簽章的函式：

```kotlin
fun logSave() {
    println("Save requested")
}

Button("Save", onClick = ::logSave) // 等同 { logSave() }
```

需要參數時使用 lambda：`onClick = { viewModel.delete(itemId) }`。

Callback 也必須符合事件語意：`Alert.onDismiss` 只應清除訊息，不能執行刪除；刪除應放在
`Button.onClick` 或 `Confirm.onConfirm`。

## First screen

```kotlin
@Composable
fun WelcomeScreen(onOpenDashboard: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        H1("Welcome")
        Paragraph("ConsoleApp uses one design system on desktop and web.")
        Alert("Ready", tone = Tone.Success)
        Button("Get started", onClick = onOpenDashboard)
    }
}
```

## Controlled state

```kotlin
var selected by remember { mutableStateOf("overview") }

Tabs(
    items = listOf(
        NavItem("overview", "Overview"),
        NavItem("activity", "Activity"),
    ),
    selected = selected,
    onSelect = { selected = it },
)
```

## Run

```shell
./gradlew :desktopApp:hotRun --auto
./gradlew :desktopApp:run
./gradlew :webApp:wasmJsBrowserDevelopmentRun
./gradlew :webApp:jsBrowserDevelopmentRun
```

Windows PowerShell 使用 `./gradlew.bat`。

## Test

```shell
./gradlew :shared:jvmTest
./gradlew :shared:jsTest
./gradlew :shared:wasmJsTest
```

## In-app catalog

```kotlin
ComponentGuide(Modifier.fillMaxSize())
```

接著閱讀 [Components](Components) 或 [Content and Markdown](Content-and-Markdown)。
