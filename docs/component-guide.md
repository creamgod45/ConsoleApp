# Component guide

這份指南是 ConsoleApp 共用元件庫的主要入口。元件實作位於 Compose Multiplatform
`commonMain`，可供 Desktop JVM、JavaScript 與 Wasm 共用。

## 60 秒開始使用

一般元件只有一個 import：

```kotlin
import cg.creamgod.consoleapp.designsystem.components.*
```

表單元件另外匯入：

```kotlin
import cg.creamgod.consoleapp.designsystem.components.form.*
```

接著直接使用短名稱：

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Alert("設定已更新", tone = Tone.Success)
    Badge("Beta", pill = true)
    Button("儲存", onClick = ::save)
    Progress(0.75f)
}
```

如果同時使用 Material 3 的同名元件，請明確指定 alias：

```kotlin
import cg.creamgod.consoleapp.designsystem.components.Button as AppButton
import androidx.compose.material3.Button as MaterialButton
```

## 設計理念

### Compose-first

Bootstrap 提供團隊熟悉的分類、語意色與元件名稱，但實作不是 HTML、CSS class 或
Bootstrap JavaScript 的包裝。每個元件都是 Compose Multiplatform composable，使用
Material 3 與 Compose Foundation 提供焦點、鍵盤、動畫及無障礙基礎。

### 狀態由呼叫端擁有

Modal 是否開啟、Tab 選擇、表單內容與頁碼都由 screen 或 ViewModel 管理。元件接收
目前狀態並透過 callback 回報事件，避免元件內部狀態與業務狀態不同步。

```kotlin
var showDelete by remember { mutableStateOf(false) }

Confirm(
    visible = showDelete,
    title = "刪除項目？",
    message = "這個動作無法復原。",
    destructive = true,
    onConfirm = {
        deleteItem()
        showDelete = false
    },
    onDismiss = { showDelete = false },
)
```

少數純展示元件可以保有局部 UI 狀態，例如 `Accordion` 展開項目與 `Carousel` 目前頁。
這些狀態不應承載業務資料。

### 使用語意，不直接指定色碼

所有需要表達意圖的元件共用 `Tone`：

| Tone | 用途 |
| --- | --- |
| `Primary` | 主要操作、目前選擇 |
| `Secondary` | 次要資訊與操作 |
| `Success` | 成功、完成、有效狀態 |
| `Danger` | 錯誤、刪除與不可逆操作 |
| `Warning` | 需要注意但尚未失敗 |
| `Info` | 一般提示與說明 |
| `Light` / `Dark` | 中性明暗表面 |

顏色由 Material theme 決定，切換明暗模式時不需要逐一修改元件。

### 短 API、清楚 model

Composable 使用 Bootstrap 熟悉的名稱，例如 `Card()`、`Modal()`、`Tabs()`。列表型
元件使用短 model：`NavItem`、`ListItem`、`DropdownItem`、`AccordionItem`。

### 平台功能留在 adapter

檔案、原生日期與時間選擇器不能假設每個平台具有相同 API。共用層負責外觀與狀態，
`jvmMain`、`jsMain`、`wasmJsMain` adapter 負責打開實際的系統或瀏覽器選擇器。

## 元件索引

### Actions

| API | 用途 |
| --- | --- |
| `Button` | Filled、Outline、Text 按鈕 |
| `ButtonGroup` | 單選式按鈕群組 |
| `CloseButton` | 具有關閉語意的圖示按鈕 |
| `Dropdown` | 按鈕觸發的選單 |

```kotlin
Button(
    text = "刪除",
    onClick = ::requestDelete,
    tone = Tone.Danger,
    variant = Variant.Outline,
)
```

### Feedback

| API | 用途 |
| --- | --- |
| `Alert` | 簡短狀態訊息，可關閉 |
| `Callout` | 可放 icon、action、自訂內容的說明區塊 |
| `Badge` | 狀態或數量標籤 |
| `Progress` | 0 到 1 的線性進度 |
| `Spinner` | 未知完成時間的載入狀態 |
| `Placeholder` | 載入中的骨架區塊 |
| `Toast` | 單一可見訊息卡片 |
| `ToastHost` | 可排隊的全域 Toast/Snackbar host |

全域 Toast 應只放一個 host：

```kotlin
val toast = rememberToastState()
val scope = rememberCoroutineScope()

