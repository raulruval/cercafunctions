package com.cercafunctions.domain.calendar

import java.time.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class CalendarUseCasesTest {
    private val madrid=ZoneId.of("Europe/Madrid")
    private val date=LocalDate.parse("2026-11-03")
    private val day=CalendarDay(date,madrid)
    private var access=true
    private var calls=0
    private var cancel=false
    private var result:CalendarRead=CalendarRead.Data(emptyList())
    private val repo=object:CalendarRepository {
        override fun hasReadAccess()=access
        override suspend fun read(day:CalendarDay):CalendarRead {calls++;if(cancel)throw CancellationException("injected");return result}
    }
    private val get=GetCalendarDay(repo)
    private val validate=RevalidateCalendarSelection(get)
    private fun event(id:Long=1,begin:Instant=day.start.plusSeconds(9*3600),title:String="Mismo título") =
        CalendarOccurrence(OccurrenceKey(id,begin),begin.plusSeconds(3600),title,"POC")

    @Test fun `local day has 23 or 25 elapsed hours at DST changes`() {
        assertEquals(23,Duration.between(CalendarDay(LocalDate.parse("2026-03-29"),madrid).start,CalendarDay(LocalDate.parse("2026-03-29"),madrid).end).toHours())
        assertEquals(25,Duration.between(CalendarDay(LocalDate.parse("2026-10-25"),madrid).start,CalendarDay(LocalDate.parse("2026-10-25"),madrid).end).toHours())
    }
    @Test fun `zone changes the actual interval and skipped date can be empty`() {
        assertNotEquals(day.start,CalendarDay(date,ZoneOffset.UTC).start)
        val skipped=CalendarDay(LocalDate.parse("2011-12-30"),ZoneId.of("Pacific/Apia"))
        assertEquals(skipped.start,skipped.end)
    }
    @Test fun `permission denied prevents repository access`()=runTest {
        access=false
        assertEquals(CalendarRead.PermissionRequired,get(date,madrid));assertEquals(0,calls)
    }
    @Test fun `only begins inside half open interval and stable sorted identities survive`()=runTest {
        val start=event(1,day.start);val end=event(2,day.end);val earlier=event(3,day.start.minusSeconds(10));val later=event(4,day.start.plusSeconds(200))
        result=CalendarRead.Data(listOf(later,end,earlier,start))
        assertEquals(CalendarRead.Data(listOf(start,later)),get(date,madrid))
    }
    @Test fun `same title and recurring event id never replace occurrence identity`()=runTest {
        val one=event();val two=event(2);val recurrence=event(1,one.key.begin.plusSeconds(3600))
        result=CalendarRead.Data(listOf(two,recurrence,one))
        val data=get(date,madrid) as CalendarRead.Data
        assertEquals(3,data.occurrences.size)
        assertEquals(3,data.occurrences.map{it.key}.toSet().size)
    }
    @Test fun `duplicate identity is unavailable not an arbitrary selection`()=runTest {
        val one=event();result=CalendarRead.Data(listOf(one,one.copy(title="Otro")))
        assertEquals(CalendarRead.Unavailable(CalendarFailure.INVALID_DATA),get(date,madrid))
    }
    @Test fun `empty remains different from provider failure`()=runTest {
        assertEquals(CalendarRead.Data(emptyList<CalendarOccurrence>()),get(date,madrid))
        result=CalendarRead.Unavailable(CalendarFailure.PROVIDER_UNAVAILABLE)
        assertEquals(result,get(date,madrid))
    }
    @Test fun `revalidation returns current local details for exact selection`()=runTest {
        val one=event();val selection=CalendarSelection(one.key,one.end,day)
        val current=one.copy(title="Título actualizado")
        result=CalendarRead.Data(listOf(current,event(2)))
        assertEquals(SelectionValidation.Valid(current),validate(selection))
    }
    @Test fun `deleted moved or end changed selection cannot silently choose another event`()=runTest {
        val one=event();val selection=CalendarSelection(one.key,one.end,day)
        for(items in listOf(emptyList(),listOf(event(2)),listOf(event(1,one.key.begin.plusSeconds(60))),listOf(one.copy(end=one.end.plusSeconds(60))))) {
            result=CalendarRead.Data(items)
            assertEquals(SelectionValidation.MissingOrChanged,validate(selection))
        }
    }
    @Test fun `revalidation propagates revoked permission and provider failures`()=runTest {
        val one=event();val selection=CalendarSelection(one.key,one.end,day)
        access=false;assertEquals(SelectionValidation.PermissionRequired,validate(selection))
        access=true;result=CalendarRead.Unavailable(CalendarFailure.QUERY_FAILED)
        assertEquals(SelectionValidation.Unavailable(CalendarFailure.QUERY_FAILED),validate(selection))
    }
    @Test fun `invalid model bounds and selection outside day are rejected`() {
        try {OccurrenceKey(0,day.start);fail("invalid id")}catch(_:IllegalArgumentException){}
        try {CalendarOccurrence(OccurrenceKey(1,day.start),day.start.minusSeconds(1),"","");fail("invalid end")}catch(_:IllegalArgumentException){}
        try {CalendarSelection(OccurrenceKey(1,day.end),day.end,day);fail("outside day")}catch(_:IllegalArgumentException){}
    }
    @Test fun `cancellation propagates`()=runTest {
        cancel=true
        try {get(date,madrid);fail("cancellation")}catch(_:CancellationException){}
    }
}
