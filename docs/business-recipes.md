# 業務情境實作

這份文件展示元件如何接入 state、validation、repository 與非同步結果。範例刻意把業務狀態放在
ViewModel，Composable 只渲染 state 並發送 intent。

## Recipe 1：會員資料編輯與驗證

### 業務規則

- Email 必填且格式正確。
- 顯示名稱 2–40 字。
- 儲存期間不能重複送出。
- 儲存成功顯示 inline success；失敗保留輸入並顯示錯誤。

### State 與 intent

```kotlin
data class ProfileUiState(
    val email: String = "",
    val displayName: String = "",
    val marketingEnabled: Boolean = false,
    val emailError: String? = null,
    val nameError: String? = null,
    val submitting: Boolean = false,
    val savedMessage: String? = null,
    val submitError: String? = null,
) {
    val canSubmit: Boolean
        get() = emailError == null && nameError == null &&
            email.isNotBlank() && displayName.isNotBlank() && !submitting
}

sealed interface ProfileIntent {
    data class EmailChanged(val value: String) : ProfileIntent
    data class NameChanged(val value: String) : ProfileIntent
    data class MarketingChanged(val enabled: Boolean) : ProfileIntent
    data object Submit : ProfileIntent
    data object DismissMessage : ProfileIntent
}
```

### ViewModel validation

```kotlin
private val emailValidator = FormValidators.all(
    FormValidators.required("Email is required"),
    FormValidators.email("Enter a valid email"),
)

private val nameValidator = FormValidators.all(
    FormValidators.required("Display name is required"),
    FormValidators.minLength(2),
    FormValidators.maxLength(40),
)

fun onIntent(intent: ProfileIntent) {
    when (intent) {
        is ProfileIntent.EmailChanged -> {
            val result = emailValidator.validate(intent.value)
            _state.update {
                it.copy(email = intent.value, emailError = result.message, savedMessage = null)
            }
        }
        is ProfileIntent.NameChanged -> {
            val result = nameValidator.validate(intent.value)
            _state.update {
                it.copy(displayName = intent.value, nameError = result.message, savedMessage = null)
            }
        }
        is ProfileIntent.MarketingChanged ->
            _state.update { it.copy(marketingEnabled = intent.enabled) }
        ProfileIntent.Submit -> save()
        ProfileIntent.DismissMessage ->
            _state.update { it.copy(savedMessage = null, submitError = null) }
    }
}

private fun save() = viewModelScope.launch {
    val snapshot = state.value
    if (!snapshot.canSubmit) return@launch

    _state.update { it.copy(submitting = true, submitError = null) }
    runCatching {
        profileRepository.update(
            email = snapshot.email.trim(),
            displayName = snapshot.displayName.trim(),
            marketingEnabled = snapshot.marketingEnabled,
        )
    }.onSuccess {
        _state.update { it.copy(submitting = false, savedMessage = "Profile saved") }
    }.onFailure { error ->
        _state.update {
            it.copy(submitting = false, submitError = error.message ?: "Unable to save profile")
        }
    }
}
```

### Screen

```kotlin
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val state by viewModel.state.collectAsState()

    FormSection(
        title = "Profile",
        description = "Public identity and communication preferences.",
    ) {
        state.savedMessage?.let {
            Alert(
                message = it,
                tone = Tone.Success,
                dismissible = true,
                onDismiss = { viewModel.onIntent(ProfileIntent.DismissMessage) },
            )
        }
        state.submitError?.let {
            Alert(
                title = "Save failed",
                message = it,
                tone = Tone.Danger,
                dismissible = true,
                onDismiss = { viewModel.onIntent(ProfileIntent.DismissMessage) },
            )
        }

        FormTextInput(
            value = state.email,
            onValueChange = { viewModel.onIntent(ProfileIntent.EmailChanged(it)) },
            label = "Email",
            type = FormInputType.Email,
            required = true,
            errorText = state.emailError,
        )
        FormTextInput(
            value = state.displayName,
            onValueChange = { viewModel.onIntent(ProfileIntent.NameChanged(it)) },
            label = "Display name",
            required = true,
            errorText = state.nameError,
        )
        FormSwitch(
            checked = state.marketingEnabled,
            onCheckedChange = { viewModel.onIntent(ProfileIntent.MarketingChanged(it)) },
            label = "Product updates",
            helperText = "Receive release and maintenance announcements.",
        )
        FormActions(
            onSubmit = { viewModel.onIntent(ProfileIntent.Submit) },
            submitText = if (state.submitting) "Saving…" else "Save profile",
            submitEnabled = state.canSubmit,
        )
    }
}
```

