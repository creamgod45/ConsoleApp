package cg.creamgod.consoleapp.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.designsystem.components.Alert
import cg.creamgod.consoleapp.designsystem.components.Badge
import cg.creamgod.consoleapp.designsystem.components.Button
import cg.creamgod.consoleapp.designsystem.components.Card
import cg.creamgod.consoleapp.designsystem.components.NavItem
import cg.creamgod.consoleapp.designsystem.components.Navbar
import cg.creamgod.consoleapp.designsystem.components.Progress
import cg.creamgod.consoleapp.designsystem.components.Tone
import cg.creamgod.consoleapp.designsystem.components.Variant
import cg.creamgod.consoleapp.designsystem.components.form.FormCheckbox
import cg.creamgod.consoleapp.designsystem.components.form.FormPasswordInput
import cg.creamgod.consoleapp.designsystem.components.form.FormSwitch
import cg.creamgod.consoleapp.designsystem.components.form.FormTextArea
import cg.creamgod.consoleapp.designsystem.components.form.FormTextInput
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleCallout
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleLink
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleMeta
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleSection
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleStep
import cg.creamgod.consoleapp.designsystem.patterns.article.ArticleType
import cg.creamgod.consoleapp.designsystem.patterns.article.CodeBlock
import cg.creamgod.consoleapp.designsystem.patterns.article.GuideArticle

/**
 * In-app learning path for the ConsoleApp Design System.
 *
 * The guide intentionally uses the public component API that it teaches. This keeps examples
 * executable and turns the guide itself into a small reference implementation.
 */
@Composable
fun LearningGuideBook(modifier: Modifier = Modifier) {
    var chapter by remember { mutableStateOf(LearningChapter.Roadmap) }
    var componentPage by remember { mutableStateOf<ComponentLesson?>(null) }
    val chapters = LearningChapter.entries
    val chapterIndex = chapters.indexOf(chapter)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "ConsoleApp Framework Guide Book",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "從 Compose 基礎、受控狀態到跨平台架構，循序完成一個可驗證的功能畫面。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )

        Progress(
            value = (chapterIndex + 1f) / chapters.size,
            tone = Tone.Success,
        )
        Text(
            text = "第 ${chapterIndex + 1} / ${chapters.size} 章　${chapter.label}",
            style = MaterialTheme.typography.labelLarge,
        )

        Navbar(
            brand = "學習路徑",
            items = chapters.map { NavItem(it, it.shortLabel) },
            selected = chapter,
            onSelect = {
                chapter = it
                componentPage = null
            },
        )

        val selectedComponent = componentPage
        if (selectedComponent != null) {
            ComponentLessonPage(
                lesson = selectedComponent,
                onBack = { componentPage = null },
            )
        } else {
            when (chapter) {
                LearningChapter.Roadmap -> RoadmapChapter(onOpen = { chapter = it })
                LearningChapter.BookPlanning -> BookPlanningChapter()
                LearningChapter.Foundation -> FoundationChapter()
                LearningChapter.Compose -> ComposeChapter()
                LearningChapter.Shared -> SharedChapter()
                LearningChapter.Web -> WebMainChapter()
                LearningChapter.Desktop -> DesktopAppChapter()
                LearningChapter.FirstScreen -> FirstScreenChapter()
                LearningChapter.State -> StateChapter()
                LearningChapter.Components -> ComponentIndex(onSelect = { componentPage = it })
                LearningChapter.Architecture -> ArchitectureChapter()
                LearningChapter.Capstone -> CapstoneChapter()
            }
        }

        if (componentPage == null) {
            ChapterNavigation(
                chapter = chapter,
                onSelect = { chapter = it },
            )
        }
    }
}

@Composable
private fun RoadmapChapter(onOpen: (LearningChapter) -> Unit) {
    GuideArticle(
        meta = ArticleMeta(
            title = "先看地圖，再開始寫程式",
            summary = "這條路徑以完成作品為目標。每一階段都有可觀察成果，不要求先背完整套 API。",
            type = ArticleType.GettingStarted,
            readingMinutes = 8,
            tags = listOf("Roadmap", "Kotlin", "Compose Multiplatform"),
        ),
        sections = listOf(
            ArticleSection("goal", "你會做出什麼") {
                Alert(
                    title = "最終作品",
                    message = "一個可在 Desktop 與 Web 共用的帳號設定畫面，包含輸入、驗證、儲存狀態、成功提示與確認視窗。",
                    tone = Tone.Success,
                )
                Text("建議節奏是每週 3～5 小時；已有 Kotlin 經驗時，可以直接用四天完成。")
            },
            ArticleSection("route", "四階段學習路徑") {
                RoadmapCard(
                    badge = "階段 1 · 0.5 天",
                    title = "讀懂框架",
                    description = "理解 commonMain、Composable、Modifier、Theme 與語意化元件。",
                    outcome = "能說明畫面如何從共用程式碼顯示在 Desktop 與 Web。",
                    onClick = { onOpen(LearningChapter.Foundation) },
                )
                RoadmapCard(
                    badge = "階段 2 · 1 天",
                    title = "完成第一個畫面",
                    description = "使用 Card、Alert、Button 與排版元件組合資訊頁。",
                    outcome = "不碰底層 Material API，也能做出一致的畫面。",
                    onClick = { onOpen(LearningChapter.FirstScreen) },
                )
                RoadmapCard(
                    badge = "階段 3 · 1～2 天",
                    title = "掌握狀態與表單",
                    description = "學會受控狀態、驗證、事件 callback，以及 loading/success/error。",
                    outcome = "輸入資料有單一來源，畫面可以預測、測試與重用。",
                    onClick = { onOpen(LearningChapter.State) },
                )
                RoadmapCard(
                    badge = "階段 4 · 2 天",
                    title = "接上架構並完成作品",
                    description = "分離 UI、商業規則、repository 與平台能力，再完成實戰專案。",
                    outcome = "同一套畫面邏輯可跨平台，平台差異留在 adapter。",
                    onClick = { onOpen(LearningChapter.Capstone) },
                )
            },
            ArticleSection("method", "每一章怎麼學") {
                ArticleStep(1, "先執行") { Text("先啟動現有 App，觀察元件的外觀與互動。") }
                ArticleStep(2, "再複製") { Text("複製章節中的最小範例，確認可以編譯。") }
                ArticleStep(3, "刻意修改") { Text("更換文字、Tone、驗證條件或狀態，觀察畫面差異。") }
                ArticleStep(4, "用完成條件自評") { Text("能不用看答案重新寫出來，才進到下一章。") }
            },
        ),
    )
}

