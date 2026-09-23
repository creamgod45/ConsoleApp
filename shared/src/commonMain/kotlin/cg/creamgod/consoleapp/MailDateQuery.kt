package cg.creamgod.consoleapp

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

fun buildMailQuery(
    filter: MailFilterDate,
    rangeStartMillis: Long? = null,
    rangeEndMillis: Long? = null,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): MailQuery {
    val today = now.toLocalDateTime(timeZone).date

    return when (filter) {
        MailFilterDate.All -> MailQuery()
        MailFilterDate.Today -> MailQuery(
            receivedFrom = today.atStartOfDayIn(timeZone),
            receivedTo = now,
        )
        MailFilterDate.Yesterday -> {
            val yesterday = today.plus(-1, DateTimeUnit.DAY)
            MailQuery(
                receivedFrom = yesterday.atStartOfDayIn(timeZone),
                receivedTo = today.atStartOfDayIn(timeZone) - 1.milliseconds,
            )
        }
        MailFilterDate.Spec -> {
            if (rangeStartMillis == null || rangeEndMillis == null) return MailQuery()

            // Material DatePicker stores a selected calendar date as midnight UTC. Read the
            // calendar components in UTC, then interpret that date in the user's time zone.
            val startDate = Instant.fromEpochMilliseconds(rangeStartMillis)
                .toLocalDateTime(TimeZone.UTC)
                .date
            val endDate = Instant.fromEpochMilliseconds(rangeEndMillis)
                .toLocalDateTime(TimeZone.UTC)
                .date
            val receivedFrom = startDate.atStartOfDayIn(timeZone)
            val requestedEnd = endDate
                .plus(1, DateTimeUnit.DAY)
                .atStartOfDayIn(timeZone) - 1.milliseconds
            val receivedTo = minOf(requestedEnd, now)

            require(receivedFrom <= receivedTo) {
                "收件時間起點不可晚於目前時間"
            }
            MailQuery(receivedFrom = receivedFrom, receivedTo = receivedTo)
        }
    }
}
