# Tutorial：可儲存的帳號設定 helper

這個教學使用 repository 中真正會編譯及測試的程式碼，不使用未定義的 `::save`、`items` 或
`viewModel` 當作捷徑。完整原始碼：
[`AccountSettingsExample.kt`](../shared/src/commonMain/kotlin/cg/creamgod/consoleapp/designsystem/catalog/examples/AccountSettingsExample.kt)。

## Helper 的輸入與輸出

```kotlin
@Composable
fun AccountSettingsForm(
    initialValue: AccountSettings,
    onSave: suspend (AccountSettings) -> Result<Unit>,
    modifier: Modifier = Modifier,
)
```

- `initialValue` 是 repository 載入後的畫面初值。
- `onSave` 接收完整草稿；成功回傳 `Result.success(Unit)`，失敗保留使用者輸入並顯示錯誤。
- helper 擁有欄位草稿、saving、Alert 與 reset confirmation。
- 呼叫端仍擁有 API、database、登入使用者與 navigation。

## 呼叫端接上實際 repository

```kotlin
interface AccountSettingsRepository {
    suspend fun update(value: AccountSettings)
}

@Composable
fun AccountSettingsScreen(repository: AccountSettingsRepository) {
    val loadedSettings = remember {
        AccountSettings(
            displayName = "Ada",
            email = "ada@example.com",
            plan = AccountPlan.Pro,
            productUpdates = true,
            warningThreshold = 80f,
        )
    }

    AccountSettingsForm(
        initialValue = loadedSettings,
        onSave = { edited -> runCatching { repository.update(edited) } },
        modifier = Modifier.fillMaxWidth(),
    )
}
```

資料路徑是 `repository → initialValue → draft → onSave → repository`。因此測試可替換 fake
repository，Preview 也不需要真實網路。

## 為何保存 baseline

表單同時保存 `baseline` 和 `draft`。輸入只改 draft；儲存成功後才把 draft 提升為新的
baseline；Reset 回到最近一次成功儲存的內容。這避免 Reset 回到已過期的畫面初值。

## 驗證規則可以單獨測試

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

把 `AccountSettings` 換成 ProductDraft、OrderDraft 或 TeamSettings，保留相同的
「typed model → pure validation → controlled fields → async save → result feedback」結構，
就是可以舉一反三的表單 helper。

更細的逐段說明見 [`docs/tutorial-account-settings.md`](../docs/tutorial-account-settings.md)。