@Composable
private fun BookPlanningChapter() {
    GuideArticle(
        meta = ArticleMeta(
            title = "如何規劃一篇教學與一本技術書",
            summary = "先決定讀者要完成的能力，再安排概念、操作、練習與驗證；篇章不是 API 的排列清單。",
            type = ArticleType.HowTo,
            readingMinutes = 15,
            tags = listOf("Writing", "Curriculum", "Guide"),
        ),
        sections = listOf(
            ArticleSection("audience", "1. 定義讀者與先備知識") {
                Text("每一篇開頭寫清楚讀者目前會什麼、這篇結束後能做什麼，以及預估時間。")
                CodeBlock(
                    code = """
                        讀者：會 Kotlin 基本語法，第一次接觸 Compose
                        目標：能做出一個受控的 Email 輸入欄位
                        先備：理解 val、var、函式與 lambda
                        完成證據：輸入無效時顯示錯誤，有效時按鈕才可使用
                    """.trimIndent(),
                    language = "text",
                )
            },
            ArticleSection("sequence", "2. 使用固定的篇章骨架") {
                ArticleStep(1, "情境") { Text("先說明為什麼要學，以及它解決哪個真實問題。") }
                ArticleStep(2, "心智模型") { Text("用一個可記憶的規則解釋原理，例如『資料往下、事件往上』。") }
                ArticleStep(3, "最小範例") { Text("只保留本章的新概念，所有依賴與狀態都要完整。") }
                ArticleStep(4, "逐步擴充") { Text("一次只增加一個狀態或例外情境。") }
                ArticleStep(5, "刻意練習") { Text("要求讀者改變條件，而不是原樣複製。") }
                ArticleStep(6, "完成條件") { Text("以可觀察、可測試的結果收尾。") }
            },
            ArticleSection("types", "3. 不同文章解決不同問題") {
                ConceptCard("Getting Started", "最短成功路徑；讓第一次使用者快速看到成果。")
                ConceptCard("Tutorial", "連續步驟完成一個完整作品；適合學習。")
                ConceptCard("How-to", "針對明確任務提供做法；適合工作時查找。")
                ConceptCard("Concept", "解釋架構、原理與取捨；建立判斷能力。")
                ConceptCard("Reference", "精確列出 API、參數、預設值與限制。")
                ConceptCard("Troubleshooting", "從症狀、原因、檢查到修復。")
            },
            ArticleSection("quality", "4. 發布前檢查") {
                ChecklistItem("範例包含必要 import、state 與 callback，不使用未解釋變數。")
                ChecklistItem("標題描述能力或任務，不只寫元件名稱。")
                ChecklistItem("概念篇與 API 參考分開，讀者能選擇閱讀模式。")
                ChecklistItem("每章都有練習與 Definition of Done。")
                ChecklistItem("Desktop 與 Web 的差異有明確標示。")
            },
        ),
    )
}

@Composable
private fun ComposeChapter() {
    GuideArticle(
        meta = ArticleMeta(
            title = "Compose 概念篇：宣告式 UI 與重新組合",
            summary = "Compose 不是逐行命令 UI 改變，而是讓目前狀態決定畫面應該長什麼樣子。",
            type = ArticleType.Concept,
            readingMinutes = 20,
            tags = listOf("Compose", "Recomposition", "Modifier"),
        ),
        sections = listOf(
            ArticleSection("declarative", "宣告式 UI") {
                CodeBlock(
                    code = """
                        @Composable
                        fun SaveStatus(saved: Boolean) {
                            if (saved) {
                                Alert("已儲存", tone = Tone.Success)
                            } else {
                                Text("尚未儲存")
                            }
                        }
                    """.trimIndent(),
                    language = "Kotlin",
                )
                Text("當 saved 改變，Compose 重新執行需要更新的 Composable，產生對應畫面。")
            },
            ArticleSection("remember", "remember 與狀態擁有者") {
                Text("remember 保存跨重新組合的 UI 狀態；需要跨設定、流程或持久化的資料，應提升到 ViewModel 或 repository。")
                ArticleCallout(
                    title = "不要記住衍生資料",
                    message = "能從現有 state 計算的值直接計算，例如 canSave = name.isNotBlank()；避免建立第二份可能不同步的狀態。",
                    tone = Tone.Warning,
                )
            },
            ArticleSection("modifier", "Modifier 的順序") {
                CodeBlock(
                    code = """
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    """.trimIndent(),
                    language = "Kotlin",
                )
                Text("Modifier 像資料管線，由左到右包裝行為。padding 在 background 前後會得到不同的視覺範圍。")
            },
            ArticleSection("side-effects", "副作用放到正確位置") {
                Text("Composable 可以被重複執行，不要在函式主體直接發 API request 或寫檔。使用事件 callback、LaunchedEffect 或由 ViewModel 管理一次性工作。")
            },
            ArticleSection("checkpoint", "完成條件") {
                Text("能解釋 recomposition、remember、state hoisting 與 Modifier 順序，並能指出為什麼 Composable 主體不適合直接執行儲存。")
            },
        ),
    )
}

@Composable
private fun SharedChapter() {
    GuideArticle(
        meta = ArticleMeta(
            title = "Kotlin Shared：commonMain 的概念與學習方式",
            summary = "Shared module 是跨平台共用的產品核心：放 UI、model、驗證與介面，不放 JVM 或瀏覽器專屬實作。",
            type = ArticleType.Concept,
            readingMinutes = 18,
            tags = listOf("Kotlin", "commonMain", "Shared"),
        ),
        sections = listOf(
            ArticleSection("belongs", "什麼應該放進 shared") {
                ChecklistItem("可跨平台的 Composable 與 design system 元件。")
                ChecklistItem("資料 model、表單驗證、畫面狀態與純 Kotlin 商業規則。")
                ChecklistItem("Repository、file picker 等能力的介面或 request model。")
                ChecklistItem("commonTest 的規則與流程測試。")
            },
            ArticleSection("not-belongs", "什麼不該直接放進 commonMain") {
                Text("java.io.File、Swing、OkHttp 的 JVM 實作，以及 window、DOM、browser API。這些依賴應留在 desktopApp 或 webApp。")
            },
            ArticleSection("boundary", "以介面建立平台邊界") {
                CodeBlock(
                    code = """
                        // shared/commonMain
                        interface PlatformFilePicker {
                            suspend fun pickFiles(request: FilePickerRequest): List<PickedFile>
                        }

                        @Composable
                        fun AttachmentScreen(filePicker: PlatformFilePicker) {
                            // 共用畫面只依賴介面
                        }
                    """.trimIndent(),
                    language = "Kotlin",
                )
            },
            ArticleSection("study", "建議學習順序") {
                ArticleStep(1, "純 Kotlin") { Text("先熟悉 data class、sealed type、lambda、coroutine 與 Result。") }
                ArticleStep(2, "共用 UI") { Text("在 commonMain 寫無平台依賴的 Composable。") }
                ArticleStep(3, "能力介面") { Text("為檔案、網路或儲存定義小而清楚的 contract。") }
                ArticleStep(4, "平台注入") { Text("在 main entry 建立實作，再以參數交給 App。") }
                ArticleStep(5, "commonTest") { Text("用 fake implementation 驗證規則，不啟動平台 UI。") }
            },
        ),
    )
}

