package cg.creamgod.consoleapp

import java.nio.file.Files
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopFileChooserTest {
    @Test
    fun directoryHistorySupportsBackForwardAndClearsForwardOnNewVisit() {
        val home = Files.createTempDirectory("consoleapp-history-")
        val documents = Files.createDirectory(home.resolve("documents"))
        val pictures = Files.createDirectory(home.resolve("pictures"))
        val downloads = Files.createDirectory(home.resolve("downloads"))
        try {
            assertTrue(Files.isDirectory(documents))
            assertEquals(documents, existingDirectoryOrAncestor(documents))
            assertEquals(null, existingDirectoryOrAncestor(java.nio.file.Path.of("documents")))
            val visited = DirectoryHistory(home).visit(documents).visit(pictures)
            assertEquals(pictures, visited.current)
            assertEquals(listOf(home, documents), visited.backStack)
            assertEquals(documents, visited.back().current)
            assertEquals(pictures, visited.back().forward().current)
            assertEquals(home, visited.back().back().current)
            assertEquals(visited, visited.forward())
            assertEquals(emptyList(), visited.back().visit(downloads).forwardStack)
        } finally {
            Files.deleteIfExists(downloads)
            Files.deleteIfExists(pictures)
            Files.deleteIfExists(documents)
            Files.deleteIfExists(home)
        }
    }

    @Test
    fun deletedHistoryPathFallsBackToItsExistingParent() {
        val home = Files.createTempDirectory("consoleapp-history-deleted-")
        val removed = Files.createDirectory(home.resolve("removed"))
        val current = Files.createDirectory(home.resolve("current"))
        try {
            val history = DirectoryHistory(home).visit(removed).visit(current)
            Files.delete(removed)
            val previous = history.back()
            assertEquals(home, previous.current)
            assertEquals(current, previous.forward().current)
            assertEquals(home, history.visit(home.resolve("missing").resolve("nested")).current)
        } finally {
            Files.deleteIfExists(current)
            Files.deleteIfExists(removed)
            Files.deleteIfExists(home)
        }
    }

    @Test
    fun folderSelectionModeHidesFilesAndMarksReturnedDirectory() {
        val folder = FileChooserEntry(java.nio.file.Path.of("Projects"), true, null)
        val file = FileChooserEntry(java.nio.file.Path.of("report.pdf"), false, 128, "application/pdf")
        val request = FilePickerRequest(selectionMode = FilePickerSelectionMode.Directories)

        assertTrue(folder.matches(request))
        assertFalse(file.matches(request))
        assertTrue(folder.toPickedFile().isDirectory)
        assertFalse(file.toPickedFile().isDirectory)
        assertEquals(0L, folder.toPickedFile().sizeBytes)
        assertTrue(describeFileSelection(request).contains("可選項目：資料夾"))
    }

    @Test
    fun mixedSelectionModeKeepsFilesAndFolders() {
        val folder = FileChooserEntry(java.nio.file.Path.of("Projects"), true, null)
        val file = FileChooserEntry(java.nio.file.Path.of("report.pdf"), false, 128, "application/pdf")
        val request = FilePickerRequest(selectionMode = FilePickerSelectionMode.FilesAndDirectories)

        assertTrue(folder.matches(request))
        assertTrue(file.matches(request))
    }


    @Test
    fun selectionConditionsExplainEveryRestriction() {
        val description = describeFileSelection(
            FilePickerRequest(
                allowMultiple = true,
                allowedExtensions = setOf("pdf", ".png"),
                allowedMimeTypes = setOf("application/pdf", "image/png"),
                minFileSizeBytes = 1,
                maxFileSizeBytes = 10L * 1024 * 1024,
            ),
        )
        assertTrue(description.contains("可選多個"))
        assertTrue(description.contains(".pdf"))
        assertTrue(description.contains(".png"))
        assertTrue(description.contains("application/pdf"))
        assertTrue(description.contains("image/png"))
        assertTrue(description.contains("1 B～10 MB"))
    }

    @Test
    fun dialogRemainsDefaultAndSeparateWindowCanBeRequested() {
        assertEquals(FilePickerPresentation.Dialog, FilePickerRequest().presentation)
        assertEquals(
            FilePickerPresentation.SeparateWindow,
            FilePickerRequest(presentation = FilePickerPresentation.SeparateWindow).presentation,
        )
    }

    @Test
    fun imagePreviewUsesFileContentsAndIsDownsampled() {
        val imageFile = Files.createTempFile("consoleapp-preview-", ".png")
        try {
            val original = BufferedImage(800, 400, BufferedImage.TYPE_INT_RGB)
            original.setRGB(0, 0, 0x00FF00)
            ImageIO.write(original, "png", imageFile.toFile())

            val entry = FileChooserEntry(imageFile, false, Files.size(imageFile), "image/png")
            assertTrue(entry.canPreviewImage)
            val preview = readImageThumbnail(imageFile.toFile(), maxEdgePixels = 100)
            assertTrue(preview != null)
            assertTrue(preview.width <= 100)
            assertTrue(preview.height <= 100)
        } finally {
            Files.deleteIfExists(imageFile)
        }
    }

    @Test
    fun nonImageFilesAreNotTreatedAsPreviews() {
        assertFalse(FileChooserEntry(java.nio.file.Path.of("report.pdf"), false, 42, "application/pdf").canPreviewImage)
    }

    @Test
    fun shortcutLocationsIncludeExistingBuiltInsAndCustomPathsOnce() {
        val home = Files.createTempDirectory("consoleapp-shortcuts-test-")
        val desktop = Files.createDirectory(home.resolve("Desktop"))
        val documents = Files.createDirectory(home.resolve("Documents"))
        val videos = Files.createDirectory(home.resolve("Videos"))
        val music = Files.createDirectory(home.resolve("Music"))
        val pictures = Files.createDirectory(home.resolve("Pictures"))
        val custom = Files.createDirectory(home.resolve("Projects"))

        try {
            val locations = buildShortcutLocations(
                home = home,
                desktop = desktop,
                documents = documents,
                roots = emptyList(),
                quickPaths = listOf(custom.toString(), custom.toString(), videos.toString(), "", "\u0000"),
            )
            assertEquals(
                listOf("桌面", "個人資料夾", "文件", "影片", "音樂", "圖片", "Projects"),
                locations.map { it.label },
            )
            assertEquals(custom, locations.last().path)
            assertEquals(ShortcutSection.Custom, locations.last().section)
        } finally {
            Files.deleteIfExists(custom)
            Files.deleteIfExists(pictures)
            Files.deleteIfExists(music)
            Files.deleteIfExists(videos)
            Files.deleteIfExists(documents)
            Files.deleteIfExists(desktop)
            Files.deleteIfExists(home)
        }
    }

    @Test
    fun foldersSortFirstAndExtensionFilterKeepsFolders() {
        val root = Files.createTempDirectory("consoleapp-chooser-test-")
        val folder = Files.createDirectory(root.resolve("Archive"))
        val pdf = Files.write(root.resolve("report.PDF"), byteArrayOf(1, 2, 3))
        val note = Files.writeString(root.resolve("notes.txt"), "notes")

        try {
            val entries = listDirectory(root)
            assertEquals(listOf("Archive", "notes.txt", "report.PDF"), entries.map { it.name })
            assertEquals(3L, entries.last().sizeBytes)

            val request = FilePickerRequest(allowedExtensions = setOf(".pdf"))
            assertTrue(entries[0].matches(request))
            assertFalse(entries[1].matches(request))
            assertTrue(entries[2].matches(request))
        } finally {
            Files.deleteIfExists(note)
            Files.deleteIfExists(pdf)
            Files.deleteIfExists(folder)
            Files.deleteIfExists(root)
        }
    }

    @Test
    fun mimeAndSizeFiltersApplyToFilesButKeepFoldersVisible() {
        val request = FilePickerRequest(
            allowedMimeTypes = setOf("image/*"),
            minFileSizeBytes = 100,
            maxFileSizeBytes = 1_000,
        )
        val folder = FileChooserEntry(java.nio.file.Path.of("Pictures"), true, null)
        val image = FileChooserEntry(java.nio.file.Path.of("photo.png"), false, 500, "image/png")
        val tooSmall = image.copy(sizeBytes = 50)
        val wrongType = image.copy(mimeType = "application/pdf")
        val unknownType = image.copy(mimeType = null)

        assertTrue(folder.matches(request))
        assertTrue(image.matches(request))
        assertFalse(tooSmall.matches(request))
        assertFalse(wrongType.matches(request))
        assertFalse(unknownType.matches(request))
    }
}
