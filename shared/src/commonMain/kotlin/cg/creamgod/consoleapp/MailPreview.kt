package cg.creamgod.consoleapp

data class MailPreview(
    val id: Int,
    val sender: String,
    val subject: String,
    val preview: String,
    val time: String,
    val unread: Boolean,
    val starred: Boolean,
    val content: String,
    val attachments: List<MailAttachment>,
)