@Composable
private fun WebMainChapter() {
    GuideArticle(
        meta = ArticleMeta(
            title = "WebMain 框架概念與寫法",
            summary = "webApp 負責瀏覽器啟動點與 Web adapter；共用 App 仍由 shared 提供。",
            type = ArticleType.Tutorial,
            readingMinutes = 16,
            tags = listOf("webMain", "JS", "Wasm"),
        ),
        sections = listOf(
            ArticleSection("entry", "瀏覽器入口") {
                CodeBlock(
                    code = """
                        @OptIn(ExperimentalComposeUiApi::class)
                        fun main() {
                            val filePicker = BrowserFilePicker()
                            val questionApi = WebQuestionApi(
                                "http://localhost:8000/api/upload",
                                filePicker = filePicker,
                            )

                            ComposeViewport {
                                WithFontResourcesLoaded {
                                    App(
                                        submitQuestion = questionApi::submit,
                                        filePicker = filePicker,
                                    )
                                }
                            }
                        }
                    """.trimIndent(),
                    language = "Kotlin",
                )
            },
            ArticleSection("roles", "每一層的責任") {
                ConceptCard("ComposeViewport", "把 Compose 內容掛載到瀏覽器 viewport。")
                ConceptCard("WithFontResourcesLoaded", "等待 Web 字型資源就緒，再顯示共用 App。")
                ConceptCard("BrowserFilePicker", "封裝瀏覽器檔案選擇能力，實作 shared contract。")
                ConceptCard("WebQuestionApi", "處理 Web request 與 browser file data。")
            },
            ArticleSection("rules", "Web 專屬程式碼規則") {
                Text("只有 adapter 與入口直接使用 browser API。畫面如果需要 URL、storage 或 clipboard，先在 shared 定義介面，再從 Web 注入。")
            },
            ArticleSection("run", "執行與驗證") {
                CodeBlock(
                    code = """
                        ./gradlew :webApp:wasmJsBrowserDevelopmentRun
                        ./gradlew :webApp:jsBrowserDevelopmentRun
                    """.trimIndent(),
                    language = "shell",
                )
                ChecklistItem("確認字型載入前不閃爍錯誤版面。")
                ChecklistItem("確認檔案選擇取消時不被當成失敗。")
                ChecklistItem("確認 API endpoint、CORS 與錯誤訊息。")
            },
        ),
    )
}

@Composable
private fun DesktopAppChapter() {
    GuideArticle(
        meta = ArticleMeta(
            title = "DesktopApp 框架概念與寫法",
            summary = "desktopApp 建立視窗、生命週期與 JVM adapter，再把能力注入同一個 shared App。",
            type = ArticleType.Tutorial,
            readingMinutes = 16,
            tags = listOf("Desktop", "JVM", "Window"),
        ),
        sections = listOf(
            ArticleSection("entry", "Desktop 入口") {
                CodeBlock(
                    code = """
                        fun main() = application {
                            val filePicker = remember { DesktopFilePicker() }
                            val questionApi = remember {
                                DesktopQuestionApi("http://localhost:8000/api/upload")
                            }

                            Window(
                                onCloseRequest = ::exitApplication,
                                title = "ConsoleApp",
                                state = rememberWindowState(
                                    width = 1024.dp,
                                    height = 768.dp,
                                ),
                            ) {
                                App(
                                    submitQuestion = questionApi::submit,
                                    filePicker = filePicker,
                                    dialogHost = { filePicker.Host() },
                                )
                            }
                        }
                    """.trimIndent(),
                    language = "Kotlin",
                )
            },
            ArticleSection("roles", "Desktop 專屬責任") {
                ConceptCard("application / Window", "管理 JVM 應用程式生命週期、關閉事件、標題與視窗尺寸。")
                ConceptCard("remember", "避免重新組合時重建持有原生資源的 adapter。")
                ConceptCard("DesktopFilePicker", "將 Swing 或系統檔案對話框包裝成 shared 介面。")
                ConceptCard("dialogHost", "把需要 Composable host 的平台 UI 掛入共用 App。")
            },
            ArticleSection("run", "執行與封裝") {
                CodeBlock(
                    code = """
                        ./gradlew :desktopApp:run
                        ./gradlew :desktopApp:createDistributable
                        ./gradlew :desktopApp:packageDistributionForCurrentOS
                    """.trimIndent(),
                    language = "shell",
                )
                ArticleCallout(
                    title = "封裝限制",
                    message = "jpackage 不能跨作業系統建置安裝檔；Windows、macOS、Linux 必須在各自環境打包。",
                    tone = Tone.Warning,
                )
            },
        ),
    )
}

@Composable
private fun FoundationChapter() {
    GuideArticle(
        meta = ArticleMeta(
            title = "框架核心：Compose-first 的跨平台設計系統",
            summary = "先建立正確心智模型：這不是 HTML/CSS wrapper，而是一組建立在 Compose Multiplatform 上的語意化元件。",
            type = ArticleType.Concept,
            readingMinutes = 12,
            tags = listOf("commonMain", "Theme", "Composable"),
        ),
        sections = listOf(
            ArticleSection("mental-model", "四個核心觀念") {
                ConceptCard("1. Composable 是畫面函式", "輸入資料與 callback，輸出 UI；不要在元件內偷偷修改外部資料。")
                ConceptCard("2. commonMain 是共用核心", "畫面與商業規則放在 commonMain，檔案選擇等原生能力由平台 adapter 注入。")
                ConceptCard("3. Theme 提供一致語意", "使用 Tone.Success 或 MaterialTheme 色彩，不要在每個畫面散落固定色碼。")
                ConceptCard("4. Modifier 描述版面", "尺寸、padding、捲動與對齊由 Modifier 串接，順序會影響結果。")
            },
            ArticleSection("structure", "先認識專案地圖") {
                CodeBlock(
                    code = """
                        shared/src/commonMain/
                        ├── kotlin/.../designsystem/components   # 公開元件
                        ├── kotlin/.../designsystem/content      # 文件與 Markdown
                        ├── kotlin/.../designsystem/patterns     # Guide Article
                        └── composeResources                     # 共用資源

                        desktopApp/                              # JVM 平台入口與 adapter
                        webApp/                                  # JS / Wasm 平台入口與 adapter
                    """.trimIndent(),
                    language = "text",
                )
            },
            ArticleSection("imports", "匯入公開 API") {
                CodeBlock(
                    code = """
                        import cg.creamgod.consoleapp.designsystem.components.*
                        import cg.creamgod.consoleapp.designsystem.components.form.*
                        import cg.creamgod.consoleapp.designsystem.patterns.article.*
                    """.trimIndent(),
                    language = "Kotlin",
                )
                ArticleCallout(
                    title = "避免同名衝突",
                    message = "若同時使用 Material 3 Button，請使用 import alias，讓程式碼清楚指出是哪一套元件。",
                    tone = Tone.Warning,
                )
            },
            ArticleSection("checkpoint", "完成條件") {
                Text("你能解釋 commonMain 為什麼不能直接呼叫 JVM 檔案 API，並能找到 Card、FormTextInput 與 GuideArticle 的原始碼。")
            },
        ),
    )
}

