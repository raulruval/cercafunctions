package com.cercafunctions.platform.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.provider.CalendarContract
import android.provider.CalendarContract.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.SdkSuppress
import com.cercafunctions.domain.calendar.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.UUID

/** All writes are instrumentation fixtures; the production adapter only reads. */
@SdkSuppress(minSdkVersion=29)
class CalendarProviderTest {
    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    private val resolver=instrumentation.targetContext.contentResolver
    private val calendarIds=mutableListOf<Long>()
    private val eventIds=mutableSetOf<Long>()
    private val zone=ZoneId.of("Europe/Madrid")
    private val day=CalendarDay(LocalDate.parse("2026-11-03"),zone)
    private val repo=AndroidCalendarRepository(resolver,{true})
    private val get=GetCalendarDay(repo)
    private val validate=RevalidateCalendarSelection(get)
    @Before fun setup(){instrumentation.uiAutomation.adoptShellPermissionIdentity(Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR)}
    @After fun cleanup(){
        try {calendarIds.forEach {resolver.delete(ContentUris.withAppendedId(Calendars.CONTENT_URI,it),null,null)}}
        finally {instrumentation.uiAutomation.dropShellPermissionIdentity()}
    }
    private fun calendar(label:String="POC calendario",visible:Int=1):Long {
        val account="cercafunctions-test-${UUID.randomUUID()}"
        val uri=Calendars.CONTENT_URI.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,"true")
            .appendQueryParameter(Calendars.ACCOUNT_NAME,account).appendQueryParameter(Calendars.ACCOUNT_TYPE,CalendarContract.ACCOUNT_TYPE_LOCAL).build()
        val values=ContentValues().apply {
            put(Calendars.ACCOUNT_NAME,account);put(Calendars.ACCOUNT_TYPE,CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(Calendars.NAME,account);put(Calendars.CALENDAR_DISPLAY_NAME,label);put(Calendars.CALENDAR_COLOR,0xff00796b.toInt())
            put(Calendars.CALENDAR_ACCESS_LEVEL,Calendars.CAL_ACCESS_OWNER);put(Calendars.OWNER_ACCOUNT,account)
            put(Calendars.CALENDAR_TIME_ZONE,zone.id);put(Calendars.VISIBLE,visible);put(Calendars.SYNC_EVENTS,1)
        }
        return ContentUris.parseId(requireNotNull(resolver.insert(uri,values))).also{calendarIds.add(it)}
    }
    private fun event(cal:Long,begin:Instant,title:String="POC reunión",allDay:Boolean=false,cancelled:Boolean=false,rrule:String?=null,exdate:String?=null,eventZone:ZoneId=zone):Long {
        val values=ContentValues().apply {
            put(Events.CALENDAR_ID,cal);put(Events.TITLE,title);put(Events.DTSTART,begin.toEpochMilli())
            put(Events.EVENT_TIMEZONE,if(allDay)"UTC" else eventZone.id);put(Events.ALL_DAY,if(allDay)1 else 0)
            put(Events.STATUS,if(cancelled)Events.STATUS_CANCELED else Events.STATUS_CONFIRMED)
            if(rrule==null)put(Events.DTEND,begin.plusSeconds(if(allDay)86400 else 3600).toEpochMilli())
            else {put(Events.RRULE,rrule);put(Events.DURATION,"PT1H")}
            if(exdate!=null)put(Events.EXDATE,exdate)
        }
        return ContentUris.parseId(requireNotNull(resolver.insert(Events.CONTENT_URI,values))).also{eventIds.add(it)}
    }
    private suspend fun owned(date:LocalDate=day.date,queryZone:ZoneId=zone):List<CalendarOccurrence> {
        val read=get(date,queryZone)
        assertTrue(read is CalendarRead.Data)
        return (read as CalendarRead.Data).occurrences.filter{it.key.eventId in eventIds}
    }
    @Test fun recurrenceUsesSelectedOccurrenceAndExdateIsRespected()=runBlocking {
        val cal=calendar()
        val original=day.date.minusDays(1).atTime(9,0).atZone(zone).toInstant()
        val recurring=event(cal,original,rrule="FREQ=DAILY;COUNT=3")
        val excluded=event(cal,original,rrule="FREQ=DAILY;COUNT=3",exdate=DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(day.date.atTime(9,0).atZone(zone).toInstant()))
        val rows=owned()
        assertEquals(listOf(recurring),rows.map{it.key.eventId})
        assertEquals(day.date.atTime(9,0).atZone(zone).toInstant(),rows.single().key.begin)
        assertNotEquals(original,rows.single().key.begin)
        assertFalse(rows.any{it.key.eventId==excluded})
    }
    @Test fun recurringLocalTimeIsExpandedAcrossSpringDstChange()=runBlocking {
        val cal=calendar()
        val original=LocalDate.of(2026,3,28).atTime(9,0).atZone(zone).toInstant()
        val id=event(cal,original,rrule="FREQ=DAILY;COUNT=3")
        val row=owned(LocalDate.of(2026,3,29)).single{it.key.eventId==id}
        assertEquals(Instant.parse("2026-03-29T07:00:00Z"),row.key.begin)
        assertEquals(LocalTime.of(9,0),row.key.begin.atZone(zone).toLocalTime())
    }
    @Test fun filtersUseBeginHalfOpenVisibleTimedAndNotCancelled()=runBlocking {
        val cal=calendar()
        val atStart=event(cal,day.start)
        event(cal,day.end);event(cal,day.start.minusSeconds(1800))
        event(cal,day.date.atStartOfDay(ZoneOffset.UTC).toInstant(),allDay=true)
        event(cal,day.start.plusSeconds(3600),cancelled=true)
        event(calendar(visible=0),day.start.plusSeconds(3600))
        assertEquals(listOf(atStart),owned().map{it.key.eventId})
    }
    @Test fun identicalTitlesAcrossCalendarsKeepDifferentKeysAndNames()=runBlocking {
        event(calendar("POC A"),day.start.plusSeconds(3600),"POC igual")
        event(calendar("POC B"),day.start.plusSeconds(3600),"POC igual")
        val rows=owned()
        assertEquals(2,rows.map{it.key}.toSet().size)
        assertEquals(setOf("POC A","POC B"),rows.map{it.calendarName}.toSet())
    }
    @Test fun zoneAndLocalDayUseInstantsInsteadOfOriginalCreationZone()=runBlocking {
        val cal=calendar()
        val early=event(cal,day.start.plusSeconds(1800))
        val ny=ZoneId.of("America/New_York")
        val nyBegin=day.date.atTime(9,0).atZone(ny).toInstant()
        val other=event(cal,nyBegin,eventZone=ny)
        assertEquals(nyBegin,owned().single{it.key.eventId==other}.key.begin)
        assertTrue(owned().any{it.key.eventId==early})
        assertFalse(owned(queryZone=ZoneId.of("America/Los_Angeles")).any{it.key.eventId==early})
    }
    @Test fun changedEndMovedAndDeletedOccurrencesAreRevalidated()=runBlocking {
        val cal=calendar()
        for(operation in 0..2) {
            val id=event(cal,day.start.plusSeconds((operation+1)*3600L))
            val row=owned().single{it.key.eventId==id}
            val selection=CalendarSelection(row.key,row.end,day)
            assertTrue(validate(selection) is SelectionValidation.Valid)
            val uri=ContentUris.withAppendedId(Events.CONTENT_URI,id)
            when(operation) {
                0 -> resolver.update(uri,ContentValues().apply{put(Events.DTSTART,row.key.begin.toEpochMilli());put(Events.DTEND,row.end.plusSeconds(60).toEpochMilli())},null,null)
                1 -> resolver.update(uri,ContentValues().apply{put(Events.DTSTART,row.key.begin.plusSeconds(60).toEpochMilli());put(Events.DTEND,row.end.plusSeconds(60).toEpochMilli())},null,null)
                else -> resolver.delete(uri,null,null)
            }
            assertEquals(SelectionValidation.MissingOrChanged,validate(selection))
        }
    }
}
