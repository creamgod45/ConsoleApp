package cg.creamgod.consoleapp

import androidx.compose.animation.core.animate
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.Page.*
import cg.creamgod.consoleapp.designsystem.catalog.ComponentGuide
import cg.creamgod.consoleapp.designsystem.catalog.LearningGuideBook
import cg.creamgod.consoleapp.designsystem.catalog.examples.AccountPlan
import cg.creamgod.consoleapp.designsystem.catalog.examples.AccountSettings
import cg.creamgod.consoleapp.designsystem.catalog.examples.AccountSettingsForm
import cg.creamgod.consoleapp.designsystem.components.*
import cg.creamgod.consoleapp.designsystem.components.form.*
import cg.creamgod.consoleapp.designsystem.components.overlay.DsModal
import cg.creamgod.consoleapp.designsystem.content.MarkdownDocument
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleStep
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.*
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch
import kotlin.collections.listOf
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant

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
    if (selectedService != "----" && !expanded) {
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
    loadMails: suspend (MailQuery) -> List<MailMessage> = {
        throw UnsupportedOperationException("尚未設定郵件讀取服務")
    },
    filePicker: PlatformFilePicker = UnsupportedPlatformFilePicker,
    dialogHost: @Composable () -> Unit = {},
) {
    var currentPage by remember { mutableStateOf(Home) }
    var themeMode by remember { mutableStateOf(ThemeMode.System) }
    ReplyTheme(
        themeMode = themeMode
    ) {
        drawer(
            onPageSelected = { page -> currentPage = page },
            onThemeModeChanged = { mode -> themeMode = mode },
            currentPage = currentPage,
            themeMode = themeMode,
            content = {

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

                    GuideBook -> {
                        LearningGuideBook()
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

                    Markdown_Example -> {
                        Markdown_ExamplePage(
                            onPageSelected = { page ->
                                currentPage = page
                            }
                        )
                    }

                    ComponentGuide -> {
                        ComponentGuide(modifier = Modifier.fillMaxSize())
                    }

                    Mailer -> {
                        MailerPage(
                            loadMails = loadMails,
                            onPageSelected = { page ->
                                currentPage = page
                            }
                        )
                    }
                }
            }
        )
        dialogHost()
    }
}


private val timestampFormatter = LocalDateTime.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    day()
    char(' ')
    hour()
    char(':')
    minute()
    char(':')
    second()
}