@Composable
private fun FirstScreenChapter() {
    GuideArticle(
        meta = ArticleMeta(
            title = "第一個畫面：用語意組合 UI",
            summary = "從文字、Card、Alert 與 Button 開始，學習先描述用途，再處理視覺細節。",
            type = ArticleType.Tutorial,
            readingMinutes = 15,
            tags = listOf("Layout", "Components", "Tone"),
        ),
        sections = listOf(
            ArticleSection("screen", "建立歡迎畫面") {
                CodeBlock(
                    code = """
                        @Composable
                        fun WelcomeScreen(onStart: () -> Unit) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("開始使用 ConsoleApp", style = MaterialTheme.typography.headlineMedium)
                                Alert(
                                    title = "學習目標",
                                    message = "完成一個可跨平台共用的功能頁面。",
                                    tone = Tone.Info,
                                )
                                Card(title = "第一章", subtitle = "約 15 分鐘") {
                                    Text("先從公開元件 API 開始，不急著客製底層樣式。")
                                }
                                Button("開始學習", onClick = onStart)
                            }
                        }
                    """.trimIndent(),
                    language = "Kotlin",
                )
            },
            ArticleSection("reading", "閱讀元件呼叫的方式") {
                ArticleStep(1, "資料往下傳") { Text("title、message、tone 都是畫面交給元件的資料。") }
                ArticleStep(2, "事件往上回報") { Text("onStart 不由 Button 自己決定導向哪裡，而是交回呼叫端。") }
                ArticleStep(3, "排版由容器負責") { Text("Column 決定垂直排列與間距，Card 只處理自己的內容。") }
            },
            ArticleSection("practice", "練習") {
                Text("新增第二張 Card，加入 Warning Alert，再把按鈕改成 Outline variant。")
                ArticleCallout(
                    title = "完成條件",
                    message = "畫面能在明暗主題下清楚顯示；沒有直接寫死背景色與文字色。",
                    tone = Tone.Success,
                )
            },
        ),
    )
}

@Composable
private fun StateChapter() {
    GuideArticle(
        meta = ArticleMeta(
            title = "狀態、事件與表單驗證",
            summary = "理解『狀態由畫面擁有』，就能避免 val cannot be reassigned、資料不同步與難以測試的元件。",
            type = ArticleType.Tutorial,
            readingMinutes = 20,
            tags = listOf("State", "Forms", "Validation"),
        ),
        sections = listOf(
            ArticleSection("controlled", "受控狀態模式") {
                CodeBlock(
                    code = """
                        @Composable
                        fun ProfileScreen() {
                            var email by remember { mutableStateOf("") }
                            val validation = FormValidators.all(
                                FormValidators.required(),
                                FormValidators.email(),
                            ).validate(email)

                            FormTextInput(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                required = true,
                                errorText = validation.message,
                            )
                        }
                    """.trimIndent(),
                    language = "Kotlin",
                )
                Text("value 是目前狀態，onValueChange 是使用者意圖。兩者都由 ProfileScreen 管理。")
            },
            ArticleSection("callback", "為什麼參數不能重新賦值") {
                CodeBlock(
                    code = """
                        @Composable
                        fun ThemeButton(
                            themeMode: ThemeMode,
                            onThemeModeChanged: (ThemeMode) -> Unit,
                        ) {
                            Button("切換主題", onClick = {
                                val next = if (themeMode == ThemeMode.Light) {
                                    ThemeMode.Dark
                                } else {
                                    ThemeMode.Light
                                }
                                onThemeModeChanged(next)
                            })
                        }
                    """.trimIndent(),
                    language = "Kotlin",
                )
                ArticleCallout(
                    title = "關鍵規則",
                    message = "函式參數是唯讀 val。子元件透過 callback 回報新值，由真正擁有 var 狀態的上層更新。",
                    tone = Tone.Info,
                )
            },
            ArticleSection("ui-states", "完整畫面不只有成功狀態") {
                Text("實務功能至少設計 idle、loading、success、validation error 與 repository error。")
                ArticleStep(1, "送出前") { Text("檢查 validation.isValid，無效時不要呼叫 repository。") }
                ArticleStep(2, "送出中") { Text("停用按鈕並顯示進度，避免重複送出。") }
                ArticleStep(3, "完成後") { Text("顯示成功或可恢復的錯誤訊息，讓使用者知道下一步。") }
            },
            ArticleSection("checkpoint", "完成條件") {
                Text("能建立一個必填 Email 欄位，無效時顯示訊息，有效時啟用儲存按鈕，且表單元件內沒有保存重複狀態。")
            },
        ),
    )
}

