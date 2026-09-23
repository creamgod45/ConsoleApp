# Material 3 Components

框架已提供完整的 Material 3 常用分類，並以 controlled state 封裝成可在 Desktop、JS 與
Wasm 共用的 API。本頁先提供可複製範例；各元件的既有詳細說明另見 [Components](Components)。

## API 對照

| 類別 | API |
| --- | --- |
| Actions | `Button`, `ButtonGroup`, `IconButton`, `SegmentedButtons`, `SplitButton` |
| FAB | `FloatingActionButton`, `ExtendedFloatingActionButton`, `FabMenu` |
| Pickers | `DatePicker`, `DateRangePicker`, `TimePicker` |
| Navigation | `NavigationBar`, `NavigationRail`, `NavigationDrawer` |
| App bars | `TopAppBar`, `BottomAppBar`, `Toolbar` |
| Sheets | `BottomSheet`, `SideSheet` |
| Chips | `AssistChip`, `FilterChip`, `InputChip`, `SuggestionChip` |
| Feedback | `LoadingIndicator`, `Progress`, `Snackbar`, `SnackbarHost` |
| Search / structure | `Search`, `HorizontalDivider`, `VerticalDivider` |

## 實際狀態範例

```kotlin
enum class Route { Home, Search, Settings }

@Composable
fun MainNavigation() {
    var route by remember { mutableStateOf(Route.Home) }
    val items = listOf(
        NavItem(Route.Home, "首頁", icon = { Icon(Icons.Default.Home, null) }),
        NavItem(Route.Search, "搜尋", icon = { Icon(Icons.Default.Search, null) }),
        NavItem(Route.Settings, "設定", icon = { Icon(Icons.Default.Settings, null) }),
    )

    NavigationBar(
        items = items,
        selected = route,
        onSelect = { route = it },
    )
}
```

`route` 是真正的 screen state；`onSelect` 才改變它。相同原則適用 Picker 的
`value/onValueChange`、Sheet 的 `visible/onDismiss`、Search 的 `query/onQueryChange`。

## 日期與時間

```kotlin
var open by remember { mutableStateOf(false) }
var dateMillis by remember { mutableStateOf<Long?>(null) }

Button("選擇到貨日期", onClick = { open = true })
DatePicker(
    visible = open,
    selectedDateMillis = dateMillis,
    onDateSelected = { dateMillis = it },
    onDismiss = { open = false },
    title = "到貨日期",
)
```

日期以 UTC epoch milliseconds 回傳；時間使用 `TimeValue(hour, minute)`。呼叫端負責格式化、
驗證與送入 domain model。

## Snackbar

```kotlin
val host = rememberSnackbarState()
val scope = rememberCoroutineScope()

SnackbarHost(host)
Button("儲存", onClick = {
    scope.launch { host.showSnackbar("設定已儲存") }
})
```

每個 screen scaffold 建議只放一個 host。網路與 repository 工作由 ViewModel 負責，host 只顯示
成功、失敗與重試 action。