關鍵點：欄位元件不驗證 repository 規則、不保存 submitting；它們只顯示 ViewModel state。

## Recipe 2：刪除確認、錯誤保留與 Undo

### State flow

```text
Idle → Confirming(item) → Deleting(item) → Deleted(item)
                         ↘ Failed(item, message)
```

### UI state

```kotlin
sealed interface DeleteState {
    data object Idle : DeleteState
    data class Confirming(val item: Item) : DeleteState
    data class Deleting(val item: Item) : DeleteState
    data class Failed(val item: Item, val message: String) : DeleteState
}
```

### Screen integration

```kotlin
val toast = rememberToastState()
val scope = rememberCoroutineScope()
val deleteState by viewModel.deleteState.collectAsState()

val target = when (val value = deleteState) {
    is DeleteState.Confirming -> value.item
    is DeleteState.Deleting -> value.item
    is DeleteState.Failed -> value.item
    DeleteState.Idle -> null
}

Confirm(
    visible = target != null,
    title = "Delete ${target?.name}?",
    message = when (deleteState) {
        is DeleteState.Failed -> (deleteState as DeleteState.Failed).message
        is DeleteState.Deleting -> "Deleting…"
        else -> "You can undo this action for a short time."
    },
    confirmText = if (deleteState is DeleteState.Deleting) "Deleting…" else "Delete",
    destructive = true,
    onDismiss = viewModel::cancelDelete,
    onConfirm = {
        val item = target ?: return@Confirm
        scope.launch {
            val deleted = viewModel.delete(item.id)
            if (deleted) {
                val result = toast.show("${item.name} deleted", actionLabel = "Undo")
                if (result == SnackbarResult.ActionPerformed) viewModel.restore(item.id)
            }
        }
    },
)
```

實務上 `Confirm` 不會自動關閉，因此失敗時可以保留 Dialog 與錯誤內容；成功後 ViewModel 切回
Idle。刪除 API 若不可恢復，不應提供假的 Undo。

## Recipe 3：搜尋、篩選與 server-side pagination

### 查詢模型

```kotlin
data class UserQuery(
    val keyword: String = "",
    val roles: Set<String> = emptySet(),
    val enabledOnly: Boolean = false,
    val page: Int = 1,
    val pageSize: Int = 20,
)

data class UserListUiState(
    val query: UserQuery = UserQuery(),
    val rows: List<User> = emptyList(),
    val pageCount: Int = 0,
    val loading: Boolean = false,
    val error: String? = null,
    val filterOpen: Boolean = false,
)
```

任何 keyword 或 filter 改變都應把 page 重設為 1，再由 ViewModel debounce 並重新查詢：

```kotlin
fun updateKeyword(value: String) {
    _state.update { it.copy(query = it.query.copy(keyword = value, page = 1)) }
    scheduleSearch()
}

fun updateRoles(values: Set<String>) {
    _state.update { it.copy(query = it.query.copy(roles = values, page = 1)) }
    reload()
}

fun changePage(page: Int) {
    _state.update { it.copy(query = it.query.copy(page = page)) }
    reload()
}
```

### Screen

```kotlin
FormTextInput(
    value = state.query.keyword,
    onValueChange = viewModel::updateKeyword,
    label = "Search users",
    type = FormInputType.Search,
    trailingIcon = {
        Button("Filters", onClick = viewModel::openFilters, variant = Variant.Text)
    },
)

when {
    state.loading && state.rows.isEmpty() -> repeat(5) {
        Placeholder(Modifier.fillMaxWidth().height(56.dp))
    }
    state.error != null -> Alert(
        title = "Unable to load users",
        message = state.error,
        tone = Tone.Danger,
    )
    state.rows.isEmpty() -> Callout(
        title = "No users found",
        message = "Change the keyword or clear filters.",
    )
    else -> ListGroup(
        items = state.rows.map { ListItem(it.id, it.name, it.email) },
        selected = selectedUserId,
        onSelect = viewModel::selectUser,
    )
}

Pagination(
    currentPage = state.query.page,
    pageCount = state.pageCount,
    onPageChange = viewModel::changePage,
)

Offcanvas(
    visible = state.filterOpen,
    onDismiss = viewModel::closeFilters,
    title = "Filters",
) {
    FormMultiSelect(
        values = state.query.roles,
        options = roleOptions,
        onValuesChange = viewModel::updateRoles,
        label = "Roles",
    )
    FormSwitch(
        checked = state.query.enabledOnly,
        onCheckedChange = viewModel::updateEnabledOnly,
        label = "Enabled users only",
    )
}
```