@Composable
private fun ComponentIndex(onSelect: (ComponentLesson) -> Unit) {
    GuideArticle(
        meta = ArticleMeta(
            title = "元件學習目錄",
            summary = "每個元件都是一個獨立教學頁。先學 state contract，再看範例、常見錯誤與練習。",
            type = ArticleType.Reference,
            readingMinutes = 5,
            tags = listOf("Components", "API", "Catalog"),
        ),
        sections = componentLessons
            .groupBy { it.category }
            .map { (category, lessons) ->
                ArticleSection(category.lowercase().replace(" ", "-"), category) {
                    lessons.forEach { lesson ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            title = lesson.name,
                            subtitle = lesson.purpose,
                            onClick = { onSelect(lesson) },
                        ) {
                            Text(
                                text = "開啟獨立教學頁 →",
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            },
    )
}

@Composable
private fun ComponentLessonPage(
    lesson: ComponentLesson,
    onBack: () -> Unit,
) {
    Button(
        text = "← 返回元件目錄",
        onClick = onBack,
        variant = Variant.Outline,
    )
    GuideArticle(
        meta = ArticleMeta(
            title = lesson.name,
            summary = lesson.purpose,
            type = ArticleType.Reference,
            readingMinutes = 8,
            tags = listOf(lesson.category, "獨立元件頁"),
        ),
        sections = listOf(
            ArticleSection("preview", "可視化預覽") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    subtitle = "這是可操作的實際元件，不是圖片。",
                ) {
                    ComponentLivePreview(lesson.name)
                }
            },
            ArticleSection("mental-model", "何時使用") {
                Text(lesson.whenToUse)
                ArticleCallout(
                    title = "狀態責任",
                    message = lesson.stateRule,
                    tone = Tone.Info,
                )
            },
            ArticleSection("example", "最小完整範例") {
                Text("下方程式碼可直接選取與複製；範例包含必要 state 與 callback。")
                CodeBlock(code = lesson.example, language = "Kotlin")
            },
            ArticleSection("breakdown", "逐步拆解") {
                lesson.breakdown.forEachIndexed { index, step ->
                    ArticleStep(index + 1, step.first) { Text(step.second) }
                }
            },
            ArticleSection("business", "實際業務設計案例") {
                ArticleCallout(
                    title = lesson.businessCase.first,
                    message = lesson.businessCase.second,
                    tone = Tone.Secondary,
                )
                Text("設計時先確認資料擁有者、失敗狀態與使用者下一步，再選擇元件。")
            },
            ArticleSection("mistakes", "常見錯誤") {
                lesson.mistakes.forEach { Text("• $it") }
            },
            ArticleSection("practice", "練習與完成條件") {
                Text(lesson.practice)
                ArticleCallout(
                    title = "完成條件",
                    message = "範例能在明暗主題下運作，狀態與 callback 來源清楚，且沒有把業務副作用藏在元件內。",
                    tone = Tone.Success,
                )
            },
        ),
    )
}

@Composable
private fun ComponentLivePreview(name: String) {
    var text by remember(name) { mutableStateOf("") }
    var checked by remember(name) { mutableStateOf(false) }

    when (name) {
        "Button" -> Button("儲存變更", onClick = {})
        "Alert" -> Alert("資料已成功儲存", tone = Tone.Success)
        "Callout" -> cg.creamgod.consoleapp.designsystem.components.Callout(
            title = "發布前檢查",
            message = "請確認測試與版本號。",
            tone = Tone.Warning,
        )
        "Badge" -> Badge("已啟用", tone = Tone.Success, pill = true)
        "Progress" -> Progress(0.65f)
        "Spinner" -> cg.creamgod.consoleapp.designsystem.components.Spinner()
        "Card" -> Card(title = "方案", subtitle = "專業版") { Text("每月 NT$300") }
        "FormTextInput" -> FormTextInput(text, { text = it }, label = "Email")
        "FormPasswordInput" -> FormPasswordInput(text, { text = it }, label = "密碼")
        "FormTextArea" -> FormTextArea(text, { text = it }, label = "備註")
        "FormCheckbox" -> FormCheckbox(checked, { checked = it }, label = "我同意條款")
        "FormSwitch" -> FormSwitch(checked, { checked = it }, label = "接收通知")
        "Collapse" -> cg.creamgod.consoleapp.designsystem.components.Collapse(expanded = true) {
            Text("展開後的詳細內容")
        }
        "Placeholder" -> cg.creamgod.consoleapp.designsystem.components.Placeholder(
            Modifier.fillMaxWidth(),
            animated = true,
        )
        "H1–H6 / Paragraph" -> {
            Text("語意標題", style = MaterialTheme.typography.headlineMedium)
            Text("使用 theme 控制的內文排版。")
        }
        "CodeBlock" -> CodeBlock("Button(\"Save\", onClick = onSave)", language = "Kotlin")
        else -> {
            Badge(name, tone = Tone.Primary)
            Text(
                text = "此頁專注說明 $name 的互動合約；請使用下方完整範例在 Playground 操作。",
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ArchitectureChapter() {
    GuideArticle(
        meta = ArticleMeta(
            title = "架構：把 UI、規則與平台能力分開",
            summary = "元件負責呈現，screen 負責狀態協調，repository 負責資料，adapter 負責平台差異。",
            type = ArticleType.Concept,
            readingMinutes = 18,
            tags = listOf("Architecture", "Repository", "Adapter"),
        ),
        sections = listOf(
            ArticleSection("layers", "四層責任") {
                ConceptCard("Component", "接收 value 與 callback，呈現單一可重用互動。")
                ConceptCard("Screen / ViewModel", "管理畫面狀態、驗證、loading 與事件流程。")
                ConceptCard("Repository", "定義讀寫資料的邊界，不依賴具體畫面。")
                ConceptCard("Platform adapter", "封裝 Desktop 與 Web 的檔案、網路或系統 API 差異。")
            },
            ArticleSection("flow", "資料流") {
                CodeBlock(
                    code = """
                        使用者操作
                            ↓ callback
                        Screen / ViewModel
                            ↓ suspend function
                        Repository interface (commonMain)
                            ↓ implementation
                        Desktop 或 Web adapter
                            ↓ result
                        Screen state → UI 重新組合
                    """.trimIndent(),
                    language = "text",
                )
            },
            ArticleSection("contract", "先定義共用合約") {
                CodeBlock(
                    code = """
                        interface ProfileRepository {
                            suspend fun load(): Profile
                            suspend fun save(profile: Profile): Result<Unit>
                        }

                        data class Profile(
                            val displayName: String,
                            val email: String,
                            val notificationsEnabled: Boolean,
                        )
                    """.trimIndent(),
                    language = "Kotlin",
                )
            },
            ArticleSection("checkpoint", "完成條件") {
                Text("用 fake repository 測試儲存成功與失敗，不啟動 Desktop 視窗或瀏覽器也能驗證商業流程。")
            },
        ),
    )
}

@Composable
private fun CapstoneChapter() {
    GuideArticle(
        meta = ArticleMeta(
            title = "實戰作品：帳號設定頁",
            summary = "把前面章節串起來，完成可跨平台、可驗證、可測試的帳號設定功能。",
            type = ArticleType.Tutorial,
            readingMinutes = 30,
            tags = listOf("Capstone", "Testing", "Definition of Done"),
        ),
        sections = listOf(
            ArticleSection("requirements", "功能需求") {
                ArticleStep(1, "個人資料") { Text("顯示名稱、Email，以及是否接收通知。") }
                ArticleStep(2, "驗證") { Text("名稱必填；Email 必須符合格式。") }
                ArticleStep(3, "儲存流程") { Text("呈現送出中、成功與失敗狀態，避免重複送出。") }
                ArticleStep(4, "危險操作") { Text("重設設定前顯示 Confirm，確認後才呼叫 repository。") }
            },
            ArticleSection("plan", "建置順序") {
                Text("1. 建立 Profile 與 ProfileRepository。")
                Text("2. 先用 fake repository 寫 screen state 與驗證。")
                Text("3. 使用 FormSection、FormTextInput、FormSwitch 與 FormActions 組合 UI。")
                Text("4. 加入 Alert、Confirm 與錯誤恢復。")
                Text("5. 最後才接 Desktop / Web 的真實 repository。")
            },
            ArticleSection("skeleton", "畫面骨架") {
                CodeBlock(
                    code = """
                        FormSection(title = "帳號設定") {
                            FormTextInput(
                                value = state.displayName,
                                onValueChange = onNameChanged,
                                label = "顯示名稱",
                                errorText = state.nameError,
                            )
                            FormTextInput(
                                value = state.email,
                                onValueChange = onEmailChanged,
                                label = "Email",
                                errorText = state.emailError,
                            )
                            FormSwitch(
                                checked = state.notificationsEnabled,
                                onCheckedChange = onNotificationsChanged,
                                label = "接收通知",
                            )
                            FormActions(
                                onSubmit = onSave,
                                submitEnabled = state.canSave,
                                submitText = if (state.saving) "儲存中…" else "儲存",
                            )
                        }
                    """.trimIndent(),
                    language = "Kotlin",
                )
            },
            ArticleSection("done", "Definition of Done") {
                ChecklistItem("Desktop 與 Web 顯示相同功能。")
                ChecklistItem("所有輸入皆由 screen state 控制。")
                ChecklistItem("驗證失敗時不呼叫 repository。")
                ChecklistItem("儲存中不能重複送出。")
                ChecklistItem("成功、失敗與重設都有清楚回饋。")
                ChecklistItem("fake repository 測試涵蓋成功與失敗。")
                ArticleCallout(
                    title = "畢業標準",
                    message = "關閉本書後，你仍能從空白 screen 重新建立上述流程，並說明每一層的責任。",
                    tone = Tone.Success,
                )
            },
            ArticleSection("next", "下一步") {
                Text("完成後請打開 App 內的 Components Guide，逐一探索元件 Playground 與 Catalog；需要精確參數時再查 docs/component-reference.md。")
            },
        ),
    )
}

@Composable
private fun RoadmapCard(
    badge: String,
    title: String,
    description: String,
    outcome: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        title = title,
        subtitle = description,
        onClick = onClick,
    ) {
        Badge(badge, tone = Tone.Secondary, pill = true)
        Text(
            text = "完成成果：$outcome",
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConceptCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        title = title,
    ) {
        Text(description)
    }
}

@Composable
private fun ChecklistItem(text: String) {
    Text("✓ $text", style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun ChapterNavigation(
    chapter: LearningChapter,
    onSelect: (LearningChapter) -> Unit,
) {
    val chapters = LearningChapter.entries
    val index = chapters.indexOf(chapter)
    val previous = chapters.getOrNull(index - 1)
    val next = chapters.getOrNull(index + 1)

    if (previous != null || next != null) {
        GuideArticle(
            meta = ArticleMeta(
                title = "繼續學習",
                summary = "依照路徑前進，或返回上一章複習。",
                type = ArticleType.HowTo,
                readingMinutes = 1,
            ),
            sections = emptyList(),
            previous = previous?.let { item ->
                ArticleLink(item.label) { onSelect(item) }
            },
            next = next?.let { item ->
                ArticleLink(item.label) { onSelect(item) }
            },
        )
    }
}

private enum class LearningChapter(
    val label: String,
    val shortLabel: String,
) {
    Roadmap("學習地圖", "地圖"),
    BookPlanning("篇章規劃", "規劃"),
    Foundation("框架基礎", "基礎"),
    Compose("Compose 概念", "Compose"),
    Shared("Kotlin Shared", "Shared"),
    Web("WebMain", "Web"),
    Desktop("DesktopApp", "Desktop"),
    FirstScreen("第一個畫面", "畫面"),
    State("狀態與表單", "狀態"),
    Components("元件逐頁學習", "元件"),
    Architecture("跨平台架構", "架構"),
    Capstone("實戰作品", "實戰"),
}

private data class ComponentLesson(
    val category: String,
    val name: String,
    val purpose: String,
    val whenToUse: String,
    val stateRule: String,
    val example: String,
    val breakdown: List<Pair<String, String>>,
    val businessCase: Pair<String, String>,
    val mistakes: List<String>,
    val practice: String,
)

private fun componentLesson(
    category: String,
    name: String,
    purpose: String,
    example: String,
    businessTitle: String,
    businessDescription: String,
    stateRule: String = "畫面或 ViewModel 擁有業務狀態；元件接收目前值並透過 callback 回報使用者意圖。",
) = ComponentLesson(
    category = category,
    name = name,
    purpose = purpose,
    whenToUse = "當畫面需要「$purpose」時使用 $name。先確認這是使用者任務所需，而不是只為裝飾增加元件。",
    stateRule = stateRule,
    example = example.trimIndent(),
    breakdown = listOf(
        "準備資料" to "在呼叫端建立顯示資料與必要 state，不讓元件自行猜測業務值。",
        "連接事件" to "將 callback 連到明確 intent；需要 I/O 時由 screen 或 ViewModel 啟動。",
        "處理狀態" to "補齊 disabled、loading、error、empty 與 accessibility 等情境。",
    ),
    businessCase = businessTitle to businessDescription,
    mistakes = listOf(
        "把 repository 或平台 API 直接藏進可重用元件。",
        "只展示 happy path，沒有 disabled、錯誤或空資料狀態。",
        "用顯示文字當資料識別值，造成翻譯或改名後狀態失效。",
    ),
    practice = "將範例套用到自己的業務資料，加入一個失敗或 disabled 情境，並確認 callback 只回報 intent。",
)

private val componentLessons = listOf(
    componentLesson("Actions", "Button", "觸發一個清楚、立即的動作", """
        var saved by remember { mutableStateOf(false) }
        Button("儲存", onClick = { saved = true }, enabled = !saved)
        if (saved) Alert("已儲存", tone = Tone.Success)
    """, "儲存表單", "表單驗證通過後才啟用主要按鈕；送出中停用，完成後顯示結果。"),
    componentLesson("Actions", "ButtonGroup", "在少量互斥動作中選擇一個", """
        var mode by remember { mutableStateOf("list") }
        ButtonGroup(
            items = listOf(ButtonItem("list", "列表"), ButtonItem("grid", "網格")),
            selected = mode,
            onSelect = { mode = it },
        )
    """, "檢視模式", "報表頁讓使用者切換列表與網格，選擇值由 URL/query state 擁有。"),
    componentLesson("Actions", "CloseButton", "關閉可撤銷的訊息或區塊", "CloseButton(onClick = onDismiss)", "關閉公告", "關閉只改變 visible 狀態，不應順便刪除伺服器資料。"),
    componentLesson("Actions", "Dropdown", "收納次要動作清單", """
        Dropdown(
            label = "匯出",
            items = listOf(DropdownItem("csv", "CSV"), DropdownItem("pdf", "PDF")),
            onSelect = onExport,
        )
    """, "報表匯出", "將低頻率格式放入選單，onSelect 回傳穩定 id 而不是顯示文字。"),

    componentLesson("Feedback", "Alert", "在內容流中呈現持續狀態", "Alert(\"付款完成\", tone = Tone.Success)", "付款結果", "成功或失敗訊息留在訂單內容中，直到使用者理解或關閉。"),
    componentLesson("Feedback", "Callout", "以標題、訊息與 action 強調重要資訊", "Callout(title = \"備份提醒\", message = \"升級前請先備份\", tone = Tone.Warning)", "系統維護", "在設定頁說明不可忽略的維護前置作業。"),
    componentLesson("Feedback", "Badge", "顯示短狀態、分類或數量", "Badge(\"已付款\", tone = Tone.Success, pill = true)", "訂單狀態", "用語意 tone 顯示訂單狀態，同時保留文字，不能只依賴顏色。"),
    componentLesson("Feedback", "Progress", "顯示已知比例的工作進度", "Progress(value = uploadedBytes.toFloat() / totalBytes)", "檔案上傳", "由 uploader 提供 0f..1f 進度；未知長度改用 Spinner。"),
    componentLesson("Feedback", "Spinner", "顯示未知完成時間的等待", "if (loading) Spinner()", "載入搜尋結果", "request 進行中顯示等待，但同時保留可理解的 loading 文字。"),
    componentLesson("Feedback", "Placeholder", "資料載入前保留版面骨架", "Placeholder(Modifier.fillMaxWidth().height(24.dp))", "Dashboard 首次載入", "骨架形狀對應即將出現的內容，避免 layout shift。"),
    componentLesson("Feedback", "Toast / ToastHost", "顯示短暫、跨區域的操作結果", """
        val toastState = rememberToastState()
        ToastHost(toastState)
        scope.launch { toastState.show("設定已更新") }
    """, "全域儲存結果", "頁面切換後仍可看見短暫成功訊息；需要持續處理的錯誤改用 Alert。"),

    componentLesson("Disclosure", "Collapse", "受控地展開或收合內容", "Collapse(expanded = detailsVisible) { Text(\"詳細資料\") }", "訂單詳情", "畫面擁有 expanded，深層連結需要時可由 route state 控制。"),
    componentLesson("Disclosure", "Accordion", "組織多組可展開說明", "Accordion(listOf(AccordionItem(\"退款政策\") { Text(\"內容\") }))", "FAQ", "用於可獨立閱讀的問題；不要藏住完成任務必須看到的欄位。"),

    componentLesson("Surfaces", "Card", "將標題、內容與動作組成一個資訊單位", "Card(title = \"專業版\", footer = { Button(\"升級\", onClick = onUpgrade) }) { Text(\"每月 NT$300\") }", "訂閱方案", "每張卡代表一個方案；有 footer actions 時避免整張卡同時可點擊。"),
    componentLesson("Surfaces", "ListGroup", "呈現可選取或停用的項目清單", "ListGroup(items = accounts, selected = selectedId, onSelect = onAccountSelected)", "切換工作區", "value 使用 account id，label 僅顯示名稱。"),
    componentLesson("Surfaces", "Carousel", "依序瀏覽少量同類內容", "Carousel(items = slides, showControls = true, showIndicators = true)", "產品導覽", "適合非關鍵的 onboarding；重要警告不可只放在可能被略過的 slide。"),

    componentLesson("Navigation", "Breadcrumb", "顯示階層位置與返回路徑", "Breadcrumb(listOf(BreadcrumbItem(\"首頁\", onClick = onHome), BreadcrumbItem(\"設定\")))", "後台設定", "路徑反映資訊架構，不等同瀏覽器歷史。"),
    componentLesson("Navigation", "Navbar", "提供品牌、主要導覽與動作", "Navbar(\"ConsoleApp\", items, selected, onSelect)", "管理後台", "selected 由 route state 決定，Navbar 不自己管理目前頁。"),
    componentLesson("Navigation", "Tabs", "切換同一情境下的同層內容", "Tabs(items, selected = tab, onSelect = { tab = it })", "帳號設定分頁", "Profile、Security、Billing 共用同一帳號情境；跨產品區域應用 Navbar。"),
    componentLesson("Navigation", "Nav", "建立水平或垂直的導覽清單", "Nav(items, selected = section, onSelect = onSectionSelected, vertical = true)", "文件章節", "導覽狀態可與文章 section id 同步。"),
    componentLesson("Navigation", "Pagination", "切換大量資料的 1-based 頁碼", "Pagination(currentPage = page, pageCount = pageCount, onPageChange = onPageChange)", "使用者搜尋", "query 或 filter 改變時重設到第一頁，request 由 ViewModel 處理。"),
    componentLesson("Navigation", "ScrollSpy", "標示目前閱讀中的文件段落", "ScrollSpy(items, activeSection = section, onNavigate = onNavigate)", "長篇技術文件", "active section 由 screen 提供；共用元件不直接讀 DOM。"),

    componentLesson("Forms", "FormTextInput", "輸入單行文字、Email、搜尋或數字", """
        var email by remember { mutableStateOf("") }
        val result = FormValidators.email().validate(email)
        FormTextInput(email, { email = it }, "Email", errorText = result.message)
    """, "會員 Email", "顯示即時格式驗證，真正唯一性仍由後端確認。"),
    componentLesson("Forms", "FormPasswordInput", "輸入可切換顯示狀態的密碼", "FormPasswordInput(password, { password = it }, label = \"密碼\")", "登入", "密碼只存在必要的短生命週期 state，不寫入 log 或 rememberSaveable。"),
    componentLesson("Forms", "FormTextArea", "輸入多行內容", "FormTextArea(note, { note = it }, label = \"備註\", maxLines = 8)", "客服工單", "搭配字數限制與 helper text，避免無界內容。"),
    componentLesson("Forms", "FormSelect", "從固定選項選擇一個值", "FormSelect(value = role, options = roles, onValueChange = { role = it }, label = \"角色\")", "成員角色", "option value 使用 enum/id，不使用翻譯 label。"),
    componentLesson("Forms", "FormMultiSelect", "從固定選項選擇多個值", "FormMultiSelect(values = tags, options = options, onValuesChange = { tags = it }, label = \"標籤\")", "文章標籤", "使用 Set 避免重複，送出前再轉成 API 格式。"),
    componentLesson("Forms", "FormCheckbox", "確認獨立的是／否選項", "FormCheckbox(accepted, { accepted = it }, label = \"我同意條款\")", "服務條款", "必填同意要有驗證與可存取的條款連結。"),
    componentLesson("Forms", "FormRadioGroup", "在互斥選項中直接看見所有選擇", "FormRadioGroup(plan, plans, { plan = it }, label = \"方案\")", "選擇訂閱方案", "選項少且需要比較時優於 Select。"),
    componentLesson("Forms", "FormSwitch", "立即切換一項設定", "FormSwitch(notifications, { notifications = it }, label = \"接收通知\")", "通知偏好", "若切換立即寫入後端，需處理 loading、失敗回復與錯誤訊息。"),
    componentLesson("Forms", "FormRange", "在連續範圍中選擇數值", "FormRange(threshold, { threshold = it }, \"警示門檻\", 0f..100f)", "用量警示", "顯示格式化後的目前值，精確輸入需求應另提供文字欄位。"),
    componentLesson("Forms", "FormSection", "將相關欄位組成有標題的段落", "FormSection(title = \"個人資料\") { /* fields */ }", "帳號設定", "依使用者任務分段，不依資料表欄位隨意分組。"),
    componentLesson("Forms", "FormInputGroup", "在輸入前後加入 prefix 或 suffix", "FormInputGroup(prefix = { Text(\"https://\") }) { FormTextInput(slug, onSlugChange, \"網址\") }", "網站 slug", "prefix 是輔助視覺；完整可存取 label 仍不可省略。"),
    componentLesson("Forms", "FormFilePicker", "呈現跨平台檔案選擇欄位", "FormFilePicker(fileNames, onPick = onPick, onClear = onClear, label = \"附件\")", "報帳附件", "共用 UI 只接檔名與 intent，真正檔案由 platform adapter 保存。"),
    componentLesson("Forms", "FormActions", "統一表單送出與重設動作", "FormActions(onSubmit = onSave, submitEnabled = valid && !saving, onReset = onReset)", "編輯資料", "送出狀態由 screen 擁有；重設若會丟失資料可先 Confirm。"),
    componentLesson("Forms", "FormValidators", "組合可獨立測試的輸入規則", "val result = FormValidators.all(FormValidators.required(), FormValidators.email()).validate(email)", "註冊驗證", "前端驗證改善體驗，後端仍必須執行權威驗證。"),

    componentLesson("Overlay", "Popup", "在浮層中呈現自訂內容", "Popup(visible = visible, onDismiss = onDismiss) { Text(\"內容\") }", "快捷面板", "visible 由呼叫端控制，點擊外部只回報 dismiss intent。"),
    componentLesson("Overlay", "Modal", "讓使用者專注完成一個有限任務", "Modal(visible, onDismiss, title = \"編輯名稱\") { /* form */ }", "快速編輯", "長流程應使用獨立頁面；Modal 失敗時保留草稿。"),
    componentLesson("Overlay", "Confirm", "在不可逆或高風險動作前確認", "Confirm(visible, onConfirm = onDelete, onDismiss = onDismiss, title = \"刪除？\", destructive = true)", "刪除帳號", "確認文字明確指出對象與後果；失敗時不自動關閉。"),
    componentLesson("Overlay", "Ask", "在對話框取得一段文字", "Ask(visible, value, onValueChange, onConfirm, onDismiss, title = \"輸入原因\")", "取消訂單原因", "value 是草稿，onConfirm 才觸發業務動作。"),
    componentLesson("Overlay", "Choice", "在對話框選擇一個選項", "Choice(visible, value, options, onValueChange, onConfirm, onDismiss, title = \"選擇狀態\")", "工單狀態", "草稿選擇與確認提交分離，取消不修改正式資料。"),
    componentLesson("Overlay", "Offcanvas", "從側邊顯示輔助流程或篩選器", "Offcanvas(visible, onDismiss, title = \"篩選\") { /* filters */ }", "搜尋篩選", "套用前可保留 filter draft；確認後再更新 query 並重設頁碼。"),
    componentLesson("Overlay", "Popover", "在 anchor 附近提供補充資訊與動作", "Popover(visible, onDismiss, title = \"權限\", message = \"可編輯專案\")", "權限說明", "內容應短且非關鍵，重要規則留在頁面中。"),
    componentLesson("Overlay", "Tooltip", "解釋圖示或簡短控制項", "Tooltip(visible = hovered, text = \"重新整理\") { IconButton(onClick = onRefresh) { /* icon */ } }", "圖示按鈕", "Tooltip 不能取代 contentDescription，也不要放互動內容。"),

    componentLesson("Content", "H1–H6 / Paragraph", "建立有語意層級的文件排版", "H1(\"帳號設定\")\nParagraph(\"管理個人資料與通知偏好。\")", "說明中心", "每頁只有一個主標題，層級不可只為字體大小而跳級。"),
    componentLesson("Content", "DocumentRenderer", "渲染強型別文件模型", "DocumentRenderer(document { h1(\"指南\"); paragraph(\"內容\") })", "內建操作手冊", "資料可由 DSL 或 parser 產生，renderer 只負責呈現。"),
    componentLesson("Content", "MarkdownDocument", "解析並顯示 Markdown 字串", "MarkdownDocument(\"# 標題\\n\\n**內容**\")", "CMS 預覽", "不信任內容需先定義連結、圖片與 HTML 安全策略。"),
    componentLesson("Content", "MarkdownResource", "載入 composeResources 內的 Markdown", "MarkdownResource(\"docs/getting-started.md\")", "離線說明文件", "resource path 在 commonMain 中保持一致，並提供 loading/failure UI。"),
    componentLesson("Articles", "GuideArticle", "組合有目錄、metadata 與前後頁的教學文章", "GuideArticle(meta = meta, sections = sections, next = ArticleLink(\"下一章\", onNext))", "產品學習中心", "章節資料與導覽由書籍 screen 擁有，文章 pattern 只渲染。"),
    componentLesson("Articles", "ArticleStep", "拆解有順序的教學步驟", "ArticleStep(1, \"建立 state\") { Text(\"先定義資料來源。\") }", "上手教學", "每一步只增加一個新概念，標題使用可執行動詞。"),
    componentLesson("Articles", "ArticleCallout", "標示注意、風險或完成條件", "ArticleCallout(\"注意\", \"送出中不可重複點擊。\", tone = Tone.Warning)", "部署指南", "Callout 補充主流程，不要讓整篇文章充滿警告框。"),
    componentLesson("Articles", "CodeBlock", "顯示可選取複製的程式碼", "CodeBlock(\"Button(\\\"Save\\\", onClick = onSave)\", language = \"Kotlin\")", "API 教學", "範例必須定義 state 與 callback 來源，不能留下無解釋 placeholder。"),
)