Scaffold(snackbarHost = { ToastHost(toast) }) { padding ->
    Button("儲存", onClick = {
        scope.launch { toast.show("儲存完成") }
    })
}
```

### Disclosure and surfaces

| API | 用途 |
| --- | --- |
| `Accordion` | 一個或多個可展開區塊 |
| `Collapse` | 受控的顯示／隱藏動畫 |
| `Card` | Header、title、body、footer 容器 |
| `ListGroup` | 可選擇且可 disabled 的列表 |
| `Carousel` | 前後切換與指示器 |

```kotlin
Accordion(
    items = listOf(
        AccordionItem("基本資料", initiallyExpanded = true) {
            Text("內容")
        },
        AccordionItem("進階設定") {
            Text("更多內容")
        },
    ),
)
```

### Navigation

| API | 用途 |
| --- | --- |
| `Breadcrumb` | 顯示目前資訊階層 |
| `Navbar` | 品牌、主要導覽與 action |
| `Nav` | 水平或垂直導覽 |
| `Tabs` | 分頁內容切換 |
| `Pagination` | 頁碼與前後頁 |
| `ScrollSpy` | 由畫面回報 active section 的章節導覽 |

`ScrollSpy` 不直接讀取 DOM 或捲動容器；screen 根據自己的 scroll state 計算 active
section，再傳入 `active`。這讓相同 API 能在 Desktop 與 Web 使用。

### Overlay

| API | 用途 |
| --- | --- |
| `Popup` | 自由配置的浮動內容 |
| `Modal` | 自訂 body 與 footer 的阻斷式視窗 |
| `Confirm` | 確認／取消問題 |
| `Ask` | 帶文字輸入的問題 |
| `Choice` | 單選問題 |
| `Offcanvas` | 從左右側顯示的面板 |
| `Popover` | 帶標題與內容的 anchored 說明 |
| `Tooltip` | 短文字提示；顯示狀態由呼叫端控制 |

Overlay 必須在所有結束路徑更新 `visible`：確認、取消、外部點擊及返回鍵都不應留下
過期狀態。

## 表單

表單 API 保留 `Form` 前綴，避免在大型畫面中看不出欄位用途：

```kotlin
var email by remember { mutableStateOf("") }
val emailValidation = FormValidators.all(
    FormValidators.required(),
    FormValidators.email(),
).validate(email)

FormTextInput(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    type = FormInputType.Email,
    required = true,
    errorText = emailValidation.message,
    modifier = Modifier.fillMaxWidth(),
)
```

可用元件：

- `FormTextInput`, `FormPasswordInput`, `FormTextArea`
- `FormSelect`, `FormMultiSelect`
- `FormCheckbox`, `FormRadioGroup`, `FormSwitch`, `FormRange`
- `FormInputGroup`, `FormFilePicker`, `FormSection`, `FormActions`
- `FormValidators.required/email/minLength/maxLength/pattern/all`

驗證器不依賴 Compose，可直接放在 ViewModel 或 common test。

## App 內建指南

元件庫提供可直接掛入畫面的互動介紹：

```kotlin
import cg.creamgod.consoleapp.designsystem.catalog.ComponentGuide

@Composable
fun DesignSystemScreen() {
    ComponentGuide(Modifier.fillMaxSize())
}
```

`ComponentGuide` 包含：

- 理念：Compose-first、controlled state、語意色與平台 adapter。
- 文章：`GuideArticle` 的 Getting Started 實際範例。
- 範例：Button、Alert、Form、Progress、Accordion、Confirm 的互動展示。
- 索引：讀取 machine-readable catalog 顯示 Ready、Planned、Adapter 狀態。

需要撰寫教學內容時，請參考[引導文章作者指南](./article-guide.md)。文章元件支援
Getting Started、Tutorial、How-to、Concept、Reference、Troubleshooting 與 Migration。

## 無障礙規則

- 可操作 icon 必須提供有意義的 `contentDescription`。
- 表單欄位必須有可見 label，不以 placeholder 取代名稱。
- 顏色不能是唯一的狀態訊號；錯誤同時顯示文字。
- 點擊目標需保持足夠尺寸，列表整列可以操作時應讓整列可點擊。
- Popup、Modal 與 Tooltip 要有明確的開啟與關閉流程。
- 動畫不可承載必要資訊；使用者即使忽略動畫仍可理解狀態。

## 新增元件的規則

1. 先確認需求是否能由既有元件組合完成。
2. 公開 API 放在 `cg.creamgod.consoleapp.designsystem.components`，名稱保持簡短。
3. 實體檔案依 actions、feedback、navigation、overlay、surface 分類。
4. 業務狀態由呼叫端控制，component 僅保留局部展示狀態。
5. 使用 `Tone`、Material theme 與 tokens，不硬編碼產品顏色或間距。
6. 補上 `Modifier`、disabled、label/content description 與 error state。
7. 更新 `BootstrapComponentCatalog` 與這份 Guide。
8. 至少執行 JVM test、JS compile、Wasm compile。

## 驗證指令

```shell
./gradlew :shared:jvmTest \
  :shared:compileKotlinJs \
  :shared:compileKotlinWasmJs
```

## 相容性

早期的 `DsCallout`、`DsModal`、`DsPopup`、`DsToastHost` 等 API 暫時保留，讓既有呼叫
不會失效。新程式應使用 `Callout`、`Modal`、`Popup`、`ToastHost` 等短名稱。
