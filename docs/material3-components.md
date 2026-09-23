# Material 3 元件與實務範例

這批 API 位於 `cg.creamgod.consoleapp.designsystem.components.*`，可在 `commonMain` 同時供
Desktop JVM、JavaScript 與 Wasm 使用。它們採用 controlled state：元件顯示值，呼叫端保存
真正的業務狀態，callback 回報使用者的操作。

```kotlin
import cg.creamgod.consoleapp.designsystem.components.*
```

## 完整對照

| Material 3 類別 | Framework API |
| --- | --- |
| Buttons / button groups | `Button`, `ButtonGroup`, `SegmentedButtons`, `SplitButton` |
| FAB / extended FAB / FAB menu | `FloatingActionButton`, `ExtendedFloatingActionButton`, `FabMenu` |
| Icon buttons | `IconButton`, `CloseButton` |
| Date & time pickers | `DatePicker`, `DateRangePicker`, `TimePicker` |
| Loading & progress | `LoadingIndicator`, `Progress`, `Spinner` |
| Navigation | `NavigationBar`, `NavigationRail`, `NavigationDrawer` |
| Sheets | `BottomSheet`, `SideSheet` |
| App bars / toolbars | `TopAppBar`, `BottomAppBar`, `Toolbar` |
| Chips | `AssistChip`, `FilterChip`, `InputChip`, `SuggestionChip` |
| Search | `Search` |
| Snackbar | `Snackbar`, `SnackbarHost`, `rememberSnackbarState` |
| Divider | `HorizontalDivider`, `VerticalDivider` |
| Other Material controls | `Card`, `Badge`, `Carousel`, `FormCheckbox`, `FormRadioGroup`, `FormSwitch`, `FormRange`, `Tabs`, `FormTextInput`, `Dropdown`, `Modal`, `Tooltip` |

## FAB 與相關動作

`FloatingActionButton` 執行目前畫面的單一主要動作；有文字時使用
`ExtendedFloatingActionButton`。同一主要目的有數個建立方式時才使用 `FabMenu`。

```kotlin
enum class CreateTarget { Document, Folder }

@Composable
fun CreateActions(onCreate: (CreateTarget) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }

    FabMenu(
        expanded = menuOpen,
        onExpandedChange = { menuOpen = it },
        items = listOf(
            FabMenuItem(CreateTarget.Document, "新增文件", { Icon(Icons.Default.Description, null) }),
            FabMenuItem(CreateTarget.Folder, "新增資料夾", { Icon(Icons.Default.CreateNewFolder, null) }),
        ),
        onSelect = onCreate,
    )
}
```

`items.value` 是實際業務值，不要用顯示文字判斷動作。`expanded` 由畫面保存，因此路由切換
或送出成功時也能主動關閉。

## Segmented 與 Split button

```kotlin
enum class ViewMode { List, Grid }

@Composable
fun DraftActions(
    onSave: (publish: Boolean) -> Unit,
    onDuplicate: () -> Unit,
) {
    var viewMode by remember { mutableStateOf(ViewMode.List) }
    SegmentedButtons(
        items = listOf(ButtonItem(ViewMode.List, "清單"), ButtonItem(ViewMode.Grid, "格狀")),
        selected = viewMode,
        onSelect = { viewMode = it },
    )

    SplitButton(
        text = "儲存",
        onClick = { onSave(false) },
        items = listOf(
            DropdownItem("publish", "儲存並發布"),
            DropdownItem("duplicate", "另存副本"),
        ),
        onSelect = { action ->
            when (action) {
                "publish" -> onSave(true)
                "duplicate" -> onDuplicate()
            }
        },
    )
}
```

`onClick` 永遠是預設動作；`onSelect` 處理下拉選單的替代動作。

## 日期、範圍與時間

Picker 回傳日期的 UTC epoch milliseconds；時間則使用 `TimeValue(hour, minute)`，避免
`java.time` 進入 `commonMain` API。

```kotlin
@Composable
fun DeliverySchedule() {
    var showDate by remember { mutableStateOf(false) }
    var deliveryDate by remember { mutableStateOf<Long?>(null) }
    var deliveryTime by remember { mutableStateOf(TimeValue(9, 30)) }

    Button("選擇配送日", onClick = { showDate = true })
    DatePicker(
        visible = showDate,
        selectedDateMillis = deliveryDate,
        onDateSelected = { selected -> deliveryDate = selected },
        onDismiss = { showDate = false },
        title = "配送日期",
    )

    var showTime by remember { mutableStateOf(false) }
    Button("選擇時間", onClick = { showTime = true })
    TimePicker(
        visible = showTime,
        value = deliveryTime,
        onValueChange = { deliveryTime = it },
        onDismiss = { showTime = false },
        is24Hour = true,
    )
}
```

