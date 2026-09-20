package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.designsystem.tokens.BootstrapTokens

data class AccordionItem(
    val title: String,
    val initiallyExpanded: Boolean = false,
    val enabled: Boolean = true,
    val content: @Composable () -> Unit,
)

@Composable
fun Collapse(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(visible = expanded, modifier = modifier) {
        content()
    }
}

@Composable
fun Accordion(
    items: List<AccordionItem>,
    modifier: Modifier = Modifier,
    allowMultiple: Boolean = false,
    flush: Boolean = false,
) {
    var expanded by remember(items) {
        mutableStateOf(items.mapIndexedNotNull { index, item -> index.takeIf { item.initiallyExpanded } }.toSet())
    }

    Surface(
        modifier = modifier,
        shape = if (flush) RectangleShape else MaterialTheme.shapes.medium,
        border = if (flush) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            items.forEachIndexed { index, item ->
                val isExpanded = index in expanded
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = item.enabled) {
                            expanded = when {
                                isExpanded -> expanded - index
                                allowMultiple -> expanded + index
                                else -> setOf(index)
                            }
                        }
                        .padding(BootstrapTokens.Spacing.three),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(item.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收合" else "展開",
                    )
                }
                Collapse(expanded = isExpanded) {
                    Column(modifier = Modifier.padding(BootstrapTokens.Spacing.three)) {
                        item.content()
                    }
                }
                if (index != items.lastIndex) HorizontalDivider()
            }
        }
    }
}