fun Long.toReadableTimestamp(): String {
    val localDateTime = Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())

    return timestampFormatter.format(localDateTime)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MailerPage(
    loadMails: suspend (MailQuery) -> List<MailMessage>,
    onPageSelected: (Page) -> Unit,
) {
    val mails = remember { mutableStateListOf<MailPreview>() }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var reloadRequest by remember { mutableIntStateOf(0) }
    var selectedMail by remember { mutableStateOf<MailPreview?>(null) }
    var selectedMailIds by remember { mutableStateOf(emptySet<Int>()) }
    var filterWithMainBox by remember { mutableStateOf(true) }
    var filterWithAttachment by remember { mutableStateOf(false) }
    var filterWithRead by remember { mutableStateOf(MailFilter.All) }
    var filterWithDate by remember { mutableStateOf(MailFilterDate.All) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var dateRangeValue by remember { mutableStateOf<DateRangeValue?>(null) }
    val toastState = rememberToastState()
    val pageScope = rememberCoroutineScope()
    val mailQuery = remember(filterWithDate, dateRangeValue, reloadRequest) {
        buildMailQuery(
            filter = filterWithDate,
            rangeStartMillis = dateRangeValue?.startMillis,
            rangeEndMillis = dateRangeValue?.endMillis,
        )
    }

    LaunchedEffect(mailQuery) {
        loading = true
        loadError = null
        try {
            val response = loadMails(mailQuery)
            selectedMail = null
            selectedMailIds = emptySet()
            mails.clear()
            mails.addAll(
                response.map { mail ->
                    MailPreview(
                        id = mail.id,
                        sender = mail.name,
                        subject = mail.email,
                        preview = "來自 ${mail.email} 的模擬郵件",
                        time = mail.receivedAt.replace('T', ' '),
                        unread = true,
                        starred = false,
                        content = mail.content,
                        attachments = mail.attachments,
                    )
                },
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            loadError = error.message ?: "無法讀取郵件"
        } finally {
            loading = false
        }
    }

    fun updateMail(id: Int, transform: (MailPreview) -> MailPreview) {
        val index = mails.indexOfFirst { it.id == id }
        if (index >= 0) mails[index] = transform(mails[index])
    }

    fun notify(message: String) {
        pageScope.launch { toastState.show(message = message) }
    }

    fun removeWithUndo(mail: MailPreview, message: String) {
        val originalIndex = mails.indexOfFirst { it.id == mail.id }
        if (originalIndex < 0) return
        mails.removeAt(originalIndex)
        selectedMailIds = selectedMailIds - mail.id
        if (selectedMail?.id == mail.id) selectedMail = null

        pageScope.launch {
            val result = toastState.show(
                message = message,
                actionLabel = "復原",
                withDismissAction = true,
            )
            if (result == SnackbarResult.ActionPerformed && mails.none { it.id == mail.id }) {
                mails.add(originalIndex.coerceAtMost(mails.size), mail)
            }
        }
    }

    fun updateSelected(transform: (MailPreview) -> MailPreview, message: String) {
        val ids = selectedMailIds
        if (ids.isEmpty()) return
        mails.indices.forEach { index ->
            if (mails[index].id in ids) mails[index] = transform(mails[index])
        }
        selectedMailIds = emptySet()
        notify(message)
    }

    fun removeSelectedWithUndo(message: String) {
        val ids = selectedMailIds
        if (ids.isEmpty()) return
        val removed = mails.mapIndexedNotNull { index, mail ->
            if (mail.id in ids) index to mail else null
        }
        mails.removeAll { it.id in ids }
        selectedMailIds = emptySet()
        if (selectedMail?.id?.let { it in ids } == true) selectedMail = null

        pageScope.launch {
            val result = toastState.show(
                message = message,
                actionLabel = "復原",
                withDismissAction = true,
            )
            if (result == SnackbarResult.ActionPerformed) {
                removed.forEach { (originalIndex, mail) ->
                    if (mails.none { it.id == mail.id }) {
                        mails.add(originalIndex.coerceAtMost(mails.size), mail)
                    }
                }
            }
        }
    }

    val visibleMails = mails.filter { mail ->
        val matchesAttachment = !filterWithAttachment || mail.attachments.isNotEmpty()
        val matchesReadState = when (filterWithRead) {
            MailFilter.All -> true
            MailFilter.Unread -> mail.unread
            MailFilter.Starred -> mail.starred
        }
        matchesAttachment && matchesReadState
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val selectionState = when {
                    selectedMailIds.isEmpty() -> ToggleableState.Off
                    visibleMails.isNotEmpty() && visibleMails.all { it.id in selectedMailIds } -> ToggleableState.On
                    else -> ToggleableState.Indeterminate
                }
                TriStateCheckbox(
                    state = selectionState,
                    enabled = !loading && mails.isNotEmpty(),
                    onClick = {
                        selectedMailIds = if (selectionState == ToggleableState.On) {
                            emptySet()
                        } else {
                            visibleMails.mapTo(mutableSetOf()) { it.id }
                        }
                    },
                )
                Text(
                    text = when {
                        loading -> "正在讀取郵件…"
                        selectedMailIds.isNotEmpty() -> "已選取 ${selectedMailIds.size} 封"
                        else -> "收件匣（${visibleMails.size}）"
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(
                    onClick = { reloadRequest++ },
                    enabled = !loading,
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "重新讀取郵件")
                }
            }

            if (selectedMailIds.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = {
                        val count = selectedMailIds.size
                        updateSelected({ it.copy(unread = false) }, "已將 $count 封郵件標示為已讀")
                    }) {
                        Icon(Icons.Default.MarkEmailRead, contentDescription = "批次標示為已讀")
                    }
                    IconButton(onClick = {
                        val count = selectedMailIds.size
                        updateSelected({ it.copy(starred = true) }, "已為 $count 封郵件加上星號")
                    }) {
                        Icon(Icons.Default.Star, contentDescription = "批次加上星號")
                    }
                    IconButton(onClick = {
                        val count = selectedMailIds.size
                        removeSelectedWithUndo("已封存 $count 封郵件")
                    }) {
                        Icon(Icons.Default.Archive, contentDescription = "批次封存")
                    }
                    IconButton(onClick = {
                        val count = selectedMailIds.size
                        removeSelectedWithUndo("已刪除 $count 封郵件")
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "批次刪除")
                    }
                    IconButton(onClick = { selectedMailIds = emptySet() }) {
                        Icon(Icons.Default.Close, contentDescription = "清除選取")
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    label = "收件匣",
                    selected = filterWithMainBox,
                    onSelectedChange = {
                        v -> filterWithMainBox = v
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Mail, contentDescription = "收件匣")
                    }
                )
                FilterChip(
                    label = "附件",
                    selected = filterWithAttachment,
                    onSelectedChange = {
                        v -> filterWithAttachment = v
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.AttachFile, contentDescription = "附件")
                    }
                )

                DropdownChip(
                    value = filterWithRead,
                    //modifier = Modifier.widthIn(min = 112.dp),
                    options = listOf(
                        FormOption(MailFilter.All, "全部郵件"),
                        FormOption(MailFilter.Unread, "未讀郵件"),
                        FormOption(MailFilter.Starred, "星號郵件"),
                    ),
                    onValueChange = { filterWithRead = it },
                    label = {
                        when (it) {
                            MailFilter.All -> "全部郵件"
                            MailFilter.Unread -> "未讀郵件"
                            MailFilter.Starred -> "星號郵件"
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (filterWithRead === MailFilter.All) {
                                Icons.Filled.AllInbox
                            } else if (filterWithRead === MailFilter.Unread) {
                                Icons.Filled.MarkAsUnread
                            } else {
                                Icons.Filled.MarkEmailRead
                            },
                            contentDescription = "狀態"
                        )
                    }
                )

                DropdownChip(
                    value = filterWithDate,
                    options = listOf(
                        FormOption(MailFilterDate.All, "全部"),
                        FormOption(MailFilterDate.Spec, "特定日期"),
                        FormOption(MailFilterDate.Today, "今天"),
                        FormOption(MailFilterDate.Yesterday, "昨天"),
                    ),
                    onValueChange = { selected ->
                        filterWithDate = selected

                        if (selected == MailFilterDate.Spec) {
                            showDateRangePicker = true
                        } else {
                            dateRangeValue = null
                        }
                    },
                    label = { selected ->
                        when (selected) {
                            MailFilterDate.All -> "全部"
                            MailFilterDate.Spec -> {
                                if (dateRangeValue == null) {
                                    "特定日期"
                                } else {
                                    "自訂範圍"
                                }
                            }
                            MailFilterDate.Today -> "今天"
                            MailFilterDate.Yesterday -> "昨天"
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "時間"
                        )
                    }

                )
                val pickerNow = remember(showDateRangePicker) { Clock.System.now() }
                val pickerToday = pickerNow
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                val pickerTodayMillis = pickerToday
                    .atStartOfDayIn(TimeZone.UTC)
                    .toEpochMilliseconds()
                val pickerYear = pickerToday.year
                val selectableMailDates = remember(pickerTodayMillis, pickerYear) {
                    object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                            utcTimeMillis <= pickerTodayMillis

                        override fun isSelectableYear(year: Int): Boolean = year == pickerYear
                    }
                }
                DateRangePicker(
                    visible = showDateRangePicker,
                    value = dateRangeValue,
                    onValueChange = { selectedRange ->
                        dateRangeValue = selectedRange
                        filterWithDate = MailFilterDate.Spec
                    },
                    onDismiss = {
                        showDateRangePicker = false
                        if (filterWithDate == MailFilterDate.Spec && dateRangeValue == null) {
                            filterWithDate = MailFilterDate.All
                        }
                    },
                    title = "選擇郵件日期範圍",
                    yearRange = pickerYear..pickerYear,
                    selectableDates = selectableMailDates,
                )

                dateRangeValue?.let { range ->
                    val startMillis = range.startMillis.toReadableTimestamp()
                    val endMillis = range.endMillis.toReadableTimestamp()


                    FilterChip(
                        label = "$startMillis ~ $endMillis",
                        selected = filterWithDate == MailFilterDate.Spec,
                        onSelectedChange = { showDateRangePicker = true },
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    loading -> Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LoadingIndicator()
                        Text("正在連線")
                    }

                    loadError != null -> Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Alert(
                            title = "郵件載入失敗",
                            message = loadError.orEmpty(),
                            tone = Tone.Danger,
                        )
                        Button("重試", onClick = { reloadRequest++ })
                    }

                    visibleMails.isEmpty() -> Text(
                        text = "目前沒有郵件",
                        modifier = Modifier.align(Alignment.Center),
                    )

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(items = visibleMails, key = { it.id }) { mail ->
                            SwipeMailItem(
                                mail = mail,
                                selected = mail.id in selectedMailIds,
                                onSelectedChange = { selected ->
                                    selectedMailIds = if (selected) {
                                        selectedMailIds + mail.id
                                    } else {
                                        selectedMailIds - mail.id
                                    }
                                },
                                onToggleRead = {
                                    val willBeUnread = !mail.unread
                                    updateMail(mail.id) { current -> current.copy(unread = !current.unread) }
                                    notify(if (willBeUnread) "已標示為未讀" else "已標示為已讀")
                                },
                                onToggleStar = {
                                    val willBeStarred = !mail.starred
                                    updateMail(mail.id) { current -> current.copy(starred = !current.starred) }
                                    notify(if (willBeStarred) "已加上星號" else "已取消星號")
                                },
                                onArchive = { removeWithUndo(mail, "郵件已封存") },
                                onDelete = { removeWithUndo(mail, "郵件已刪除") },
                                onOpen = {
                                    val openedMail = mail.copy(unread = false)
                                    selectedMail = openedMail
                                    updateMail(mail.id) { openedMail }
                                },
                            )
                        }
                    }
                }
            }
        }
        ToastHost(
            state = toastState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
    selectedMail?.let { mail ->
        DsModal(
            visible = true,
            onDismissRequest = { selectedMail = null },
            title = mail.subject,
            modifier = Modifier.verticalScroll(rememberScrollState()),
            footer = {
                IconButton(onClick = {
                    val updated = mail.copy(starred = !mail.starred)
                    updateMail(mail.id) { updated }
                    selectedMail = updated
                    notify(if (updated.starred) "已加上星號" else "已取消星號")
                }) {
                    Icon(
                        imageVector = if (mail.starred) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (mail.starred) "取消星號" else "加上星號",
                    )
                }
                IconButton(onClick = {
                    removeWithUndo(mail, "郵件已封存")
                }) {
                    Icon(imageVector = Icons.Filled.Archive, contentDescription = "封存郵件")
                }
                IconButton(onClick = {
                    removeWithUndo(mail, "郵件已刪除")
                }) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "刪除郵件")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button("關閉", onClick = { selectedMail = null })
            },
        ) {
            Text(mail.sender, style = MaterialTheme.typography.titleMedium)
            Text(mail.subject, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("收件時間：${mail.time}", style = MaterialTheme.typography.labelMedium)
            HorizontalDivider()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 480.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = mail.content,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            if (mail.attachments.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "附件（${mail.attachments.size}）",
                    style = MaterialTheme.typography.titleMedium,
                )
                val accordionItems = remember(mail.attachments) {
                    mail.attachments.map { attachment ->
                        AccordionItem(
                            initiallyExpanded = false,
                            title = attachment.filename,
                            content = {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "",
                                    subtitle = "${attachment.contentType} · ${attachment.sizeBytes.toReadableFileSize()}",
                                ) {
                                    Text(
                                        text = attachment.content.ifBlank { "此附件沒有可預覽的文字內容" },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 160.dp)
                                            .verticalScroll(rememberScrollState()),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            },
                        )
                    }
                }
                Accordion(
                    items = accordionItems,
                    allowMultiple = true,
                )
            }
        }
    }
}

