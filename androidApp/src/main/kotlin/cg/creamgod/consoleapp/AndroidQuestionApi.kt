package cg.creamgod.consoleapp

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.util.concurrent.TimeUnit

class AndroidQuestionApi(
    private val endpoint: String,
    private val contentResolver: ContentResolver,
) {
    private val client = OkHttpClient.Builder()
        .callTimeout(2, TimeUnit.MINUTES)
        .build()

    suspend fun submit(question: String, picked: PickedFile) = withContext(Dispatchers.IO) {
        require(!picked.isDirectory) { "附件必須是檔案" }

        val uri = Uri.parse(picked.path)
        val mediaType = contentResolver.getType(uri)?.toMediaTypeOrNull()
        val fileBody = ContentUriRequestBody(
            contentResolver = contentResolver,
            uri = uri,
            mediaType = mediaType,
            sizeBytes = picked.sizeBytes,
        )
        val form = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("question", question)
            .addFormDataPart("file", picked.name, fileBody)
            .build()
        val request = Request.Builder()
            .url(endpoint)
            .post(form)
            .build()

        client.newCall(request).execute().use { response ->
            val responseText = response.body.string()
            check(response.isSuccessful) {
                "伺服器回傳 HTTP ${response.code}: $responseText"
            }
        }
    }
}

private class ContentUriRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    private val mediaType: MediaType?,
    private val sizeBytes: Long,
) : RequestBody() {
    override fun contentType(): MediaType? = mediaType

    override fun contentLength(): Long = sizeBytes.takeIf { it > 0L } ?: -1L

    override fun writeTo(sink: BufferedSink) {
        val input = contentResolver.openInputStream(uri)
            ?: error("無法讀取選取的附件")
        input.use { sink.writeAll(it.source()) }
    }
}
