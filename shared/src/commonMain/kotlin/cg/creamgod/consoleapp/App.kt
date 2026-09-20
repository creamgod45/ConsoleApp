package cg.creamgod.consoleapp

import ThemeMode
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.Page.*
import kotlinx.coroutines.launch
import kotlin.String

val md_theme_light_primary = Color(0xFF0B0C0A)
val md_theme_light_onPrimary = Color(0xFF26E543)
val md_theme_light_primaryContainer = Color(0xFF33D467)
val md_theme_dark_primary = Color(0xFFD4CDCD)
val md_theme_dark_onPrimary = Color(0xFF1CA531)
val md_theme_dark_primaryContainer = Color(0xFF1F823F)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    // ..
)
private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    // ..
)

@Composable
fun ReplyTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val colorScheme =
        if (useDarkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun listTexts(name: String) {
    Text(text = "Hello, $name!", modifier = Modifier.padding(1.dp, 8.dp))
}

@Composable
fun welcomePage(names: List<String> = listOf("Compose", "Kotlin", "Multiplatform", "Java"))
{
    Surface {
        Row {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = {
                    for (name in names) {
                        listTexts(name)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizableSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<String>,
    onResultClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    // Customization options
    placeholder: @Composable (() -> Unit) = {
        Text("Search something information")
    },
    leadingIcon: @Composable (() -> Unit)? = { Icon(Icons.Default.Search, contentDescription = "Search") },
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingContent: ((String) -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
) {
    // Track expanded state of search bar
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier
            .fillMaxSize()
            .semantics { isTraversalGroup = true }
    ) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            inputField = {
                // Customizable input field implementation
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = {
                        onSearch(query)
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            // Show search results in a lazy column for better performance
            LazyColumn {
                items(count = searchResults.size) { index ->
                    val resultText = searchResults[index]
                    ListItem(
                        headlineContent = {
                            Text(resultText)
                        },
                        supportingContent = supportingContent?.let { content ->
                            {
                                content(resultText)
                            }
                        },
                        leadingContent = leadingContent,
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .clickable {
                                onResultClick(resultText)
                                expanded = false
                            }
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun settingPage()
{
    var query by remember { mutableStateOf("") }

    val allItems = listOf("Compose", "Kotlin", "Multiplatform", "Java")

    val searchResults =
        if (query.isBlank()) {
            allItems
        } else {
            allItems.filter { item ->
                item.contains(query, ignoreCase = true)
            }
        }

    Surface {
        Row {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = {
                    CustomizableSearchBar(
                        query = query,
                        onQueryChange = { newQuery ->
                            query = newQuery
                        },
                        onSearch = {},
                        searchResults = searchResults,
                        onResultClick = { selectedItem ->
                            query = selectedItem
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun menuLayout(
    themeMode: ThemeMode,
    onThemeClick: () -> Unit,
    backCallback: () -> Unit = {},
    onGreetingClick: () -> Unit = {},
    onSettingClick: () -> Unit = {},
    ifPageEnablePage: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
){
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Column(content = {

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(
                            "ConsoleApp",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        OutlinedButton(
                            onClick = backCallback,
                            enabled = ifPageEnablePage,
                            content = {
                                Text("Back")
                            }
                        )
                    },
                    actions = {
                        IconButton(onClick = onThemeClick) {
                            Icon(
                                imageVector = if (themeMode == ThemeMode.Light) {
                                    Icons.Filled.Bedtime
                                } else {
                                    Icons.Filled.WbSunny
                                },
                                contentDescription = "Theme"
                            )
                        }
                        IconButton(onClick = onGreetingClick){
                            Icon(
                                imageVector = Icons.Filled.WavingHand,
                                contentDescription = "揮手"
                            )
                        }
                        IconButton(onClick = onSettingClick){
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "設定"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) {
            innerPadding -> Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                content()
            }
        }
    })

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ServiceSelector(
    onPageSelected: (Page) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf("----") }

    val services = listOf(
        Kotlin,
        Compose,
        Java,
        Multiplatform,
    )

    Box {
        ListItem(
            onClick = {
                expanded = true
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = true,
            overlineContent = {
                Text("select service")
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "下拉選單"
                )
            },
            supportingContent = {
                Text("目前選擇：$selectedService")
            },
            content = {
                Text("Welcome To ConsoleApp")
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            services.forEach { page ->
                DropdownMenuItem(
                    text = {
                        Text(page.name)
                    },
                    onClick = {
                        selectedService = page.name
                        expanded = false
                        onPageSelected(page)
                    }
                )
            }
        }
    }
    if (selectedService != "----" && expanded === false) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp, 0.dp, 0.dp, 0.dp), content = {
            Text(text = "Welcome To $selectedService")
            Text(text = "Why you choose $selectedService is better?")
            Text(text = "Because $selectedService is better than others")
        })
    }
}

@Composable
@Preview(showBackground = true)
fun App() {
    var currentPage by remember { mutableStateOf(Home) }
    var themeMode by remember { mutableStateOf(ThemeMode.System) }


    ReplyTheme(
        themeMode = themeMode
    ) {
        menuLayout(
            themeMode = themeMode,
            onThemeClick = {
                themeMode = when (themeMode) {
                    ThemeMode.System -> ThemeMode.Light
                    ThemeMode.Light -> ThemeMode.Dark
                    ThemeMode.Dark -> ThemeMode.System
                }
            },
            backCallback = {
                currentPage = Home
            },
            ifPageEnablePage = currentPage != Home,
            onGreetingClick = {
                currentPage = Greeting
            },
            onSettingClick = {
                currentPage = Setting
            },
        ) {
            when (currentPage) {
                Home -> {
                    Text(
                        text = "Welcome To ConsoleApp Home Page use nav menu go to page view product",
                        modifier = Modifier.padding(1.dp, 8.dp)
                    )
                    Column(modifier = Modifier.fillMaxSize(), content = {
                        ServiceSelector(
                            onPageSelected = { page ->
                                currentPage = page
                            }
                        )
                    })
                }
                Greeting -> {
                    welcomePage(
                    )
                }
                Setting -> {
                    settingPage(
                    )
                }
                Kotlin -> {
                    KotlinPage(
                        onPageSelected = { page ->
                            currentPage = page
                        }
                    )
                }
                Java -> {
                    Text(text = "Welcome To Java Page")
                }
                Multiplatform -> {
                    Text(text = "Welcome To Multiplatform Page")
                }
                Compose -> {
                    Text(text = "Welcome To Compose Page")
                }
                Kotlin_introduction -> {
                    KotlinIntroductionScreen(
                        onPageSelected = { page ->
                            currentPage = page
                        }
                    )
                }
                Kotlin_variable -> {
                    KotlinVariablePage(
                        onPageSelected = { page ->
                            currentPage = page
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KotlinPage(
    onPageSelected: (Page) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), content = {
        drawer(
            onPageSelected,
            content = {
                Text(text = "Welcome To Kotlin Page")
            }
        )
    })
}

@Composable
fun KotlinVariablePage(
    onPageSelected: (Page) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), content = {
        drawer(
            onPageSelected,
            content = {
                Text(text = "Welcome To Kotlin 變數 Page")
            }
        )
    })
}

@Composable
fun KotlinIntroductionScreen(
    onPageSelected: (Page) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), content = {
        drawer(
            onPageSelected,
            content = {
                Text(text = "Kotlin － 介紹")
            }
        )
    })
}

@Composable
fun drawer(
    onPageSelected: (Page) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
){
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    content = {
                        Spacer(Modifier.height(12.dp))
                        Text("選單", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                        HorizontalDivider()

                        Text(
                            "科林文章",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        NavigationDrawerItem(
                            label = { Text("首頁") },
                            selected = false,
                            onClick = { onPageSelected(Kotlin) }
                        )
                        NavigationDrawerItem(
                            label = { Text("介紹") },
                            selected = false,
                            onClick = { onPageSelected(Kotlin_introduction) }
                        )
                        NavigationDrawerItem(
                            label = { Text("變數") },
                            selected = false,
                            onClick = { onPageSelected(Kotlin_variable) }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                )
            }
        },
    ) {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { },
                    icon = { Icon(Icons.Filled.Menu, contentDescription = "Menu") },
                    modifier = Modifier.width(56.dp),
                    onClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    }
                )
            }
        ) {
            contentPadding -> Column(
                modifier = Modifier.padding(contentPadding)
            ) {
                content()
            }
        }
    }
}