@Composable
private fun SwipeMailItem(
    mail: MailPreview,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onOpen: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val actionWidthPx = with(density) { (88.dp * 2).toPx() }
    var horizontalOffset by remember(mail.id) { mutableFloatStateOf(0f) }

    suspend fun animateOffsetTo(target: Float) {
        animate(
            initialValue = horizontalOffset,
            targetValue = target,
        ) { value, _ ->
            horizontalOffset = value
        }
    }

    fun closeThen(action: () -> Unit) {
        scope.launch {
            animateOffsetTo(0f)
            action()
        }
    }

    val dragState = rememberDraggableState { delta ->
        horizontalOffset = (horizontalOffset + delta).coerceIn(-actionWidthPx, actionWidthPx)
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.Start,
        ) {
            MailSwipeAction(
                label = if (mail.unread) "已讀" else "未讀",
                icon = if (mail.unread) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { closeThen(onToggleRead) },
            )
            MailSwipeAction(
                label = if (mail.starred) "取消星號" else "星號",
                icon = if (mail.starred) Icons.Default.StarBorder else Icons.Default.Star,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = { closeThen(onToggleStar) },
            )
        }
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.End,
        ) {
            MailSwipeAction(
                label = "封存",
                icon = Icons.Default.Archive,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { closeThen(onArchive) },
            )
            MailSwipeAction(
                label = "刪除",
                icon = Icons.Default.Delete,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                onClick = { closeThen(onDelete) },
            )
        }

        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(horizontalOffset.roundToInt(), 0) }
                .clickable(onClick = onOpen)
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        val revealThreshold = actionWidthPx * 0.45f
                        val flingThreshold = 1_200f
                        val target = when {
                            velocity >= flingThreshold -> actionWidthPx
                            velocity <= -flingThreshold -> -actionWidthPx
                            horizontalOffset >= revealThreshold -> actionWidthPx
                            horizontalOffset <= -revealThreshold -> -actionWidthPx
                            else -> 0f
                        }
                        scope.launch { animateOffsetTo(target) }
                    },
                ),
            colors = ListItemDefaults.colors(
                containerColor = if (mail.unread) {
                    MaterialTheme.colorScheme.surfaceContainerLow
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ),
            leadingContent = {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = onSelectedChange,
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = { onSelectedChange(true) },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(mail.sender.take(1), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            },
            headlineContent = {
                Text(
                    text = mail.sender,
                    fontWeight = if (mail.unread) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Column {
                    Text(
                        text = mail.subject,
                        fontWeight = if (mail.unread) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = mail.preview,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(4.dp)) {

                        if (mail.attachments.isNotEmpty()) {
                            mail.attachments.forEach { attachment ->
                                AssistChip(
                                    label = attachment.filename,
                                    onClick = {  },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Filled.AttachFile, contentDescription = "附件檔案")
                                    }
                                )
                            }
                        }
                    }
                }
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(mail.time, style = MaterialTheme.typography.labelSmall)
                    if (mail.attachments.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "${mail.attachments.size} 個附件",
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = mail.attachments.size.toString(),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    Icon(
                        imageVector = if (mail.starred) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (mail.starred) "已加星號" else "未加星號",
                        tint = if (mail.starred) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        )
    }
    HorizontalDivider()
}

private fun Long.toReadableFileSize(): String = when {
    this >= 1_048_576 -> "${toSingleDecimal(1_048_576)} MB"
    this >= 1_024 -> "${toSingleDecimal(1_024)} KB"
    else -> "$this B"
}

private fun Long.toSingleDecimal(divisor: Long): String {
    val tenths = this * 10 / divisor
    return "${tenths / 10}.${tenths % 10}"
}

@Composable
private fun MailSwipeAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxHeight().width(88.dp),
        color = containerColor,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
fun Markdown_ExamplePage(onPageSelected: (Page) -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), content = {
        MarkdownDocument(
            """
            # Release notes

            - Added **semantic typography**
            - Added [clickable links](https://example.com)

            | Feature | Status |
            | --- | --- |
            | Markdown | Ready |
            """.trimIndent(),
        )
    })
}