關鍵點：Pagination 只顯示頁碼；query、debounce、取消舊 request 與錯誤重試都屬於 ViewModel。

## Recipe 4：跨平台附件上傳

不要把 JVM `File` 或 browser `File` 傳進 common UI。先定義平台中立 metadata：

```kotlin
data class PickedAttachment(
    val id: String,
    val name: String,
    val mediaType: String?,
    val sizeBytes: Long,
)

interface AttachmentPicker {
    suspend fun pick(multiple: Boolean): List<PickedAttachment>
    suspend fun readBytes(id: String): ByteArray
}
```

ViewModel 接收 picker 結果、驗證大小／類型，再更新 state：

```kotlin
fun pickFiles() = viewModelScope.launch {
    val picked = attachmentPicker.pick(multiple = true)
    val invalid = picked.firstOrNull { it.sizeBytes > 10 * 1024 * 1024 }
    _state.update {
        if (invalid != null) {
            it.copy(fileError = "${invalid.name} exceeds 10 MB")
        } else {
            it.copy(files = picked, fileError = null)
        }
    }
}
```

```kotlin
FormFilePicker(
    fileNames = state.files.map { it.name },
    onPick = viewModel::pickFiles,
    onClear = viewModel::clearFiles,
    label = "Attachments",
    helperText = "PNG or PDF; maximum 10 MB per file.",
    errorText = state.fileError,
    enabled = !state.uploading,
    allowMultiple = true,
)

state.uploadProgress?.let { Progress(it, tone = Tone.Info) }
```

若上傳需要 retry，保留 PickedAttachment id，讓 adapter 能再次讀取 bytes；不要只保存檔名。

## Recipe 5：部署流程的暫存選擇與危險確認

正式環境部署需要先選 target，再確認不可逆操作。Choice 中的 selection 是草稿，只有 confirm
才寫入部署 intent：

```kotlin
data class DeployUiState(
    val choiceVisible: Boolean = false,
    val draftEnvironment: Environment? = null,
    val confirmVisible: Boolean = false,
    val deploying: Boolean = false,
)

Choice(
    visible = state.choiceVisible,
    value = state.draftEnvironment,
    options = Environment.entries.map { FormOption(it, it.label) },
    onValueChange = viewModel::selectDraftEnvironment,
    onConfirm = { environment ->
        if (environment == Environment.Production) {
            viewModel.requestProductionConfirmation()
        } else {
            viewModel.deploy(environment)
        }
    },
    onDismiss = viewModel::cancelChoice,
    title = "Deploy environment",
    message = "Choose where this version should run.",
)

Confirm(
    visible = state.confirmVisible,
    title = "Deploy to production?",
    message = "This immediately replaces the current production version.",
    confirmText = if (state.deploying) "Deploying…" else "Deploy",
    destructive = true,
    onConfirm = viewModel::confirmProductionDeploy,
    onDismiss = viewModel::cancelProductionDeploy,
)
```

取消 Choice 時清除 draft，避免下次開啟殘留過期選擇。

## Recipe 6：內建說明中心與 Markdown 文件

將 release notes、操作指南與 troubleshooting 放在：

```text
shared/src/commonMain/composeResources/files/help/
├── getting-started.md
├── account-security.md
└── troubleshooting.md
```

用 route 只保存文件 id，不保存完整路徑：

```kotlin
enum class HelpDocument(val resourcePath: String) {
    GettingStarted("help/getting-started.md"),
    AccountSecurity("help/account-security.md"),
    Troubleshooting("help/troubleshooting.md"),
}
```

```kotlin
@Composable
fun HelpScreen(document: HelpDocument, onRetry: () -> Unit) {
    MarkdownResource(
        path = document.resourcePath,
        modifier = Modifier.fillMaxWidth(),
        loading = {
            Column {
                Placeholder(Modifier.fillMaxWidth().height(36.dp))
                Placeholder(Modifier.fillMaxWidth().height(120.dp))
            }
        },
        failure = { error ->
            Callout(
                title = "Document unavailable",
                message = error.message,
                tone = Tone.Danger,
                action = { Button("Retry", onClick = onRetry, variant = Variant.Text) },
            )
        },
    )
}
```

若文件來自網路，repository 負責下載、cache 與版本；取得 Markdown String 後交給
`MarkdownDocument`。不要讓 renderer 同時負責 HTTP 與 cache policy。
