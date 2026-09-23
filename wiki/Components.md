# Components

## Actions

| API | 用途 |
| --- | --- |
| `Button` | Filled、Outline、Text action |
| `ButtonGroup` | 單選 action group；使用 `ButtonItem` |
| `SegmentedButtons` | Material 3 互斥分段選擇 |
| `SplitButton` | 預設動作加替代動作 menu |
| `IconButton` | 必填無障礙說明的圖示按鈕 |
| `FloatingActionButton` / `ExtendedFloatingActionButton` | 畫面主要動作 |
| `FabMenu` | 多個相關主要動作 |
| `CloseButton` | 有 content description 的關閉 action |
| `Dropdown` | 使用 `DropdownItem` 的 button menu |

`Button` 參數：`text` 是 action 名稱；`onClick` 發送 intent；`tone` 表達主要／危險等語意；
`variant` 決定 Filled／Outline／Text 強度；`enabled` 通常綁定 `isValid && !loading`；
`leadingIcon`／`trailingIcon` 是 Composable slots。

`ButtonGroup.selected` 與 `Dropdown.onSelect` 都是業務值 `T`，不要用顯示 label 當 identifier。

```kotlin
@Composable
fun ReportActions(
    canSave: Boolean,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onExport: (String) -> Unit,
) {
    Button("Save", onClick = onSave, enabled = canSave)
    Button("Delete", onClick = onDelete, tone = Tone.Danger, variant = Variant.Outline)
    Dropdown(
        label = "Export",
        items = listOf(DropdownItem("csv", "CSV"), DropdownItem("pdf", "PDF")),
        onSelect = onExport,
    )
}
```

## Feedback

| API | 用途 |
| --- | --- |
| `Alert` | 簡短、可 dismiss 的狀態訊息 |
| `Callout` | Icon、action 與 content slots |
| `Badge` | 狀態或數量標籤 |
| `Progress` | `0f..1f` 線性進度 |
| `Spinner` | Indeterminate loading |
| `LoadingIndicator` | Material 3 expressive 等待／已知進度 indicator |
| `Snackbar` / `SnackbarHost` | 短暫結果與全域訊息佇列 |
| `Placeholder` | 可動畫 skeleton |
| `Toast` | 單一受控訊息卡片 |
| `ToastHost` | 全域 Snackbar queue；搭配 `rememberToastState` |

`Alert` 適合目前畫面內的持續狀態；跨畫面短暫結果使用 ToastHost。`ToastState.show()` 接收
`message`、`actionLabel`、`withDismissAction`、`duration`，並回傳 action 是否被執行。

```kotlin
Alert("Saved", tone = Tone.Success)
Callout("Backup", message = "Back up before migration", tone = Tone.Warning)
Badge("42", pill = true)
Progress(0.75f)
Spinner()
Placeholder(Modifier.fillMaxWidth().height(24.dp))
Toast("Saved", visible = visible, onDismiss = { visible = false })
```

## Disclosure and surfaces

| API | 用途 |
| --- | --- |
| `Collapse` | 受控 AnimatedVisibility |
| `Accordion` | 單一或多個可展開 `AccordionItem` |
| `Card` | Header、title、body、footer、click action |
| `ListGroup` | 可 selected／disabled 的 `ListItem` |
| `Carousel` | `CarouselItem`、controls 與 indicators |

`Card.onClick != null` 時整張卡可點擊；若 footer 已有多個 action，不應再設定整卡 click。
Accordion 與 Carousel 保存 local UI state，不應承載 URL、權限或 repository 狀態。

```kotlin
Collapse(expanded) { Text("Details") }
Accordion(listOf(AccordionItem("Profile") { Text("Content") }))
Card(
    title = "Profile",
    footer = { Button("Edit", onClick = { editing = true }) },
) { Text("Body") }
ListGroup(items, selected = selected, onSelect = { selected = it })
Carousel(slides, showControls = true, showIndicators = true)
```

此處的 `editing`、`selected`、`items`、`slides` 必須由呼叫端 state 提供；完整狀態流程請看
[Account Settings Tutorial](Tutorial-Account-Settings)。

## Navigation

| API | 用途 |
| --- | --- |
| `Breadcrumb` | `BreadcrumbItem` 階層導覽 |
| `Navbar` | Brand、`NavItem` 與 actions |
| `NavigationBar` | 窄畫面的底部目的地 |
| `NavigationRail` | 中型寬度的側邊目的地 |
| `NavigationDrawer` | 寬畫面常駐 drawer |
| `TopAppBar` / `BottomAppBar` / `Toolbar` | 頁面與區域 actions |
| `Tabs` | Material primary tab row |
| `Nav` | 水平／垂直 navigation |
| `Pagination` | 1-based pages 與 sibling range |
| `ScrollSpy` | 由 screen 提供 active section |

