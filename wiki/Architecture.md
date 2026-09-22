# Architecture

```text
designsystem/
├── catalog/              # ComponentGuide and coverage metadata
├── components/           # Reusable UI components
│   ├── actions/
│   ├── disclosure/
│   ├── feedback/
│   ├── form/
│   ├── navigation/
│   ├── overlay/
│   └── surface/
├── content/              # Typography, typed documents, Markdown
├── foundation/           # Shared semantic models
├── patterns/article/     # Documentation patterns
└── tokens/               # Spacing, radius, breakpoints
```

## Controlled state

Screen 或 ViewModel 擁有業務狀態：

```kotlin
var visible by remember { mutableStateOf(false) }
Confirm(
    visible = visible,
    onConfirm = { visible = false },
    onDismiss = { visible = false },
    title = "Confirm",
    message = "Continue?",
)
```

Accordion、Carousel 等純展示元件可保有局部 UI state，但不可承載業務資料。

## Theme and tokens

元件使用 `MaterialTheme` 與 `Tone`，layout 使用 `BootstrapTokens`。不要在 feature screen
散落品牌色、圓角或 spacing 常數。

## Platform adapters

`commonMain` 負責外觀與 state contract；JVM、JS、Wasm 實作實際 file picker、date/time
picker、外部連結與其他平台能力。

## Compatibility

`Ds*` API 是相容層。新程式使用 `Callout`、`Modal`、`Popup`、`ToastHost`、`Confirm` 等
短名稱。

## Adding a component

1. 先確認能否由既有元件組合。
2. 公開 API 保持短、語意清楚並接受 `Modifier`。
3. 業務狀態由呼叫端控制。
4. 提供 disabled、error、label 與 accessibility semantics。
5. 更新 catalog、repository docs 與 Wiki source。
6. 執行 JVM、JS 與 Wasm 驗證。
