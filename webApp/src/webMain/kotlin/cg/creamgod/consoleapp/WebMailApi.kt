package cg.creamgod.consoleapp

import web.http.fetch
import web.http.text

class WebMailApi(
    private val endpoint: String,
    private val lazy: Boolean = true,
    private val limit: Int = 500,
) {
    suspend fun load(query: MailQuery = MailQuery()): List<MailMessage> {
        require(limit in 1..500) { "limit 必須介於 1 到 500" }
        val separator = if ('?' in endpoint) '&' else '?'
        val queryParameters = buildList {
            add("lazy=$lazy")
            add("limit=$limit")
            query.receivedFrom?.let { add("received_from=$it") }
            query.receivedTo?.let { add("received_to=$it") }
        }.joinToString("&")
        val requestUrl = "$endpoint$separator$queryParameters"
        val response = fetch(requestUrl)
        val responseText = response.text()
        check(response.ok) {
            "讀取郵件失敗：HTTP ${response.status}: $responseText"
        }
        return parseMailMessages(responseText)
    }
}
