# ConsoleApp Design System Wiki

ConsoleApp Design System 是一套用 Compose Multiplatform 建立的跨平台 UI 與內容框架。
同一套 `commonMain` API 可用於 Desktop JVM、JavaScript 與 Wasm。

```kotlin
@Composable
fun Dashboard(onContinue: () -> Unit) {
    H1("Dashboard")
    Lead("A shared Material 3 interface for desktop and web.")
    Alert("Connected", tone = Tone.Success)
    Button("Continue", onClick = onContinue)
}
```

## 從這裡開始

- [Getting Started](Getting-Started) — import、第一個元件、執行與測試。
- [Learning Roadmap](Learning-Roadmap) — 從 Kotlin、Compose、shared 到完整作品的分階段學習路徑。
- [How to Plan Chapters](Planning-a-Guide-Book) — 如何規劃教學書、範例、拆解與驗收標準。
- [Compose Concepts](Compose-Concepts) — 宣告式 UI、重新組合、state hoisting、effect 與 Modifier。
- [Kotlin Shared / commonMain](Kotlin-Shared) — 共用程式碼邊界、contract、測試與平台注入。
- [WebMain Guide](WebMain-Guide) — ComposeViewport、瀏覽器 adapter、JS/Wasm 執行與除錯。
- [DesktopApp Guide](DesktopApp-Guide) — Window、JVM adapter、原生檔案對話框與封裝。
- [Components](Components) — 所有公開元件的分類、state model 與基本用法。
- [Component Learning Pages](Component-Learning-Pages) — 每個元件獨立頁面的閱讀方式與完整案例索引。
- [Business Recipes](Business-Recipes) — ViewModel、驗證、非同步結果與復原流程。
- [Business Case Study](Business-Case-Study) — 從需求拆解到 repository、UI state、測試的帳號設定案例。
- [Account Settings Tutorial](Tutorial-Account-Settings) — 可編譯的完整 helper、repository 與測試。
- [Content and Markdown](Content-and-Markdown) — H1–H6、Document DSL、parser 與 resource。
- [Architecture](Architecture) — 模組邊界、controlled state、平台 adapter 與擴充原則。

## 設計原則

1. Material 3 提供 theme、互動與無障礙基礎。
2. Bootstrap／HTML-like 名稱提供快速、熟悉的開發體驗。
3. 業務狀態由 screen 或 ViewModel 擁有。
4. JVM、JS 與 Wasm 共用相同的 UI API。
5. 檔案 picker、日期／時間與其他平台能力放在 adapter。

## 快速範例

```kotlin
@Composable
fun SettingsScreen() {
    var email by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        H1("Settings")
        FormTextInput(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            type = FormInputType.Email,
        )
        Button("Save", onClick = { /* persist state */ })
    }
}
```

App 內也可直接掛載 `ComponentGuide()` 檢查互動與跨平台呈現。

## 文件範例標準

可直接使用的範例會定義 state 與 callback 來源；完整業務教學還會交代 validation、loading、
成功、失敗、repository 與測試。`::save`、`viewModel` 或 `items` 若未定義，不會被當成可複製範例。
