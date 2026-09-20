package cg.creamgod.consoleapp.designsystem.patterns.article

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.designsystem.components.Badge
import cg.creamgod.consoleapp.designsystem.components.Button
import cg.creamgod.consoleapp.designsystem.components.Callout
import cg.creamgod.consoleapp.designsystem.components.Card
import cg.creamgod.consoleapp.designsystem.components.Tone
import cg.creamgod.consoleapp.designsystem.components.Variant
import cg.creamgod.consoleapp.designsystem.tokens.BootstrapTokens

enum class ArticleType(
    val label: String,
    val description: String,
) {
    GettingStarted("入門指南", "讓新使用者在最短時間完成第一次成功操作"),
    Tutorial("逐步教學", "以連續步驟帶讀者完成一個完整成果"),
    HowTo("操作方法", "解決一個明確、可重複的工作目標"),
    Concept("概念說明", "解釋設計理念、架構與取捨"),
    Reference("API 參考", "提供精確且容易查找的參數與行為"),
    Troubleshooting("疑難排解", "從症狀、原因到修復方式逐步診斷"),
    Migration("遷移指南", "協助讀者安全地從舊 API 或舊架構升級"),
}

data class ArticleMeta(
    val title: String,
    val summary: String,
    val type: ArticleType,
    val readingMinutes: Int? = null,
    val tags: List<String> = emptyList(),
)

data class ArticleSection(
    val id: String,
    val title: String,
    val content: @Composable ColumnScope.() -> Unit,
)

data class ArticleLink(
    val title: String,
    val onClick: () -> Unit,
)

@Composable
fun GuideArticle(
    meta: ArticleMeta,
    sections: List<ArticleSection>,
    modifier: Modifier = Modifier,
    onSectionSelected: ((String) -> Unit)? = null,
    previous: ArticleLink? = null,
    next: ArticleLink? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.four),
    ) {
        ArticleHeader(meta)

        if (sections.isNotEmpty()) {
            Card(title = "本文目錄") {
                Column(verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.two)) {
                    sections.forEachIndexed { index, section ->
                        Text(
                            text = "${index + 1}. ${section.title}",
                            modifier = if (onSectionSelected == null) {
                                Modifier
                            } else {
                                Modifier.clickable { onSectionSelected(section.id) }
                            },
                            color = if (onSectionSelected == null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        sections.forEachIndexed { index, section ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.three),
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.headlineSmall,
                )
                section.content(this)
            }
            if (index != sections.lastIndex) HorizontalDivider()
        }

        if (previous != null || next != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (previous == null) {
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                } else {
                    Button(
                        text = "← ${previous.title}",
                        onClick = previous.onClick,
                        variant = Variant.Outline,
                    )
                }
                next?.let {
                    Button(text = "${it.title} →", onClick = it.onClick)
                }
            }
        }
    }
}

@Composable
fun ArticleStep(
    number: Int,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.three),
        verticalAlignment = Alignment.Top,
    ) {
        Badge(number.toString(), pill = true)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.two),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
fun ArticleCallout(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Info,
) {
    Callout(
        title = title,
        message = message,
        modifier = modifier,
        tone = tone,
    )
}

@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    language: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(BootstrapTokens.Spacing.three)) {
            language?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.inversePrimary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            SelectionContainer {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ArticleHeader(meta: ArticleMeta) {
    Column(verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.two)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.two)) {
            Badge(meta.type.label, tone = meta.type.tone, pill = true)
            meta.readingMinutes?.let { Badge("約 $it 分鐘", tone = Tone.Secondary, pill = true) }
            meta.tags.forEach { Badge(it, tone = Tone.Light, pill = true) }
        }
        Text(meta.title, style = MaterialTheme.typography.headlineLarge)
        Text(
            meta.summary,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private val ArticleType.tone: Tone
    get() = when (this) {
        ArticleType.GettingStarted -> Tone.Primary
        ArticleType.Tutorial -> Tone.Success
        ArticleType.HowTo -> Tone.Info
        ArticleType.Concept -> Tone.Secondary
        ArticleType.Reference -> Tone.Dark
        ArticleType.Troubleshooting -> Tone.Warning
        ArticleType.Migration -> Tone.Danger
    }
