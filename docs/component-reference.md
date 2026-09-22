# 元件 API 與參數參考

這份文件說明每個公開元件的參數、狀態責任與適用情境。需要完整業務流程時，接著閱讀
[業務情境實作](./business-recipes.md)。

## 閱讀慣例

- **Controlled**：目前值由 screen／ViewModel 傳入，callback 只回報使用者意圖。
- **Local UI state**：元件內部只保存展開、頁籤動畫等不影響業務的狀態。
- `Modifier` 不承載業務邏輯，只處理 layout、semantics 與輸入行為。
- `Tone` 表示意圖；顏色仍由 `MaterialTheme` 決定。

### 範例中的 callback 不是特殊語法

`::save`、`::delete` 是 Kotlin 的函式參考，代表專案中必須真的存在一個簽章相符的函式：

```kotlin
fun logSave() {
    println("Save requested")
}

Button(
    text = "Save",
    onClick = ::logSave, // 等同 onClick = { logSave() }
)
```

若函式需要參數或是 suspend function，應使用 lambda：

```kotlin
@Composable
fun DeleteButton(itemId: String, onDelete: (String) -> Unit) {
    Button(text = "Delete", onClick = { onDelete(itemId) })
}
```

Callback 名稱還必須符合語意。`Alert.onDismiss` 表示「使用者關閉訊息」，不應綁定刪除：

```kotlin
var deleteError by remember { mutableStateOf<String?>("Item could not be deleted.") }
deleteError?.let { message ->
    Alert(
        message = message,
        modifier = Modifier.padding(16.dp),
        tone = Tone.Danger,
        dismissible = true,
        onDismiss = { deleteError = null },
    )
}
```

真正刪除應放在 `Button.onClick` 或 `Confirm.onConfirm`：

```kotlin
@Composable
fun DeleteConfirmation(itemId: String, onDelete: (String) -> Unit) {
    var visible by remember { mutableStateOf(true) }
    Confirm(
        visible = visible,
        title = "Delete item?",
        message = "This cannot be undone.",
        destructive = true,
        onConfirm = { onDelete(itemId); visible = false },
        onDismiss = { visible = false },
    )
}
```

文件中的 `save`、`delete`、`viewModel` 等名稱是業務端需要提供的示意依賴，不是設計系統內建函式。

## Actions

### Button

用於提交、建立、刪除、前往下一步等明確 action。

| 參數 | 型別／預設值 | 說明 |
| --- | --- | --- |
| `text` | `String` | 使用者看見的 action 名稱；避免「確定」等缺乏上下文文字。 |
| `onClick` | `() -> Unit` | 回報 intent；非同步操作通常轉交 ViewModel。 |
| `modifier` | `Modifier` | 尺寸、位置與 semantics。 |
| `tone` | `Tone.Primary` | `Danger` 適合刪除；`Success` 適合明確完成動作。 |
| `variant` | `Variant.Filled` | Filled 主動作、Outline 次動作、Text 低強度動作。 |
| `enabled` | `true` | `false` 阻止互動；提交中應避免重複送出。 |
| `leadingIcon` | `null` | 圖示 slot；裝飾圖示的 description 應為 `null`。 |
| `trailingIcon` | `null` | 常用於下一步或展開提示。 |

```kotlin
Button(
    text = if (saving) "Saving…" else "Save profile",
    onClick = onSave,
    enabled = formIsValid && !saving,
    leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) },
)
```

不要在 `onClick` 內直接維護多份畫面狀態；發送一個 intent，讓同一個 state source 回寫結果。

### ButtonGroup

適合檢視模式、排序方式等少量互斥選項，不適合大量表單選項。

| 參數 | 說明 |
| --- | --- |
| `items: List<ButtonItem<T>>` | `value` 是業務值，`label` 是顯示文字，`enabled` 控制單項。 |
| `selected: T?` | 目前選項；由呼叫端持有。 |
| `onSelect: (T) -> Unit` | 回報選擇。 |
| `tone` | 整組使用的語意色。 |

### CloseButton

`onClick` 關閉目前 surface；`enabled` 控制可用性；`description` 必須描述關閉的對象，例如
「關閉篩選面板」，而不只是「按鈕」。

### Dropdown

內部保存 menu 展開狀態，選到的業務值仍透過 `onSelect` 回報。

| 參數 | 說明 |
| --- | --- |
| `label` | Trigger button 文字。若要顯示目前值，呼叫端應更新此文字。 |
| `items` | `DropdownItem.value/label/enabled`。 |
| `onSelect` | 選取後呼叫，menu 自動關閉。 |
| `enabled` | 停用整個 trigger。 |

