# Business Recipes

元件範例必須讓讀者看得出資料來源、狀態擁有者與副作用邊界。完整表單請先看
[Account Settings Tutorial](Tutorial-Account-Settings)；本頁補充常見業務模式。

## 刪除：確認、loading 與失敗保留

```kotlin
data class Item(val id: String, val name: String)

interface ItemRepository {
    suspend fun delete(id: String)
}

@Composable
fun DeleteItemAction(item: Item, repository: ItemRepository) {
    var confirmationVisible by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Button(
        text = "Delete ${item.name}",
        onClick = { confirmationVisible = true },
        tone = Tone.Danger,
        enabled = !deleting,
    )

    errorMessage?.let { message ->
        Alert(
            title = "Delete failed",
            message = message,
            tone = Tone.Danger,
            dismissible = true,
            onDismiss = { errorMessage = null },
        )
    }

    Confirm(
        visible = confirmationVisible,
        title = "Delete ${item.name}?",
        message = "This action cannot be undone.",
        confirmText = if (deleting) "Deleting…" else "Delete",
        destructive = true,
        onDismiss = { if (!deleting) confirmationVisible = false },
        onConfirm = {
            if (!deleting) scope.launch {
                deleting = true
                runCatching { repository.delete(item.id) }
                    .onSuccess { confirmationVisible = false }
                    .onFailure { error ->
                        errorMessage = error.message ?: "Unable to delete item"
                    }
                deleting = false
            }
        },
    )
}
```

`Alert.onDismiss` 只清除錯誤；真正刪除只發生在 `Confirm.onConfirm`。失敗時 dialog 保留，
使用者可以重試。若後端真的支援 restore API，再加入 Undo，不能只在畫面上假裝復原。

## 搜尋與分頁：由呼叫端擁有 query

```kotlin
data class UserQuery(
    val keyword: String = "",
    val roles: Set<String> = emptySet(),
    val page: Int = 1,
)

@Composable
fun UserSearchControls(
    query: UserQuery,
    pageCount: Int,
    roleOptions: List<FormOption<String>>,
    onQueryChange: (UserQuery) -> Unit,
) {
    var filtersVisible by remember { mutableStateOf(false) }

    FormTextInput(
        value = query.keyword,
        onValueChange = { keyword -> onQueryChange(query.copy(keyword = keyword, page = 1)) },
        label = "Search",
        type = FormInputType.Search,
    )
    Button("Filters", onClick = { filtersVisible = true }, variant = Variant.Text)
    Pagination(
        currentPage = query.page,
        pageCount = pageCount,
        onPageChange = { page -> onQueryChange(query.copy(page = page)) },
    )
    Offcanvas(
        visible = filtersVisible,
        onDismiss = { filtersVisible = false },
        title = "Filters",
    ) {
        FormMultiSelect(
            values = query.roles,
            options = roleOptions,
            onValuesChange = { roles -> onQueryChange(query.copy(roles = roles, page = 1)) },
            label = "Roles",
        )
    }
}
```

關鍵字或 filter 改變時重設 `page = 1`。真正的 debounce、取消舊 request、loading 和 error
應放在 query 的擁有者（通常是 ViewModel），Pagination 本身不呼叫 API。

## 檔案選擇：common UI 只接 platform adapter

```kotlin
data class PickedAttachment(val id: String, val name: String, val sizeBytes: Long)

@Composable
fun AttachmentField(
    files: List<PickedAttachment>,
    uploading: Boolean,
    error: String?,
    onPickFiles: () -> Unit,
    onClearFiles: () -> Unit,
) {
    FormFilePicker(
        fileNames = files.map { it.name },
        onPick = onPickFiles,
        onClear = onClearFiles,
        label = "Attachments",
        errorText = error,
        enabled = !uploading,
        allowMultiple = true,
    )
}
```

JVM 的 `File`、瀏覽器的 `File` 不應進入 common UI。平台 adapter 負責 picker 與 bytes，
畫面只接收可跨平台的 metadata 及 callbacks。

## 從範例舉一反三

| 需求 | 保留的模式 | 替換的業務資料 |
| --- | --- | --- |
| 刪除會員 | Confirm → suspend repository → result Alert | `Item` 改成 `User` |
| 商品搜尋 | immutable query → callback → ViewModel | role 改成 category |
| 上傳發票 | platform adapter → metadata → upload | attachment 改成 invoice |
| 編輯訂單 | draft → pure validation → async save | 使用帳號設定教學的結構 |

更完整的 ViewModel state machine、repository 實作與部署確認流程見
[`docs/business-recipes.md`](../docs/business-recipes.md)。
