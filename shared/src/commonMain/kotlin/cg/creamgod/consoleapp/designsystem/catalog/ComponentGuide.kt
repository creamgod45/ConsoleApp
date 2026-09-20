package cg.creamgod.consoleapp.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.designsystem.components.Accordion
import cg.creamgod.consoleapp.designsystem.components.AccordionItem
import cg.creamgod.consoleapp.designsystem.components.Alert
import cg.creamgod.consoleapp.designsystem.components.Badge
import cg.creamgod.consoleapp.designsystem.components.Button
import cg.creamgod.consoleapp.designsystem.components.Card
import cg.creamgod.consoleapp.designsystem.components.Confirm
import cg.creamgod.consoleapp.designsystem.components.NavItem
import cg.creamgod.consoleapp.designsystem.components.Progress
import cg.creamgod.consoleapp.designsystem.components.Tabs
import cg.creamgod.consoleapp.designsystem.components.Tone
import cg.creamgod.consoleapp.designsystem.components.Variant
import cg.creamgod.consoleapp.designsystem.components.form.FormInputType
import cg.creamgod.consoleapp.designsystem.components.form.FormTextInput
import cg.creamgod.consoleapp.designsystem.components.form.FormValidators
import cg.creamgod.consoleapp.designsystem.foundation.ComponentStatus
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleCallout
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleMeta
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleSection
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleStep
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleType
import cg.creamgod.consoleapp.designsystem.patterns.article.CodeBlock
import cg.creamgod.consoleapp.designsystem.patterns.article.GuideArticle
import cg.creamgod.consoleapp.designsystem.tokens.BootstrapTokens

/**
 * In-app introduction and live playground for the ConsoleApp component library.
 * Add this composable to any screen; it owns demo-only state and mutates no app data.
 */
@Composable
fun ComponentGuide(
    modifier: Modifier = Modifier,
) {
    var section by remember { mutableStateOf(GuideSection.Overview) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(BootstrapTokens.Spacing.three),
        verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.three),
    ) {
        Text("ConsoleApp Components", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Compose-first、跨平台、短名稱、可控制狀態。Bootstrap 提供熟悉的元件語言，Compose 負責真正的行為。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )

        Tabs(
            items = GuideSection.entries.map { NavItem(it, it.label) },
            selected = section,
            onSelect = { section = it },
        )

        when (section) {
            GuideSection.Overview -> OverviewGuide()
            GuideSection.Article -> ArticleGuideDemo()
            GuideSection.Playground -> ComponentPlayground()
            GuideSection.Catalog -> CatalogGuide()
        }
    }
}

@Composable
private fun ArticleGuideDemo() {
    GuideArticle(
        meta = ArticleMeta(
            title = "第一次使用元件庫",
            summary = "從匯入 package 到顯示第一個成功訊息，完成一條最短可行路徑。",
            type = ArticleType.GettingStarted,
            readingMinutes = 3,
            tags = listOf("Compose", "Quick start"),
        ),
        sections = listOf(
            ArticleSection("import", "匯入元件") {
                Text("一般元件集中在同一個 package，可用 wildcard 或明確 import。")
                CodeBlock(
                    code = "import cg.creamgod.consoleapp.designsystem.components.*",
                    language = "Kotlin",
                )
            },
            ArticleSection("first-component", "放入第一個元件") {
                ArticleStep(1, "選擇語意") {
                    Text("成功訊息使用 Tone.Success，不需要自行指定綠色色碼。")
                }
                ArticleStep(2, "加入畫面") {
                    CodeBlock(
                        code = "Alert(\"設定已更新\", tone = Tone.Success)",
                        language = "Kotlin",
                    )
                }
                ArticleCallout(
                    title = "完成條件",
                    message = "元件在明暗模式都清楚可讀，且訊息不只依靠顏色表達。",
                    tone = Tone.Success,
                )
            },
        ),
    )
}

@Composable
private fun OverviewGuide() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Alert(
            title = "使用方式",
            message = "只需 import cg.creamgod.consoleapp.designsystem.components.*，即可使用 Button、Card、Modal 等短名稱。",
            tone = Tone.Info,
        )
        PrincipleCard(
            title = "1. Compose-first",
            text = "元件以 commonMain 的 Compose API 實作，不模擬 HTML class，也不依賴 Bootstrap JavaScript。",
        )
        PrincipleCard(
            title = "2. 狀態由畫面擁有",
            text = "輸入值、visible、selected 與 currentPage 都由呼叫端控制，元件只負責呈現與回報事件。",
        )
        PrincipleCard(
            title = "3. 語意優先",
            text = "使用 Tone.Primary、Success、Danger 等意圖，而不是把色碼散落在畫面中。",
        )
        PrincipleCard(
            title = "4. 平台能力隔離",
            text = "檔案、日期、時間等原生能力透過 adapter 注入，共用元件不直接依賴 JVM 或瀏覽器 API。",
        )
    }
}

@Composable
private fun ComponentPlayground() {
    var name by remember { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    val validation = FormValidators.required("請輸入名稱").validate(name)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("互動範例", style = MaterialTheme.typography.titleLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button("Primary", onClick = {}, tone = Tone.Primary)
            Button("Success", onClick = {}, tone = Tone.Success)
            Button("Danger", onClick = { showConfirm = true }, tone = Tone.Danger)
            Button("Outline", onClick = {}, variant = Variant.Outline)
        }
        FormTextInput(
            value = name,
            onValueChange = {
                name = it
                saved = false
            },
            label = "名稱",
            type = FormInputType.Text,
            required = true,
            errorText = validation.message,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            text = "儲存",
            onClick = { saved = true },
            enabled = validation.isValid,
        )
        if (saved) Alert("已儲存 $name", tone = Tone.Success)
        Progress(if (saved) 1f else 0.4f)
        Accordion(
            items = listOf(
                AccordionItem("為什麼使用 controlled state？", initiallyExpanded = true) {
                    Text("它讓 ViewModel、驗證、測試與畫面生命週期保持單一資料來源。")
                },
                AccordionItem("為什麼保留 Material 3？") {
                    Text("Material 3 提供跨平台互動與無障礙基礎；Bootstrap 只作為分類與語意參考。")
                },
            ),
        )
        Confirm(
            visible = showConfirm,
            title = "危險操作示範",
            message = "Danger confirmation 會使用錯誤語意色。",
            destructive = true,
            onConfirm = { showConfirm = false },
            onDismiss = { showConfirm = false },
        )
    }
}

@Composable
private fun CatalogGuide() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        bootstrapComponentCatalog.groupBy { it.category }.forEach { (category, entries) ->
            Card(title = category) {
                entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                entry.composeEquivalent,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Badge(
                            text = entry.status.label,
                            tone = entry.status.tone,
                            pill = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrincipleCard(title: String, text: String) {
    Card(title = title) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

private enum class GuideSection(val label: String) {
    Overview("理念"),
    Article("文章"),
    Playground("範例"),
    Catalog("索引"),
}

private val ComponentStatus.label: String
    get() = when (this) {
        ComponentStatus.Ready -> "Ready"
        ComponentStatus.Planned -> "Planned"
        ComponentStatus.PlatformAdapter -> "Adapter"
    }

private val ComponentStatus.tone: Tone
    get() = when (this) {
        ComponentStatus.Ready -> Tone.Success
        ComponentStatus.Planned -> Tone.Secondary
        ComponentStatus.PlatformAdapter -> Tone.Warning
    }