## Feedback

### Alert

用於畫面內、與目前內容直接相關的狀態。`message` 是必要內容，`title` 預設「提示」，
`tone` 表達結果，`dismissible` 決定是否顯示關閉 action，`onDismiss` 由呼叫端清除狀態。

```kotlin
var savedMessage by remember { mutableStateOf<String?>("Profile saved") }
savedMessage?.let { message ->
    Alert(
        title = "Profile saved",
        message = message,
        tone = Tone.Success,
        dismissible = true,
        onDismiss = { savedMessage = null },
    )
}
```

### Callout

比 Alert 更適合長期存在的教學、前置條件或風險提示。

| 參數 | 說明 |
| --- | --- |
| `title` | 區塊主旨。 |
| `message` | 選用簡短說明。 |
| `tone` | Info、Warning、Danger 等語意。 |
| `icon` | 左側 icon slot。 |
| `action` | RowScope action slot，例如「建立備份」。 |
| `content` | 自訂 body，可放清單或詳細說明。 |

### Badge

`text` 顯示狀態或數量；`tone` 表達語意；`pill = true` 適合短數字與單字。Badge 不應是唯一
狀態訊號，旁邊內容仍需能解釋其意義。

### Progress

`value` 是 `0f..1f`，元件會自動 coerce；`tone` 選擇語意色。已知總量使用 Progress，未知
完成時間使用 Spinner。

### Spinner

只有 `modifier` 與 `tone`。同時搭配文字說明「正在儲存」比單獨顯示轉圈更清楚。

### Placeholder

`animated` 預設 `true`。它只代表版面尚未載入，不可用來掩蓋錯誤或空資料。

### Toast / ToastHost

`Toast` 是單一受控訊息；`visible`、`onDismiss` 由呼叫端持有。跨畫面的短暫事件應使用
`rememberToastState()` 與單一 `ToastHost`。

`ToastState.show()` 參數：

| 參數 | 預設值 | 說明 |
| --- | --- | --- |
| `message` | 必填 | 短暫結果文字。 |
| `actionLabel` | `null` | Undo、Retry 等 action。 |
| `withDismissAction` | `actionLabel == null` | 是否顯示 dismiss。 |
| `duration` | `SnackbarDuration.Short` | 顯示時間。 |

回傳 `SnackbarResult.ActionPerformed` 或 `Dismissed`，可用來執行 undo／retry。

## Disclosure 與 surfaces

### Collapse

`expanded` 完全受控；`content` 只在可見時渲染。適合單一額外區塊。需要多組標題與 body
時使用 Accordion。

### Accordion

| 參數 | 預設值 | 說明 |
| --- | --- | --- |
| `items` | 必填 | `AccordionItem(title, initiallyExpanded, enabled, content)`。 |
| `allowMultiple` | `false` | 是否可同時展開多項。 |
| `flush` | `false` | 移除外框與圓角，適合嵌入既有 surface。 |

Accordion 的展開集合是 local UI state；若展開狀態會影響 URL、權限或資料載入，應改用受控
Collapse 組合。

### Card

| 參數 | 說明 |
| --- | --- |
| `title`, `subtitle` | 標準文字 header。 |
| `header` | 自訂頂部內容，例如圖片或圖表。 |
| `footer` | `RowScope` action 區。 |
| `onClick` | 非 null 時整張 Card 可點擊。內含多個按鈕時不要再讓整張 Card 可點。 |
| `content` | `ColumnScope` body。 |

### ListGroup

`items` 使用 `ListItem(value, headline, supportingText, enabled)`；`selected` 與 `onSelect` 形成
controlled selection。適合設定分類、master-detail 選單，不適合大量虛擬化資料。

### Carousel

`items` 使用 `CarouselItem(label, content)`；`initialIndex` 只在初始化使用；`showControls` 與
`showIndicators` 控制導覽 UI。Carousel 目前保存 local index，不適合作為必須由 URL 還原的業務頁碼。

## Navigation

### Breadcrumb

`BreadcrumbItem.onClick == null` 表示目前位置；`separator` 預設 `/`。Breadcrumb 描述資訊階層，
不可取代主要 navigation。

### Navbar

| 參數 | 說明 |
| --- | --- |
| `brand` | 產品或區域名稱。 |
| `items` | `NavItem(value, label, enabled, icon)`。 |
| `selected` | 目前 destination，可為 null。 |
| `onSelect` | 導覽 intent；實際 route change 由呼叫端處理。 |
| `actions` | 右側 action slot，例如 account、logout。 |