@Composable
fun KotlinPage(
    onPageSelected: (Page) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), content = {
        Text(text = "Welcome To Kotlin Page")
    })
}

@Composable
fun KotlinVariablePage(
    onPageSelected: (Page) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), content = {
        Text(text = "Welcome To Kotlin 變數 Page")
        var accountSettings by remember { mutableStateOf(AccountSettings(
            displayName = "niggar",
            email = "test@test.com",
            plan = AccountPlan.Free,
            productUpdates = true,
            warningThreshold = 0.4f
        )) }
        AccountSettingsForm(
            initialValue = accountSettings,
            onSave = { settings ->
                accountSettings = settings
                Result.success(Unit)
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
    })
}

@Composable
fun drawer(
    themeMode: ThemeMode,
    currentPage: Page,
    onPageSelected: (Page) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = {
                        Spacer(Modifier.height(12.dp))
                        Text("選單", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                        HorizontalDivider()

                        NavigationDrawerItem(
                            label = { Text("學習指南") },
                            selected = currentPage == GuideBook,
                            onClick = { onPageSelected(GuideBook) },
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                )
                            },
                        )

                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Book,
                                contentDescription = "框架維基",
                                modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp).size(22.dp)
                            )
                            Text(
                                "Kotlin 框架維基",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        HorizontalDivider()
                        NavigationDrawerItem(
                            label = { Text("首頁") },
                            selected = currentPage == Kotlin,
                            onClick = { onPageSelected(Kotlin) },
                            icon = {
                                Icon(imageVector = Icons.Filled.Home, contentDescription = "Kotlin 首頁")
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text("介紹") },
                            selected = currentPage == Kotlin_introduction,
                            onClick = { onPageSelected(Kotlin_introduction) },
                            icon = {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Article, contentDescription = "Kotlin 介紹")
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text("變數") },
                            selected = currentPage == Kotlin_variable,
                            onClick = { onPageSelected(Kotlin_variable) },
                            icon = {
                                Icon(imageVector = Icons.Filled.Numbers, contentDescription = "Kotlin 變數")
                            }
                        )
                        HorizontalDivider()

                        NavigationDrawerItem(
                            label = { Text(text = "元件教學") },
                            selected = currentPage == ComponentGuide,
                            onClick = {
                                onPageSelected(ComponentGuide)
                            },
                            icon = {
                                Icon(imageVector = Icons.Filled.PanTool, contentDescription = "元件教學")
                            }
                        )
                        HorizontalDivider()
                        NavigationDrawerItem(
                            label = {
                                Text(text = "Markdown 元件範例")
                            },
                            selected = currentPage == Markdown_Example,
                            onClick = {
                                onPageSelected(Markdown_Example)
                            },
                            icon = {
                                Icon(imageVector = Icons.Filled.EmojiSymbols, contentDescription = "Markdown")
                            }
                        )
                        HorizontalDivider()
                        NavigationDrawerItem(
                            label = {
                                Text(text = "信箱")
                            },
                            selected = currentPage == Mailer,
                            onClick = {
                                onPageSelected(Mailer)
                            },
                            icon = {
                                Icon(imageVector = Icons.Filled.Mail, contentDescription = "Mail")
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                )
            }
        },
    ) {
        Scaffold(
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
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val nextMode = when (themeMode) {
                                    ThemeMode.System -> ThemeMode.Light
                                    ThemeMode.Light -> ThemeMode.Dark
                                    ThemeMode.Dark -> ThemeMode.System
                                }
                                onThemeModeChanged(nextMode)
                            }
                        ) {
                            Icon(
                                imageVector = if (themeMode == ThemeMode.Light) {
                                    Icons.Filled.Bedtime
                                } else {
                                    Icons.Filled.WbSunny
                                },
                                contentDescription = "Theme",
                            )
                        }
                        IconButton(onClick = { onPageSelected(Greeting) }) {
                            Icon(
                                imageVector = Icons.Filled.WavingHand,
                                contentDescription = "揮手",
                            )
                        }
                        IconButton(onClick = { onPageSelected(Setting) }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "設定",
                            )
                        }
                    },
                )
            },
        ) {
            contentPadding -> Column(
                modifier = Modifier.padding(contentPadding)
            ) {
                content()
            }
        }
    }
}
