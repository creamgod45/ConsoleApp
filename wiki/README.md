# GitHub Wiki source

GitHub Wiki 使用獨立的 `<repository>.wiki.git` repository；這個目錄保存可版本控制、可 review
的 Wiki 原稿。

同步方式：

1. 在 GitHub repository 啟用 Wiki 並建立第一個頁面。
2. Clone `<repository>.wiki.git`。
3. 將本目錄的 `.md` 檔複製到 Wiki checkout 根目錄。
4. Commit 並 push Wiki repository。

`Home.md` 是首頁，`_Sidebar.md` 是全站導覽。其餘檔名應維持不變，避免 Wiki link 失效。

## 教材結構

- `Learning-Roadmap.md`：完整學習順序與各階段完成條件。
- `Planning-a-Guide-Book.md`：教材篇章、範例與 review 標準。
- `Compose-Concepts.md`：Compose 心智模型與實際狀態案例。
- `Kotlin-Shared.md`：commonMain 邊界、contract、adapter 與測試。
- `WebMain-Guide.md`／`DesktopApp-Guide.md`：平台入口、注入與部署。
- `Component-Learning-Pages.md`：App 內逐元件頁的長篇索引。
- `Business-Case-Study.md`：從需求到測試的完整帳號設定案例。

新增教材不能只有 API 摘要。至少要包含可複製範例、流程拆解、非 happy path、實際業務案例、
練習與可觀察的完成條件。