### Tabs

`selected` 必須存在於 `items`；`onSelect` 只回報 intent。Tabs 適合同層內容切換，不適合跨模組
主要導覽。

### Nav

與 Tabs 使用相同 `NavItem`，`vertical` 決定 Row／Column。適合 sidebar 或局部 section navigation。

### Pagination

`currentPage` 與 callback 使用 1-based 頁碼；`pageCount <= 0` 不渲染；`siblingCount` 控制目前頁
左右顯示多少頁。資料查詢、loading、錯誤與 URL 同步由呼叫端處理。

### ScrollSpy

`active` 是目前章節，`onNavigate` 回報點擊。元件不讀 DOM 或 scroll container；呼叫端根據
平台 scroll state 計算 active section。

## Forms

所有表單欄位都是 controlled component。`value` 來自唯一 state source，`onValueChange` 發送
更新 intent。`errorText != null` 才表示錯誤；`helperText` 只是說明。

### FormTextInput

| 參數 | 預設值 | 說明 |
| --- | --- | --- |
| `value` / `onValueChange` | 必填 | Controlled 文字值。 |
| `label` | 必填 | 永遠可見的欄位名稱。 |
| `type` | `Text` | 決定 keyboard type；含 Email、Password、Number、Decimal、Phone、Url、Search、Date、Time。 |
| `placeholder` | `null` | 輸入格式範例，不可取代 label。 |
| `helperText` | `null` | 限制或格式說明。 |
| `errorText` | `null` | 非 null 時顯示 error state，優先於 helper。 |
| `required` | `false` | 在 label 加上 `*`；實際驗證仍由 validator 負責。 |
| `enabled` | `true` | false 時不可操作。 |
| `readOnly` | `false` | 可聚焦／複製但不可修改。 |
| `singleLine` | `true` | 是否單行。 |
| `minLines` / `maxLines` | `1` / 依 singleLine | 文字高度限制。 |
| `leadingIcon`, `trailingIcon` | `null` | Icon slots。 |
| `visualTransformation` | `None` | 格式化顯示，不改變原始 value。 |
| `keyboardActions` | `Default` | IME action。 |

`Date`／`Time` 目前只提供文字與鍵盤語意，原生 picker 要從平台 adapter 回填 value。

### FormPasswordInput

參數與一般輸入相似，內建顯示／隱藏密碼 local state。不要把 password 寫入 saved state、log 或
analytics。

### FormTextArea

固定為多行；`minLines = 3`、`maxLines = 8`。適合 notes、description，不適合 rich text。

### FormSelect

| 參數 | 說明 |
| --- | --- |
| `value: T?` | 目前選項；null 表示尚未選擇。 |
| `options` | `FormOption(value, label, enabled)`。 |
| `onValueChange` | 選取後回報 value。 |
| `label` / `placeholder` | 欄位名稱與未選文字。 |
| `helperText` / `errorText` | 說明與錯誤。 |
| `required` / `enabled` | 視覺必要標記與整欄可用性。 |

### FormMultiSelect

`values: Set<T>` 避免重複；menu 點擊後不會自動關閉，便於連續選取。`onValuesChange` 收到完整新
集合，呼叫端應直接替換 state。

### FormCheckbox

適合同意條款或可獨立選取項目。`checked`、`onCheckedChange` 受控；`helperText` 補充影響；
`enabled` 同時停用整列與 checkbox。

### FormRadioGroup

適合 2–7 個互斥選項。`value: T?` 是目前值，option 與 group 都能 disabled；`errorText` 用於
必選但尚未完成的狀態。

### FormSwitch

適合立即生效的設定，而不是需要按「送出」的選擇。`checked`／`onCheckedChange` 受控，
`helperText` 應說明開啟後的影響。

### FormRange

| 參數 | 說明 |
| --- | --- |
| `value` / `onValueChange` | Controlled Float。 |
| `label` | 量值名稱。 |
| `valueRange` | 必填合法範圍，例如 `0f..100f`。 |
| `steps` | 中間離散刻度數；0 表示連續。 |
| `valueText` | 顯示格式，例如百分比、金額。 |

### FormSection

`title` 必填，`description` 選用，`content` 是 `ColumnScope`。只負責視覺分組，不建立獨立 form
或 validation scope。

### FormInputGroup

`prefix`／`suffix` 加上 input addon，`content` 是 `RowScope`。內容欄位通常要加
`Modifier.weight(1f)`，否則 prefix/suffix 可能擠壓輸入區。

### FormFilePicker

