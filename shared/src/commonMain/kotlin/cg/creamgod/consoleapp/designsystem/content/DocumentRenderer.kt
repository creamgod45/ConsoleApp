package cg.creamgod.consoleapp.designsystem.content

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.designsystem.patterns.article.CodeBlock
import cg.creamgod.consoleapp.designsystem.tokens.BootstrapTokens

@Composable
fun DocumentRenderer(
    document: Document,
    modifier: Modifier = Modifier,
) {
    SelectionContainer {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.three),
        ) {
            document.blocks.forEach { block -> RenderBlock(block) }
        }
    }
}

@Composable
fun MarkdownDocument(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val document = remember(markdown) { MarkdownParser.parse(markdown) }
    DocumentRenderer(document, modifier)
}

@Composable
private fun RenderBlock(block: DocumentBlock) {
    when (block) {
        is DocumentBlock.Heading -> RichText(block.content, headingStyle(block.level))
        is DocumentBlock.Paragraph -> RichText(block.content, MaterialTheme.typography.bodyLarge)
        is DocumentBlock.Code -> CodeBlock(block.source, language = block.language)
        is DocumentBlock.Quote -> QuoteBlock(block)
        is DocumentBlock.BulletList -> ListBlock(block.items) { "•" }
        is DocumentBlock.OrderedList -> ListBlock(block.items) { index -> "${block.start + index}." }
        is DocumentBlock.Table -> TableBlock(block)
        DocumentBlock.Divider -> HorizontalDivider()
    }
}

@Composable
private fun QuoteBlock(quote: DocumentBlock.Quote) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.padding(BootstrapTokens.Spacing.three),
            verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.two),
        ) {
            quote.blocks.forEach { RenderBlock(it) }
        }
    }
}

@Composable
private fun ListBlock(
    items: List<List<DocumentBlock>>,
    marker: (Int) -> String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.two)) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.two),
            ) {
                Text(marker(index), style = MaterialTheme.typography.bodyLarge)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.one),
                ) {
                    item.forEach { RenderBlock(it) }
                }
            }
        }
    }
}

@Composable
private fun TableBlock(table: DocumentBlock.Table) {
    val columnCount = maxOf(table.header.size, table.rows.maxOfOrNull { it.size } ?: 0)
    if (columnCount == 0) return

    Column(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
    ) {
        TableRow(table.header, columnCount, header = true)
        HorizontalDivider()
        table.rows.forEachIndexed { index, row ->
            TableRow(row, columnCount, header = false)
            if (index != table.rows.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun TableRow(cells: List<List<InlineContent>>, columnCount: Int, header: Boolean) {
    Row(modifier = Modifier.widthIn(min = (columnCount * 160).dp)) {
        repeat(columnCount) { index ->
            val content = cells.getOrNull(index).orEmpty()
            RichText(
                content = content,
                style = if (header) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(BootstrapTokens.Spacing.two),
            )
        }
    }
}

@Composable
private fun RichText(
    content: List<InlineContent>,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceContainerHighest
    val value = remember(content, primary, codeBackground) {
        buildAnnotatedString {
            appendInline(content, primary, codeBackground)
        }
    }
    Text(text = value, modifier = modifier, style = style)
}

private fun AnnotatedString.Builder.appendInline(
    content: List<InlineContent>,
    linkColor: androidx.compose.ui.graphics.Color,
    codeBackground: androidx.compose.ui.graphics.Color,
) {
    content.forEach { inline ->
        when (inline) {
            is InlineContent.Text -> append(inline.value)
            is InlineContent.Strong -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInline(inline.children, linkColor, codeBackground)
            }
            is InlineContent.Emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInline(inline.children, linkColor, codeBackground)
            }
            is InlineContent.Code -> withStyle(
                SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground),
            ) { append(inline.value) }
            is InlineContent.Link -> {
                withLink(LinkAnnotation.Url(inline.destination)) {
                    withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                        appendInline(inline.label, linkColor, codeBackground)
                    }
                }
            }
            is InlineContent.Image -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append(if (inline.description.isBlank()) "[Image]" else "[${inline.description}]")
            }
            InlineContent.LineBreak -> append('\n')
        }
    }
}

@Composable
private fun headingStyle(level: Int): TextStyle = when (level) {
    1 -> MaterialTheme.typography.displaySmall
    2 -> MaterialTheme.typography.headlineLarge
    3 -> MaterialTheme.typography.headlineMedium
    4 -> MaterialTheme.typography.headlineSmall
    5 -> MaterialTheme.typography.titleLarge
    else -> MaterialTheme.typography.titleMedium
}
