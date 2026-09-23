package cg.creamgod.consoleapp

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class MailDateQueryTest {
    private val now = Instant.parse("2026-09-23T12:34:56Z")
    private val utc = TimeZone.UTC

    @Test
    fun allDoesNotSendDateParameters() {
        assertEquals(MailQuery(), buildMailQuery(MailFilterDate.All, now = now, timeZone = utc))
    }

    @Test
    fun todayUsesStartOfDayThroughNow() {
        val query = buildMailQuery(MailFilterDate.Today, now = now, timeZone = utc)

        assertEquals(Instant.parse("2026-09-23T00:00:00Z"), query.receivedFrom)
        assertEquals(now, query.receivedTo)
    }

    @Test
    fun yesterdayIncludesTheWholePreviousDay() {
        val query = buildMailQuery(MailFilterDate.Yesterday, now = now, timeZone = utc)

        assertEquals(Instant.parse("2026-09-22T00:00:00Z"), query.receivedFrom)
        assertEquals(Instant.parse("2026-09-22T23:59:59.999Z"), query.receivedTo)
    }

    @Test
    fun customRangeIncludesTheSelectedEndDate() {
        val start = Instant.parse("2026-09-20T00:00:00Z").toEpochMilliseconds()
        val end = Instant.parse("2026-09-22T00:00:00Z").toEpochMilliseconds()
        val query = buildMailQuery(
            filter = MailFilterDate.Spec,
            rangeStartMillis = start,
            rangeEndMillis = end,
            now = now,
            timeZone = utc,
        )

        assertEquals(Instant.parse("2026-09-20T00:00:00Z"), query.receivedFrom)
        assertEquals(Instant.parse("2026-09-22T23:59:59.999Z"), query.receivedTo)
    }
}
