# Tutorial：完成一個可儲存的帳號設定頁

這篇不是孤立 API 片段，而是一個可實際執行、可以改造成其他表單的完整 helper。
正式程式位於
[`AccountSettingsExample.kt`](../shared/src/commonMain/kotlin/cg/creamgod/consoleapp/designsystem/catalog/examples/AccountSettingsExample.kt)，
並由 common test 驗證業務欄位規則。

## 完成後會得到什麼

- Display name 與 Email 驗證。
- Plan selection、通知開關與使用量門檻。
- 非同步儲存、避免重複提交。
- 成功／失敗 Alert。
- Reset confirmation。
- Persistence 由呼叫端注入，表單不依賴特定 repository。

## 1. 定義業務資料，而不是為每個欄位建立無關變數

```kotlin
enum class AccountPlan(val label: String) {
    Free("Free"),
    Pro("Pro"),
    Team("Team"),
}

data class AccountSettings(
    val displayName: String,
    val email: String,
    val plan: AccountPlan,
    val productUpdates: Boolean,
    val warningThreshold: Float,
)
```

以 `draft.copy(email = value)` 更新後，所有欄位仍屬於同一份設定草稿，送出時不需要重新拼裝。

## 2. 建立可以獨立測試的驗證 helper

```kotlin
data class AccountSettingsValidation(
    val displayNameError: String? = null,
    val emailError: String? = null,
) {
    val isValid: Boolean get() = displayNameError == null && emailError == null
}

fun validateAccountSettings(value: AccountSettings): AccountSettingsValidation {
    val name = FormValidators.all(
        FormValidators.required("Display name is required"),
        FormValidators.minLength(2, "Use at least 2 characters"),
        FormValidators.maxLength(40, "Use at most 40 characters"),
    ).validate(value.displayName)

    val email = FormValidators.all(
        FormValidators.required("Email is required"),
        FormValidators.email("Enter a valid email address"),
    ).validate(value.email)

    return AccountSettingsValidation(name.message, email.message)
}
```

這個函式不依賴 Compose，可以在 ViewModel、server validation mapping 與 common test 重用。

## 3. 定義 helper 的真正邊界

```kotlin
@Composable
fun AccountSettingsForm(
    initialValue: AccountSettings,
    onSave: suspend (AccountSettings) -> Result<Unit>,
    modifier: Modifier = Modifier,
)
```

`AccountSettingsForm` 負責：

- 編輯草稿。
- 顯示 validation。
- 控制 saving、Alert 與 reset confirmation 等 presentation state。

它不負責：

- HTTP、database 或 cache。
- 決定登入使用者。
- 導覽離開頁面。
- 把錯誤吞掉。

這些差異讓 helper 可在 Desktop、Web、測試環境與 Preview 重用。

## 4. 欄位直接更新同一份 draft

```kotlin
var baseline by remember(initialValue) { mutableStateOf(initialValue) }
var draft by remember(initialValue) { mutableStateOf(initialValue) }
val validation = remember(draft) { validateAccountSettings(draft) }

fun updateDraft(transform: (AccountSettings) -> AccountSettings) {
    draft = transform(draft)
    successMessage = null
    saveError = null
}

FormTextInput(
    value = draft.email,
    onValueChange = { value -> updateDraft { it.copy(email = value) } },
    label = "Email",
    type = FormInputType.Email,
    required = true,
    errorText = validation.emailError,
)

FormSelect(
    value = draft.plan,
    options = AccountPlan.entries.map { FormOption(it, it.label) },
    onValueChange = { value -> updateDraft { it.copy(plan = value) } },
    label = "Plan",
)
```

把 `AccountPlan` 換成語言、時區或權限角色，這個模式仍相同。

## 5. 儲存流程必須處理 loading 與 failure

```kotlin
var saving by remember { mutableStateOf(false) }
var successMessage by remember { mutableStateOf<String?>(null) }
var saveError by remember { mutableStateOf<String?>(null) }
val scope = rememberCoroutineScope()

FormActions(
    onSubmit = {
        scope.launch {
            saving = true
            successMessage = null
            saveError = null

            val result = runCatching { onSave(draft).getOrThrow() }
            saving = false

            result.onSuccess {
                baseline = draft
                successMessage = "Account settings saved"
            }.onFailure { error ->
                saveError = error.message ?: "An unexpected error occurred"
            }
        }
    },
    submitText = if (saving) "Saving…" else "Save settings",
    submitEnabled = validation.isValid && !saving,
)
```

這段可以舉一反三到建立資料、更新密碼、提交訂單：共同規則是 loading 期間 disable、清除舊結果、
保留輸入、將錯誤顯示給使用者。

成功後把 `draft` 設成新的 `baseline`，之後按 Reset 會回到「最近一次儲存成功」的內容，
而不是過期的初始值。完整原始碼也會在儲存期間停用欄位及 Reset。

## 6. 呼叫端提供真正 persistence

```kotlin
interface AccountSettingsRepository {
    suspend fun update(value: AccountSettings)
}

@Composable
fun AccountSettingsScreen(repository: AccountSettingsRepository) {
    val initialValue = remember {
        AccountSettings(
            displayName = "Ada",
            email = "ada@example.com",
            plan = AccountPlan.Pro,
            productUpdates = true,
            warningThreshold = 80f,
        )
    }

    AccountSettingsForm(
        initialValue = initialValue,
        onSave = { editedSettings ->
            runCatching { repository.update(editedSettings) }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
```

這裡沒有 `::save` 或未定義的 `delete()`。讀者可以清楚看到 repository 如何進入畫面、資料如何
傳入 helper、錯誤如何透過 `Result` 回來。

## 7. 測試業務規則

```kotlin
@Test
fun validatesRequiredBusinessFields() {
    val invalid = validateAccountSettings(
        AccountSettings("", "invalid", AccountPlan.Free, false, 80f),
    )
    val valid = validateAccountSettings(
        AccountSettings("Ada", "ada@example.com", AccountPlan.Pro, true, 80f),
    )

    assertFalse(invalid.isValid)
    assertTrue(valid.isValid)
}
```

## 如何改成其他業務表單

| 原範例 | 可替換成 |
| --- | --- |
| `AccountSettings` | ProductDraft、TeamSettings、OrderDraft |
| `AccountPlan` | Category、Role、ShippingMethod |
| `validateAccountSettings` | 對應領域規則的 pure function |
| `onSave` | create、update、submit、publish |
| Reset Confirm | Cancel edit、discard draft、restore defaults |
| Success Alert | Toast、navigation result、inline status |

不要複製所有 UI 後只改文字。先辨認資料模型、驗證、提交邊界，再重用相同 state flow。
