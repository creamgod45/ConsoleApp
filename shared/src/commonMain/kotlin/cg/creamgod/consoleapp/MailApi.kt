package cg.creamgod.consoleapp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.time.Instant

data class MailQuery(
    val receivedFrom: Instant? = null,
    val receivedTo: Instant? = null,
)

data class MailAttachment(
    val id: String,
    val filename: String,
    val contentType: String,
    val sizeBytes: Long,
    val content: String,
    val isInline: Boolean,
)

data class MailMessage(
    val id: Int,
    val name: String,
    val email: String,
    val receivedAt: String,
    val content: String,
    val attachments: List<MailAttachment>,
)

/** Parses the FastAPI `/api/faker/mail` response without coupling the DTO to a serializer plugin. */
fun parseMailMessages(payload: String): List<MailMessage> {
    val root = Json.parseToJsonElement(payload)
    require(root is JsonArray) { "郵件 API 回應必須是 JSON array" }

    return root.mapIndexed { index, element ->
        val item = element.jsonObject
        val id = item["id"]?.jsonPrimitive?.intOrNull
            ?: error("第 ${index + 1} 筆郵件缺少有效的 id")
        val name = item["name"]?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotBlank() }
            ?: error("第 ${index + 1} 筆郵件缺少 name")
        val email = item["email"]?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotBlank() }
            ?: error("第 ${index + 1} 筆郵件缺少 email")
        val receivedAt = item["received_at"]?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotBlank() }
            ?: error("第 ${index + 1} 筆郵件缺少 received_at")
        val content = item["content"]?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotBlank() }
            ?: error("第 ${index + 1} 筆郵件缺少 content")
        val attachments = item["attachments"]?.jsonArray?.mapIndexed { attachmentIndex, attachmentElement ->
            val attachment = attachmentElement.jsonObject
            fun requiredText(field: String): String =
                attachment[field]?.jsonPrimitive?.contentOrNull
                    ?.takeIf { it.isNotBlank() }
                    ?: error("第 ${index + 1} 筆郵件的第 ${attachmentIndex + 1} 個附件缺少 $field")

            MailAttachment(
                id = requiredText("id"),
                filename = requiredText("filename"),
                contentType = requiredText("content_type"),
                sizeBytes = attachment["size_bytes"]?.jsonPrimitive?.longOrNull
                    ?: error("第 ${index + 1} 筆郵件的第 ${attachmentIndex + 1} 個附件缺少 size_bytes"),
                content = attachment["content"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                isInline = attachment["is_inline"]?.jsonPrimitive?.booleanOrNull ?: false,
            )
        }.orEmpty()

        MailMessage(
            id = id,
            name = name,
            email = email,
            receivedAt = receivedAt,
            content = content,
            attachments = attachments,
        )
    }
}