`DateRangePicker` 使用 `DateRangeValue(startMillis, endMillis)`；只有起訖日期都選好時確認按鈕
才可用。格式化顯示文字應放在平台或 domain adapter，不要用時區不明的字串當資料值。

## 導覽與 App bar

```kotlin
enum class Route { Home, Search, Settings }

@Composable
fun PhoneNavigation(route: Route, onRouteChange: (Route) -> Unit) {
    val items = listOf(
        NavItem(Route.Home, "首頁", icon = { Icon(Icons.Default.Home, null) }),
        NavItem(Route.Search, "搜尋", icon = { Icon(Icons.Default.Search, null) }),
        NavItem(Route.Settings, "設定", icon = { Icon(Icons.Default.Settings, null) }),
    )
    NavigationBar(items = items, selected = route, onSelect = onRouteChange)
}
```

- `NavigationBar`：窄畫面底部的少量主要目的地。
- `NavigationRail`：中型寬度的側邊導覽，可用 `header` 放 FAB 或品牌。
- `NavigationDrawer`：寬畫面常駐 drawer；`content` 是頁面內容。
- `TopAppBar`：頁面標題、返回與頁級 actions。
- `BottomAppBar`：底部常用 actions。
- `Toolbar`：編輯器或資料表的區域操作，不代表全站導覽。

## Sheet

```kotlin
@Composable
fun OrderFilters(onApply: (onlyUnpaid: Boolean) -> Unit) {
    var showFilters by remember { mutableStateOf(false) }
    var onlyUnpaid by remember { mutableStateOf(false) }

    Button("篩選", onClick = { showFilters = true })
    SideSheet(
        visible = showFilters,
        onDismiss = { showFilters = false },
        title = "訂單篩選",
    ) {
        FormCheckbox(
            checked = onlyUnpaid,
            onCheckedChange = { onlyUnpaid = it },
            label = "只顯示未付款",
        )
        Button("套用", onClick = {
            onApply(onlyUnpaid)
            showFilters = false
        })
    }
}
```

`BottomSheet` 適合行動版次要工作；`SideSheet` 適合桌面寬畫面的篩選與詳細資訊。兩者的
`visible` 與 `onDismiss` 都必須接同一份狀態。

## Chips 與搜尋

```kotlin
data class OrderSuggestion(val id: String, val title: String)

@Composable
fun OrderSearch(
    suggestions: List<OrderSuggestion>,
    onSearch: (query: String, tags: Set<String>) -> Unit,
    onOpenSuggestion: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    Search(
        query = query,
        onQueryChange = { query = it },
        onSearch = { submittedQuery -> onSearch(submittedQuery, selectedTags) },
        resultsVisible = suggestions.isNotEmpty(),
    ) {
        suggestions.forEach { suggestion ->
            ListGroup(
                items = listOf(ListItem(suggestion.id, suggestion.title)),
                selected = null,
                onSelect = onOpenSuggestion,
            )
        }
    }

    listOf("急件", "未付款", "已出貨").forEach { tag ->
        FilterChip(
            label = tag,
            selected = tag in selectedTags,
            onSelectedChange = { checked ->
                selectedTags = if (checked) selectedTags + tag else selectedTags - tag
            },
        )
    }
}
```

`AssistChip` 是輔助動作，`FilterChip` 是可切換條件，`InputChip` 表示已輸入項目並可移除，
`SuggestionChip` 提供建議。不要只靠顏色表示 selected 狀態。

## Snackbar 與非同步結果

`SnackbarHostState.showSnackbar()` 是 suspend function，所以從 Composable 事件啟動時需要
`rememberCoroutineScope()`：

```kotlin
@Composable
fun SaveWithFeedback(onSave: suspend () -> Boolean) {
    val snackbarState = rememberSnackbarState()
    val scope = rememberCoroutineScope()

    Box {
        SnackbarHost(state = snackbarState, modifier = Modifier.align(Alignment.BottomCenter))
        Button("儲存", onClick = {
            scope.launch {
                val saved = onSave()
                snackbarState.showSnackbar(
                    message = if (saved) "已儲存" else "儲存失敗",
                    actionLabel = if (saved) null else "重試",
                )
            }
        })
    }
}
```

若 repository 呼叫本身較久，應由 ViewModel 執行並回傳事件；畫面只負責把事件顯示在唯一的
`SnackbarHost`。`Snackbar` 是需要完全自行控制位置與生命週期時的低階版本。

## Divider 與 Loading

`HorizontalDivider`、`VerticalDivider` 可設定 `thickness` 與 `color`。`LoadingIndicator()` 是
Material 3 expressive 的變形等待指示；`LoadingIndicator(progress = { uploadProgress })` 用於已知
進度。一般長條進度仍使用 `Progress(value = 0f..1f)`。
