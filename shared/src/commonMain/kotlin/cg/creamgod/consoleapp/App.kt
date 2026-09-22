package cg.creamgod.consoleapp

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
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
import cg.creamgod.consoleapp.designsystem.components.*
import cg.creamgod.consoleapp.designsystem.components.form.*
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleStep
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

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
fun App(
    submitQuestion: suspend (String, PickedFile) -> Unit = { _, _ ->
        throw UnsupportedOperationException("尚未設定表單送出服務")
    },
    filePicker: PlatformFilePicker = UnsupportedPlatformFilePicker,
    dialogHost: @Composable () -> Unit = {},
) {
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
                        submitQuestion = submitQuestion,
                        filePicker = filePicker,
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
        dialogHost()
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
    submitQuestion: suspend (String, PickedFile) -> Unit,
    filePicker: PlatformFilePicker,
    onPageSelected: (Page) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), content = {
        drawer(
            onPageSelected,
            content = {
                var expanded by remember { mutableStateOf(false) }
                var confirmed by remember { mutableStateOf(false) }
                var showAsk by remember { mutableStateOf(false) }
                var answer by remember { mutableStateOf("真的") }
                var showChoice by remember { mutableStateOf(false) }
                var userChoice: Comparable<*> by remember { mutableStateOf(0) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = {
                        Text(text = "Kotlin － 介紹", modifier = Modifier.padding(0.dp, 8.dp), style = MaterialTheme.typography.titleLarge)
                        Alert(message = "開始之前先下載這個專案到本地安裝與初始化，體驗快速開發與敏捷開發的邏輯過程，享受成熟的元件設計帶來的便利", modifier = Modifier.padding(0.dp, 8.dp), tone = Tone.Primary, dismissible = true)
                        Card(modifier = Modifier.padding(0.dp, 8.dp)) {
                            ArticleStep(1, "了解一個系統性結構最快的學習的路徑，就是拆解現有的成品模型。") {
                                Text(text = "顯示邏輯結構並且用了解屬性設定，還有設計邏輯與原理，為了做到什麼以及加速什麼的進程")
                                Text(text = "每個框架的設計都有合理的原因與理由，多數是框架過於原始與粗曠沒有業務邏輯的撰寫邏輯，導致所有的框架都是重新造輪子，這只會導致開發維護複雜度與維護難度")
                            }

                        }
                        Callout(
                            title = "問題 1",
                            message = "請問是否需要學習這個框架的進程呢？",
                            modifier = Modifier.padding(vertical = 8.dp),
                            tone = Tone.Warning,
                            content = {
                                Button(
                                    text = if (expanded) "收合答案" else "查看答案",
                                    onClick = { expanded = !expanded },
                                    tone = Tone.Light,
                                )

                                Collapse(
                                    expanded = expanded,
                                    modifier = Modifier.padding(vertical = 8.dp).background(Color(0xFF3ECDBB)),
                                ) {
                                    Text(
                                        modifier = Modifier.padding(16.dp),
                                        text = "答案是也不是，因為框架提供的是敏捷開發的成熟可以立刻使用的語法，而非重複造輪子的過程。",
                                    )
                                }
                            }
                        )
                        IconButton(onClick = { showAsk = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = "Help"
                            )
                        }
                        Ask(
                            visible = showAsk,
                            value = answer,
                            onValueChange = { newValue ->
                                answer = newValue
                            },
                            onConfirm = { submittedValue ->
                                answer = submittedValue
                                confirmed = true
                                showAsk = false
                            },
                            onDismiss = {
                                showAsk = false
                            },
                            title = "你真的懂了嗎？",
                            message = "請輸入你的回答。",
                            label = "回答",
                        )

                        if (confirmed) {
                            Text("你真的懂了嗎？ 使用者回答：$answer")
                        }
                        Button("顯示問題", onClick = { showChoice = true })
                        Choice(
                            visible = showChoice,
                            value = userChoice,
                            options = listOf(
                                FormOption(1, "堅強哥", true),
                                FormOption(2, "尖刺哥", true),
                                FormOption(3, "我受不了了", true),
                            ),
                            onValueChange = { submittedValue ->
                                userChoice = submittedValue
                                showChoice = false
                            },
                            onConfirm = { value ->
                                userChoice = value
                                showChoice = false
                            },
                            onDismiss = {
                                showChoice = false
                            },
                            title = "我的狀態"
                        )
                        var collectInput by remember { mutableStateOf("") }
                        var selectedFiles by remember {
                            mutableStateOf(emptyList<PickedFile>())
                        }

                        val scope = rememberCoroutineScope()
                        FormSection(
                            title = "表單",
                            content = {
                                FormFilePicker(
                                    fileNames = selectedFiles.map { it.name },
                                    onPick = {
                                        scope.launch {
                                            val picked = filePicker.pickFiles(
                                                FilePickerRequest(
                                                    title = "選擇附件",
                                                    allowMultiple = false,
                                                    presentation = FilePickerPresentation.Dialog,
                                                    allowedExtensions = setOf("pdf", "jpg", "png"),
                                                ),
                                            )
                                            if (picked.isNotEmpty()) selectedFiles = picked
                                        }
                                    },
                                    onClear = {
                                        selectedFiles = emptyList()
                                    },
                                    label = "選擇檔案",
                                    helperText = "請選擇要上傳的檔案。",
                                )

                                FormTextInput(
                                    value = collectInput,
                                    onValueChange = { value -> collectInput = value },
                                    label = "甚麼問題"
                                )
                                var multiSelectValue by remember { mutableStateOf(setOf<Int>()) }
                                FormMultiSelect(
                                    values = multiSelectValue,
                                    options = listOf(
                                        FormOption(1, "Kotlin", true),
                                        FormOption(2, "Compose", true),
                                        FormOption(3, "Java", true),
                                        FormOption(4, "Multiplatform", true),
                                    ),
                                    onValuesChange = { values ->
                                        multiSelectValue = values
                                    },
                                    label = "喜歡的語言與框架"
                                )
                                var passwordValue by remember { mutableStateOf("") }
                                FormPasswordInput(
                                    value = passwordValue,
                                    onValueChange = {
                                        ps -> passwordValue = ps
                                    },
                                    label = "秘密欄位"
                                )
                                var radioValue by remember { mutableStateOf(1) }
                                FormRadioGroup(
                                    value = radioValue,
                                    options = listOf(
                                        FormOption(1, "答案1"),
                                        FormOption(2, "答案2"),
                                        FormOption(3, "答案3"),
                                        FormOption(4, "答案4"),
                                    ),
                                    onValueChange = {
                                        v -> radioValue = v
                                    },
                                    label = "單選選項"
                                )

                                FormSupportingText("請填選答案收集資料用途")
                                var textAreaValue by remember { mutableStateOf("") }
                                FormTextArea(
                                    value = textAreaValue,
                                    onValueChange = {
                                        v -> textAreaValue = v
                                    },
                                    label = "文字範圍"
                                )
                                var rangeValue by remember { mutableStateOf(0f) }
                                FormRange(
                                    value = rangeValue,
                                    onValueChange = {
                                        v -> rangeValue = v
                                    },
                                    label = "範圍",
                                    valueRange = 0f..100f,
                                )

                                var switchValue by remember { mutableStateOf(false) }
                                FormSwitch(
                                    checked = switchValue,
                                    onCheckedChange = {
                                        v -> switchValue = v
                                    },
                                    label = "切換"
                                )

                                var checkboxValue by remember { mutableStateOf(false) }
                                FormCheckbox(
                                    checked = checkboxValue,
                                    onCheckedChange = {
                                        v -> checkboxValue = v
                                    },
                                    label = "數值"
                                )

                                var seleterValue by remember { mutableStateOf(1) }
                                FormSelect(
                                    value = seleterValue,
                                    options = listOf(
                                        FormOption(1, "選項 1"),
                                        FormOption(2, "選項 2"),
                                        FormOption(3, "選項 3"),
                                        FormOption(4, "選項 4"),
                                    ),
                                    onValueChange = { v -> seleterValue = v},
                                    label = "選項"
                                )
                                var filterChipValue by remember { mutableStateOf(false) }
                                var filterChipValue2 by remember { mutableStateOf(false) }
                                var filterChipValue3 by remember { mutableStateOf(false) }
                                Row(modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally), horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                                    FilterChip(
                                        selected = filterChipValue,
                                        onClick = {
                                            filterChipValue = !filterChipValue
                                        },
                                        label = { Text(text = "Chip 1") }
                                    )
                                    FilterChip(
                                        selected = filterChipValue2,
                                        onClick = {
                                            filterChipValue2 = !filterChipValue2
                                        },
                                        label = { Text(text = "Chip 2") }
                                    )
                                    InputChip(
                                        selected = filterChipValue3,
                                        onClick = {
                                            filterChipValue3 = !filterChipValue3
                                        },
                                        label = { Text(text = "Chip 3") }
                                    )
                                }

                                var submitting by remember { mutableStateOf(false) }
                                var submitMessage by remember { mutableStateOf<String?>(null) }
                                val scope = rememberCoroutineScope()

                                FormActions(
                                    onSubmit = {
                                        val file = selectedFiles.firstOrNull()
                                        if (file != null && !submitting) {
                                            scope.launch {
                                                submitting = true
                                                submitMessage = null
                                                try {
                                                    submitQuestion(collectInput.trim(), file)
                                                    submitMessage = "送出成功"
                                                    collectInput = ""
                                                    selectedFiles = emptyList()
                                                } catch (e: CancellationException) {
                                                    throw e
                                                } catch (e: Exception) {
                                                    submitMessage = "送出失敗：${e.message ?: "請稍後再試"}"
                                                } finally {
                                                    submitting = false
                                                }
                                            }
                                        }
                                    },
                                    submitText = if (submitting) "送出中…" else "送出",
                                    submitEnabled = !submitting &&
                                            collectInput.isNotBlank() &&
                                            selectedFiles.isNotEmpty(),
                                )
                                submitMessage?.let { Text(it) }
                            }
                        )
                    }
                )
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