Navbar／Tabs／Nav 都使用 `NavItem(value, label, enabled, icon)`；`selected` 是目前業務值，
`onSelect` 只回報 intent。`Pagination` 使用 1-based `currentPage`，資料 request 與 loading 由
ViewModel 負責。ScrollSpy 不會自行讀取 DOM。

```kotlin
Breadcrumb(listOf(BreadcrumbItem("Home", onClick = { route = "home" }), BreadcrumbItem("Settings")))
Navbar("ConsoleApp", navItems, selected, onSelect = { selected = it })
Tabs(navItems, selected, onSelect = { selected = it })
Nav(navItems, selected, onSelect = { selected = it }, vertical = true)
Pagination(page, 20, onPageChange = { page = it })
ScrollSpy(sectionItems, activeSection, onNavigate = { section -> activeSection = section })
```

## Forms

| API | 用途 |
| --- | --- |
| `FormTextInput` | Text、email、number、phone、URL、search、date、time |
| `FormPasswordInput` | 密碼顯示切換 |
| `FormTextArea` | 多行文字 |
| `FormSelect` | 單選 `FormOption` |
| `FormMultiSelect` | 多選 `FormOption` |
| `FormCheckbox` | Boolean selection |
| `FormRadioGroup` | 單選群組 |
| `FormSwitch` | On/off setting |
| `FormRange` | Slider 與 value formatter |
| `FormSection` | 表單段落 |
| `FormInputGroup` | Prefix／suffix |
| `FormFilePicker` | 平台 file picker 的共用外觀 |
| `FormActions` | Submit／reset actions |
| `FormValidators` | required、email、length、pattern、all |
| `DatePicker` / `DateRangePicker` / `TimePicker` | 跨平台 Material 日期時間選擇 |
| `Search` | 受控搜尋與結果 slot |
| `AssistChip` / `FilterChip` / `InputChip` / `SuggestionChip` | 動作、條件、輸入與建議 chips |

所有欄位都遵循 `value + onValueChange` controlled contract。`helperText` 是格式說明；
`errorText != null` 才代表驗證失敗；`required` 只畫出標記，仍需搭配 validator。

`FormTextInput` 的 `type` 決定 keyboard type，另有 `placeholder`、`enabled`、`readOnly`、
`singleLine`、`minLines`、`maxLines`、icon slots、visual transformation 與 keyboard actions。
`FormSelect.value` 可為 null；`FormMultiSelect.values` 使用 Set。FilePicker 只顯示檔名與發送
pick intent，平台 adapter 才持有真正檔案。

```kotlin
@Composable
fun ContactForm(onSubmit: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    val validation = remember(email) { FormValidators.email().validate(email) }

    FormTextInput(
        value = email,
        onValueChange = { email = it },
        label = "Email",
        type = FormInputType.Email,
        errorText = validation.message,
        required = true,
    )
    FormActions(
        onSubmit = { onSubmit(email) },
        submitEnabled = email.isNotBlank() && validation.isValid,
        onReset = { email = "" },
    )
}
```

## Overlay

| API | 用途 |
| --- | --- |
| `Popup` | Anchored/free content |
| `Modal` | Title、body、footer dialog |
| `Confirm` | 確認／取消 |
| `Ask` | 文字輸入問題 |
| `Choice` | 單選問題 |
| `Offcanvas` | Start／End side panel |
| `BottomSheet` | 行動版底部次要工作面板 |
| `SideSheet` | 桌面／寬畫面側邊工作面板 |
| `Popover` | Anchor、title、message |
| `Tooltip` | 呼叫端控制 hover／focus visibility |

所有 overlay 的 `visible` 由呼叫端持有。`Confirm.onConfirm` 不會自動關閉，讓 ViewModel 能在
非同步失敗時保留 Dialog；成功後再清除 state。Choice 的 `onValueChange` 更新草稿，
`onConfirm` 才執行業務操作。Tooltip 的 hover／focus／long-press 事件由平台處理。

```kotlin
@Composable
fun DeleteAccountDialog(accountName: String, onDelete: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    Button("Delete account", onClick = { visible = true }, tone = Tone.Danger)
    Confirm(
        visible = visible,
        onConfirm = {
            onDelete()
            visible = false
        },
        onDismiss = { visible = false },
        title = "Delete $accountName?",
        message = "This action cannot be undone.",
        confirmText = "Delete",
        destructive = true,
    )
}
```

## Guide articles

`GuideArticle` 組合 `ArticleMeta`、`ArticleSection`、`ArticleLink`；section 中可使用
`ArticleStep`、`ArticleCallout`、`CodeBlock`。`ArticleType` 包含 Getting Started、Tutorial、
How-to、Concept、Reference、Troubleshooting 與 Migration。

更多可直接複製的完整情境放在 repository 的 `docs/components.md`。
