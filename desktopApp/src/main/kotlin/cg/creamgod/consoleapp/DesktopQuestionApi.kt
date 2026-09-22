package cg.creamgod.consoleapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class DesktopQuestionApi(private val endpoint: String) {

    private val client = OkHttpClient.Builder()
        .callTimeout(2, TimeUnit.MINUTES)
        .build()

    suspend fun submit(question: String, picked: PickedFile) =
        withContext(Dispatchers.IO) {
            require(!picked.isDirectory) { "附件必須是檔案" }
            val path = Path.of(picked.path)
            require(Files.isRegularFile(path)) { "附件已不存在" }

            val mime = Files.probeContentType(path)?.toMediaTypeOrNull()
            val form = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("question", question)
                .addFormDataPart("file", path.fileName.toString(), path.toFile().asRequestBody(mime))
                .build()

            val request = Request.Builder()
                .url(endpoint)
                .post(form)
                .build()

            client.newCall(request).execute().use { response ->
                val responseText = response.body.string().orEmpty()
                check(response.isSuccessful) {
                    "伺服器回傳 HTTP ${response.code}: $responseText"
                }
            }
        }
}
