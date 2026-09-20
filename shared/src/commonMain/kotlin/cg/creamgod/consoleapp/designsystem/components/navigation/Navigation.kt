package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.designsystem.tokens.BootstrapTokens

data class BreadcrumbItem(
    val label: String,
    val onClick: (() -> Unit)? = null,
)

data class NavItem<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
    val icon: (@Composable () -> Unit)? = null,
)

@Composable
fun Breadcrumb(
    items: List<BreadcrumbItem>,
    modifier: Modifier = Modifier,
    separator: String = "/",
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.two),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            Text(
                text = item.label,
                modifier = if (item.onClick == null) Modifier else Modifier.clickable(onClick = item.onClick),
                color = if (item.onClick == null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.primary
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            if (index != items.lastIndex) {
                Text(separator, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun <T> Navbar(
    brand: String,
    items: List<NavItem<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Surface(modifier = modifier.fillMaxWidth(), tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(brand, style = MaterialTheme.typography.titleMedium)
            items.forEach { item ->
                Button(
                    text = item.label,
                    onClick = { onSelect(item.value) },
                    enabled = item.enabled,
                    variant = if (item.value == selected) Variant.Filled else Variant.Text,
                    leadingIcon = item.icon,
                )
            }
            actions?.invoke(this)
        }
    }
}

@Composable
fun <T> Tabs(
    items: List<NavItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = items.indexOfFirst { it.value == selected }.coerceAtLeast(0)
    PrimaryTabRow(selectedTabIndex = selectedIndex, modifier = modifier) {
        items.forEach { item ->
            Tab(
                selected = item.value == selected,
                onClick = { onSelect(item.value) },
                enabled = item.enabled,
                text = { Text(item.label) },
                icon = item.icon,
            )
        }
    }
}

@Composable
fun <T> Nav(
    items: List<NavItem<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
) {
    if (vertical) {
        Column(modifier = modifier) {
            for (item in items) {
                Button(
                    text = item.label,
                    onClick = { onSelect(item.value) },
                    enabled = item.enabled,
                    leadingIcon = item.icon,
                    variant = if (item.value == selected) Variant.Filled else Variant.Text,
                )
            }
        }
    } else {
        Row(modifier = modifier) {
            for (item in items) {
                Button(
                    text = item.label,
                    onClick = { onSelect(item.value) },
                    enabled = item.enabled,
                    leadingIcon = item.icon,
                    variant = if (item.value == selected) Variant.Filled else Variant.Text,
                )
            }
        }
    }
}

@Composable
fun Pagination(
    currentPage: Int,
    pageCount: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    siblingCount: Int = 2,
) {
    if (pageCount <= 0) return
    val safePage = currentPage.coerceIn(1, pageCount)
    val start = (safePage - siblingCount).coerceAtLeast(1)
    val end = (safePage + siblingCount).coerceAtMost(pageCount)

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(onClick = { onPageChange(safePage - 1) }, enabled = safePage > 1) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "上一頁")
        }
        for (page in start..end) {
            Button(
                text = page.toString(),
                onClick = { onPageChange(page) },
                variant = if (page == safePage) Variant.Filled else Variant.Text,
            )
        }
        IconButton(onClick = { onPageChange(safePage + 1) }, enabled = safePage < pageCount) {
            Icon(Icons.Default.ChevronRight, contentDescription = "下一頁")
        }
    }
}

@Composable
fun <T> ScrollSpy(
    items: List<NavItem<T>>,
    active: T,
    onNavigate: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier) {
        androidx.compose.foundation.layout.Column {
            items.forEach { item ->
                val selected = item.value == active
                Text(
                    text = item.label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = item.enabled) { onNavigate(item.value) }
                        .padding(BootstrapTokens.Spacing.two),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = if (selected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
                )
                HorizontalDivider()
            }
        }
    }
}