| 參數 | 說明 |
| --- | --- |
| `fileNames` | 已選檔名，只負責顯示。 |
| `onPick` | 開啟平台 picker 的 intent。 |
| `onClear` | 非 null 且有檔案時顯示清除。 |
| `allowMultiple` | 改變按鈕文字；真正 multiple 行為由 adapter 實作。 |
| `helperText` / `errorText` | 類型、大小限制與錯誤。 |

元件不持有 File、Blob 或路徑，避免 common UI 綁定平台型別。

### FormActions

`onSubmit` 與 `submitText` 定義主動作；`submitEnabled` 應綁定 `isValid && !isSubmitting`；只有
提供 `onReset` 才顯示 reset button。

### FormValidators

| Validator | 行為 |
| --- | --- |
| `required(message)` | 非空白。 |
| `email(message)` | 空值視為合法；需要必填時與 required 組合。 |
| `minLength(length, message)` | 最少長度。 |
| `maxLength(length, message)` | 最大長度。 |
| `pattern(regex, message)` | 空值合法，非空才比對。 |
| `all(vararg)` | 依順序回傳第一個失敗。 |

## Overlay

### Popup

`visible` 與 `onDismiss` 受控；`alignment`、`offset` 決定相對位置；`content` 是 `BoxScope`。
適合小型浮動內容，不適合需要阻斷流程的確認。

### Modal

`title` 必填，`content` 是 `ColumnScope`，`footer` 是 `RowScope`。呼叫端必須在取消、儲存成功、
外部點擊與返回鍵路徑更新 `visible`。

### Confirm

`onConfirm` 不會自動關閉，方便等待非同步結果；呼叫端成功後才關閉。`destructive = true` 會套用
危險操作樣式。`confirmText`／`dismissText` 應使用具體動詞。

### Ask

提供受控 `value`、`onValueChange`、`errorText` 與 `onConfirm(value)`。適合 rename、輸入簡短
理由；複雜表單應改用 Modal。

### Choice

使用 `FormOption<T>`；`value` 可為 null；`onValueChange` 更新暫存選擇，`onConfirm` 才執行業務
操作。若取消，通常應還原暫存 state。

### Offcanvas

`placement` 為 Start／End；寬度限制 280–420 dp；點遮罩或 close button 呼叫 `onDismiss`。
適合 filter、navigation、detail panel，不適合強制確認。

### Popover

`anchor` 與 popup 在同一個 Box；`visible`、`onDismiss` 受控；`alignment` 預設 BottomCenter。
用於補充說明或小型 action，不應包含長表單。

### Tooltip

只接收 `visible`、`text`、`alignment` 與 anchor `content`。Hover、focus、touch long-press 等事件
由平台／呼叫端轉成 visible state。Tooltip 不可放必要操作。

## Content 與文章

### H1–H6、Paragraph、Lead、Caption、SmallText

全部接受 `text`、`modifier`、`color`。使用語意 helper 的目的，是讓未來修改 typography mapping
時不需要逐頁搜尋 `TextStyle`。

### DocumentRenderer

`document` 是解析後的強型別內容，`modifier` 套在最外層 Column。Renderer 支援 heading、paragraph、
code、quote、bullet/ordered list、table、divider 與 inline styles。

### MarkdownDocument

`markdown` 改變時使用 `remember(markdown)` 重新 parse。適合已在記憶體的文件；大型或頻繁更新內容
應在 ViewModel 預先解析後直接傳 `DocumentRenderer`。

### MarkdownResource

| 參數 | 說明 |
| --- | --- |
| `path` | 相對 `composeResources/files`；可省略 `files/` prefix。 |
| `modifier` | 套用到 renderer。 |
| `loading` | 讀取期間 slot，預設 Spinner。 |
| `failure` | 接收 Throwable，預設 Material error text。 |

`readTextResource(path)` 是 suspend API；`MarkdownParser.parse()` 與 `parseInline()` 可單獨使用。

### GuideArticle

| 參數 | 說明 |
| --- | --- |
| `meta` | `ArticleMeta(title, summary, type, readingMinutes, tags)`。 |
| `sections` | 有穩定 id、title、Composable content 的 `ArticleSection`。 |
| `onSectionSelected` | Table of contents 點擊；實際 scroll 由 screen 執行。 |
| `previous`, `next` | `ArticleLink(title, onClick)`。 |

`ArticleStep` 顯示步驟號與內容；`ArticleCallout` 提供語意提示；`CodeBlock` 接收 `code` 與選用
`language`。文章類型由 `ArticleType` 明確表達讀者任務。
