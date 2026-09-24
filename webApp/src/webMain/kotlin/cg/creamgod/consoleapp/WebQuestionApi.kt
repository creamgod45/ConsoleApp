package cg.creamgod.consoleapp
import js.objects.unsafeJso
import web.form.FormData
import web.http.POST
import web.http.RequestInit
import web.http.RequestMethod
import web.http.fetch


class WebQuestionApi(
    private val endpoint: String,
    private val filePicker: BrowserFilePicker,
) {
    suspend fun submit(question: String, picked: PickedFile) {
        val file = filePicker.browserFileOf(picked)
            ?: error("找不到選取的檔案，請重新選取")

        val formData = FormData().apply {
            append("question", question)
            append("file", file, file.name)
        }


        val options = unsafeJso<RequestInit>().apply {
            method = RequestMethod.POST
            body = formData
        }

        val response = fetch(endpoint, options)
        check(response.ok) { "送出失敗：HTTP ${response.status}" }
    }
}
