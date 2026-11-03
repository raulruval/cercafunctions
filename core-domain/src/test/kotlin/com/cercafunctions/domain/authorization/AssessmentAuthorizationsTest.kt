package com.cercafunctions.domain.authorization
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.assessment.*
import com.cercafunctions.domain.journey.*
import java.time.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
class AssessmentAuthorizationsTest {
    private var time=GrantTime(Instant.parse("2026-11-03T08:00:00Z"),1_000_000,4)
    private val clock=object:Clock(){override fun instant()=time.wall;override fun getZone():ZoneId=ZoneOffset.UTC;override fun withZone(zone:ZoneId):Clock=this}
    private var access=true
    private var calls=0
    private var fail=false
    private var cancel=false
    private var revokeOnCalendarCall:Int?=null
    private var revokeDuringArrival=false
    private var event=CalendarOccurrence(OccurrenceKey(5,Instant.parse("2026-11-04T09:00:00Z")),Instant.parse("2026-11-04T10:00:00Z"),"private-title","private-calendar")
    private var rows:List<CalendarOccurrence>?=null
    private val repo=object:GrantRepository {
        override val available=true
        var current:GrantRecord?=null
        override suspend fun read()=current
        override suspend fun publish(grant:GrantRecord){current=grant}
        override suspend fun revokeIf(referenceHash:String){if(current?.referenceHash==referenceHash)current=null}
        override suspend fun revokeAll(){current=null}
    }
    private val calendar=GetCalendarDay(object:CalendarRepository {
        override fun hasReadAccess()=access
        override suspend fun read(day:CalendarDay):CalendarRead {
            calls++;if(revokeOnCalendarCall==calls)repo.revokeAll();if(cancel)throw CancellationException("cancel")
            return if(fail)CalendarRead.Unavailable(CalendarFailure.QUERY_FAILED) else CalendarRead.Data(rows ?: listOf(event))
        }
    })
    private val validate=RevalidateCalendarSelection(calendar)
    private fun authorizations()=AssessmentAuthorizations(repo,validate,calendar,GrantClock{time})
    private val grants=authorizations()
    private fun target()=GrantTarget(CalendarSelection(event.key,event.end,CalendarDay(event.key.begin.atZone(ZoneOffset.UTC).toLocalDate(),ZoneOffset.UTC)),event)
    private suspend fun denied(block:suspend()->Any?) {try{block();fail("must deny")}catch(_:AssessmentAccessDenied){}}
    private val query=QueryJourneyArrival(object:JourneyRepository {
        override suspend fun getHabitualJourney():JourneyReadResult {
            if(revokeDuringArrival)repo.revokeAll()
            return JourneyReadResult.Configured(HabitualJourney("secret-origin","secret-destination",35))
        }
        override suspend fun saveHabitualJourney(journey:HabitualJourney)=error("readonly")
    },JourneyFixtureRepository{scenario->if(scenario==ArrivalScenario.REFERENCE)null else JourneyFixture(HabitualJourney("DemoA","DemoB",35),
        DepartureInput(LocalDateTime.parse("2026-11-02T08:00"),ZoneId.of("Europe/Madrid")),Instant.parse("2026-11-01T08:00:00Z"),null)},clock)
    private val assess=AuthorizedAssessment(grants,query,AssessArrival(query,validate,clock))
    @Test fun `issue persists no raw token or calendar text and resolves after recreation`()=runTest {
        val issued=grants.issue(target());assertTrue(Regex("[A-Za-z0-9_-]{43}").matches(issued.reference))
        assertEquals(time.wall.plusSeconds(1800),issued.expiresAt)
        assertFalse(repo.current.toString().contains(issued.reference));assertFalse(repo.current.toString().contains("private-title"))
        assertFalse(repo.current.toString().contains("private-calendar"))
        assertEquals(repo.current,authorizations().resolve(issued.reference))
    }
    @Test fun `expiry is capped at occurrence start with exact bound rejected`()=runTest {
        event=event.copy(key=OccurrenceKey(5,time.wall.plusSeconds(60)),end=time.wall.plusSeconds(3600))
        val issued=grants.issue(target());assertEquals(event.key.begin,issued.expiresAt)
        time=time.copy(wall=time.wall.plusSeconds(60),elapsedMillis=time.elapsedMillis+60_000)
        denied{grants.resolve(issued.reference)};assertNull(repo.current)
    }
    @Test fun `thirty minute bound rejects without reading agenda`()=runTest {
        val ref=grants.issue(target()).reference;val before=calls
        time=time.copy(wall=time.wall.plusSeconds(1800),elapsedMillis=time.elapsedMillis+1_800_000)
        denied{grants.resolve(ref)};assertEquals(before,calls)
    }
    @Test fun `past occurrence cannot be authorized`()=runTest {
        event=event.copy(key=OccurrenceKey(5,time.wall),end=time.wall.plusSeconds(10))
        denied{grants.issue(target())};assertNull(repo.current)
    }
    @Test fun `unknown malformed and rotated tokens cannot access calendar`()=runTest {
        val first=grants.issue(target()).reference;val second=grants.issue(target()).reference;val before=calls
        denied{grants.resolve(first)};denied{grants.resolve("invalid")};denied{grants.resolve("A".repeat(43))}
        assertEquals(before,calls);assertNotNull(grants.resolve(second))
    }
    @Test fun `revocation remains denied and old invalidation cannot remove replacement`()=runTest {
        val old=grants.issue(target()).reference;val current=grants.issue(target()).reference
        grants.invalidate(old);assertNotNull(grants.resolve(current));grants.revoke();denied{grants.resolve(current)}
    }
    @Test fun `reboot elapsed rollback and wall drift invalidate`()=runTest {
        val original=time
        for(bad in listOf(original.copy(bootCount=5),original.copy(elapsedMillis=1),original.copy(wall=original.wall.minusSeconds(1)),original.copy(wall=original.wall.plusSeconds(20)))) {
            time=original;val ref=grants.issue(target()).reference;time=bad
            denied{grants.resolve(ref)};assertNull(repo.current)
        }
    }
    @Test fun `small clock correction never extends monotonic deadline`()=runTest {
        val ref=grants.issue(target()).reference
        time=time.copy(wall=time.wall.plusSeconds(1796),elapsedMillis=time.elapsedMillis+1_800_000)
        denied{grants.resolve(ref)}
    }
    @Test fun `changed title or moved removed occurrence invalidates durably`()=runTest {
        val original=event
        for(change in listOf(original.copy(title="changed"),original.copy(key=OccurrenceKey(5,original.key.begin.plusSeconds(1))),null)) {
            event=original;rows=null;val ref=grants.issue(target()).reference
            rows=if(change==null)emptyList() else listOf(change)
            denied{grants.resolve(ref)};assertNull(repo.current)
        }
    }
    @Test fun `issue requires same details that user saw`()=runTest {
        val old=target();event=event.copy(title="changed before click")
        denied{grants.issue(old)};assertNull(repo.current)
    }
    @Test fun `permission or provider loss revokes and reconcession does not restore token`()=runTest {
        val ref=grants.issue(target()).reference;access=false;denied{grants.resolve(ref)}
        access=true;denied{grants.resolve(ref)}
        val other=grants.issue(target()).reference;fail=true;denied{grants.resolve(other)};assertNull(repo.current)
    }
    @Test fun `authorized result matches shared assessment for early equal late`()=runTest {
        val ref=grants.issue(target()).reference
        for((hour,minutes) in listOf("08:15" to 10L,"08:25" to 0L,"08:40" to -15L)) {
            val result=assess(ref,DepartureInput(LocalDateTime.parse("2026-11-04T$hour"),ZoneOffset.UTC)) as AssessmentResult.Compared
            assertEquals(minutes,result.value.signedMarginMinutes)
        }
    }
    @Test fun `unknown arrival stays indeterminate and revocation during query denies`()=runTest {
        val ref=grants.issue(target()).reference;val input=DepartureInput(LocalDateTime.parse("2026-11-04T08:40"),ZoneOffset.UTC)
        assertEquals(AssessmentReason.UNKNOWN_ARRIVAL,(assess(ref,input,ArrivalScenario.DEMO_UNKNOWN) as AssessmentResult.Indeterminate).reason)
        revokeDuringArrival=true;denied{assess(ref,input)}
    }
    @Test fun `status after process recreation can revoke without retaining token`()=runTest {
        val issued=grants.issue(target());assertEquals(issued.expiresAt,authorizations().status()?.expiresAt)
        authorizations().revoke();assertNull(grants.status())
    }
    @Test fun `revocation during final calendar validation cannot publish old authorization`()=runTest {
        val ref=grants.issue(target()).reference
        revokeOnCalendarCall=calls+3
        denied{assess(ref,DepartureInput(LocalDateTime.parse("2026-11-04T08:40"),ZoneOffset.UTC))}
        assertNull(repo.current)
    }
    @Test fun `cancellation is never replaced by authorization error`()=runTest {
        val ref=grants.issue(target()).reference;cancel=true
        try{grants.resolve(ref);fail()}catch(_:CancellationException){}
    }
}
