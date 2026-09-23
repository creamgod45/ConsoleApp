package cg.creamgod.consoleapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.util.concurrent.TimeUnit

class DesktopMailApi(
    private val endpoint: String,
    private val lazy: Boolean = true,
    private val limit: Int = 500,
) {
    private val client = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun load(query: MailQuery = MailQuery()): List<MailMessage> = withContext(Dispatchers.IO) {
        require(limit in 1..500) { "limit 必須介於 1 到 500" }
        val urlBuilder = endpoint.toHttpUrl().newBuilder()
            .addQueryParameter("lazy", lazy.toString())
            .addQueryParameter("limit", limit.toString())
        query.receivedFrom?.let { urlBuilder.addQueryParameter("received_from", it.toString()) }
        query.receivedTo?.let { urlBuilder.addQueryParameter("received_to", it.toString()) }
        val url = urlBuilder.build()
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).execute().use { response ->
            val responseText = response.body.string()
            check(response.isSuccessful) {
                "讀取郵件失敗：HTTP ${response.code}: $responseText"
            }
            parseMailMessages(responseText)
        }
    }
}
