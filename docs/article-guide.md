# 引導文章作者指南

`GuideArticle()` 用來建立「帶讀者完成目標」的產品文件，而不只是把 API 羅列在頁面上。
它位於：

```kotlin
import cg.creamgod.consoleapp.designsystem.patterns.article.*
```

## 選擇文章類型

| ArticleType | 讀者的問題 | 適合內容 |
| --- | --- | --- |
| `GettingStarted` | 我第一次該怎麼開始？ | 安裝、第一次成功操作、最短可行路徑 |
| `Tutorial` | 可以帶我完整做一次嗎？ | 有順序、有結果的完整練習 |
| `HowTo` | 我要怎麼完成這件事？ | 單一目標、可直接套用的操作方法 |
| `Concept` | 為什麼這樣設計？ | 原理、架構、名詞與取捨 |
| `Reference` | 這個 API 精確怎麼用？ | 參數、回傳值、狀態與限制 |
| `Troubleshooting` | 為什麼失敗，怎麼修？ | 症狀、檢查、原因、修復與驗證 |
| `Migration` | 如何從舊版本安全升級？ | 前置條件、破壞性變更、替換步驟與回滾 |

一篇文章只設定一個主要類型。如果同時想解釋概念又提供操作，應把概念放在短 Callout，
不要讓文章失去主要目標。

## 最小文章

```kotlin
GuideArticle(
    meta = ArticleMeta(
        title = "第一次使用元件庫",
        summary = "完成 import 並顯示第一個成功訊息。",
        type = ArticleType.GettingStarted,
        readingMinutes = 3,
        tags = listOf("Compose", "Quick start"),
    ),
    sections = listOf(
        ArticleSection("import", "匯入元件") {
            Text("一般元件使用單一 package。")
            CodeBlock(
                code = "import cg.creamgod.consoleapp.designsystem.components.*",
                language = "Kotlin",
            )
        },
        ArticleSection("first", "顯示第一個元件") {
            ArticleStep(1, "選擇語意") {
                Text("成功訊息使用 Tone.Success。")
            }
            ArticleStep(2, "加入畫面") {
                CodeBlock(
                    code = "Alert(\"設定已更新\", tone = Tone.Success)",
                    language = "Kotlin",
                )
            }
        },
    ),
)
```

## 可用 building blocks

### GuideArticle

負責文章 header、metadata、目錄、章節與上一篇／下一篇導覽。

```kotlin
GuideArticle(
    meta = meta,
    sections = sections,
    onSectionSelected = { sectionId -> scrollTo(sectionId) },
    previous = ArticleLink("安裝") { openInstallArticle() },
    next = ArticleLink("表單") { openFormArticle() },
)
```

目錄只負責回報 `sectionId`，實際捲動由 screen 的 scroll state 或 navigation 處理。

### ArticleSection

每個章節需要穩定且唯一的 `id`。標題應能單獨說明該段落的目標。

### ArticleStep

用於必須依順序完成的操作。每一步只描述一個主要動作，並在需要時補上成功判斷。

### ArticleCallout

補充不屬於主要流程、但讀者不能忽略的資訊：

```kotlin
ArticleCallout(
    title = "執行前確認",
    message = "Migration 會變更儲存格式，請先建立備份。",
    tone = Tone.Warning,
)
```

建議語意：

- `Info`：背景說明或額外提示。
- `Success`：完成條件與驗證結果。
- `Warning`：可能造成問題的前置條件。
- `Danger`：資料遺失、安全性或不可逆風險。

### CodeBlock

顯示可選取的等寬程式碼與語言標籤。程式碼範例應可以直接複製，省略內容時需用註解明確標示。

## 不同文章類型的結構

### Getting started

1. 告訴讀者完成後會得到什麼。
2. 列出最低前置條件。
3. 提供最短成功路徑。
4. 顯示可觀察的成功結果。
5. 導向下一篇文章。

### Tutorial

1. 設定明確的最終作品。
2. 每一步建立在前一步成果上。
3. 避免中途展開過多架構理論。
4. 最後執行完整驗證並回顧學到的概念。

### How-to

1. 直接以任務命名，例如「如何加入刪除確認」。
2. 只保留完成任務必要的步驟。
3. 提供常見變體與限制。
4. 不需要重新教授所有基礎概念。

### Concept

1. 先定義問題與術語。
2. 說明目前設計與替代方案。
3. 清楚寫出取捨及不適用情境。
4. 連結到相關操作文章。

### Reference

1. 內容必須完整、穩定且容易搜尋。
2. 參數、預設值、狀態與錯誤分開呈現。
3. 範例用來補充規格，不取代規格。

### Troubleshooting

1. 使用讀者看得到的症狀當標題。
2. 先做無破壞性的檢查。
3. 依可能性排列原因。
4. 每個修復步驟都提供驗證方式。

### Migration

1. 標示來源版本與目標版本。
2. 在開頭列出破壞性變更及備份要求。
3. 提供 before／after 範例。
4. 說明資料遷移、回滾與完成驗證。

## 寫作品質檢查

- 標題描述讀者目標，而不是內部模組名稱。
- Summary 用一句話說清楚成果與範圍。
- 前置條件出現在第一個實際步驟之前。
- 每段程式碼都能編譯，或清楚標示為片段。
- 危險動作在執行之前顯示 Warning／Danger。
- 完成條件可被讀者觀察或測試。
- 文章有明確的下一步，而不是突然結束。
- 同一件事不要同時維護多份互相衝突的說明。

## App 內預覽

`ComponentGuide()` 的「文章」頁籤包含一篇 `GettingStarted` 範例，可直接檢查文章在
Desktop、JS 與 Wasm 的呈現：

```kotlin
import cg.creamgod.consoleapp.designsystem.catalog.ComponentGuide

ComponentGuide()
```
