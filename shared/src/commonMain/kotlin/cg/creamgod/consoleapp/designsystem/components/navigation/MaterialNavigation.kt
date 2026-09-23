package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomAppBar as MaterialBottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar as MaterialNavigationBar
import androidx.compose.material3.NavigationBarItem as MaterialNavigationBarItem
import androidx.compose.material3.NavigationDrawerItem as MaterialNavigationDrawerItem
import androidx.compose.material3.NavigationRail as MaterialNavigationRail
import androidx.compose.material3.NavigationRailItem as MaterialNavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar as MaterialTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun <T> NavigationBar(
    items: List<NavItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    MaterialNavigationBar(modifier = modifier) {
        items.forEach { item ->
            MaterialNavigationBarItem(
                selected = item.value == selected,
                onClick = { onSelect(item.value) },
                enabled = item.enabled,
                icon = item.icon ?: {},
                label = { Text(item.label) },
            )
        }
    }
}

@Composable
fun <T> NavigationRail(
    items: List<NavItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    MaterialNavigationRail(modifier = modifier) {
        header?.invoke()
        items.forEach { item ->
            MaterialNavigationRailItem(
                selected = item.value == selected,
                onClick = { onSelect(item.value) },
                enabled = item.enabled,
                icon = item.icon ?: {},
                label = { Text(item.label) },
            )
        }
    }
}

/** Persistent navigation drawer intended for wide desktop/tablet layouts. */
@Composable
fun <T> NavigationDrawer(
    items: List<NavItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier.widthIn(min = 240.dp, max = 320.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    header?.invoke()
                    items.forEach { item ->
                        MaterialNavigationDrawerItem(
                            label = { Text(item.label) },
                            selected = item.value == selected,
                            onClick = { if (item.enabled) onSelect(item.value) },
                            modifier = Modifier
                                .alpha(if (item.enabled) 1f else 0.38f)
                                .semantics { if (!item.enabled) disabled() },
                            icon = item.icon,
                        )
                    }
                }
            }
        },
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    MaterialTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions,
    )
}

@Composable
fun BottomAppBar(
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit,
) {
    MaterialBottomAppBar(modifier = modifier, actions = actions)
}

/** Compact action surface for editor, list, and detail-page commands. */
@Composable
fun Toolbar(
    modifier: Modifier = Modifier,
    title: String? = null,
    navigation: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit,
) {
    Surface(modifier = modifier.fillMaxWidth(), tonalElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigation?.invoke()
            title?.let {
                Text(it, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            }
            actions()
        }
    }
}
