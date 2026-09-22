# GitHub Wiki source

GitHub Wiki 使用獨立的 `<repository>.wiki.git` repository；這個目錄保存可版本控制、可 review
的 Wiki 原稿。

同步方式：

1. 在 GitHub repository 啟用 Wiki 並建立第一個頁面。
2. Clone `<repository>.wiki.git`。
3. 將本目錄的 `.md` 檔複製到 Wiki checkout 根目錄。
4. Commit 並 push Wiki repository。

`Home.md` 是首頁，`_Sidebar.md` 是全站導覽。其餘檔名應維持不變，避免 Wiki link 失效。
