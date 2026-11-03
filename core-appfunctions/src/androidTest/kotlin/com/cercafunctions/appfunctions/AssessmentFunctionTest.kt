package com.cercafunctions.appfunctions
import androidx.appfunctions.*
import androidx.test.filters.SdkSuppress
import com.cercafunctions.domain.authorization.*
import com.cercafunctions.domain.assessment.*
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.*
import com.cercafunctions.domain.sharing.JourneySharingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.junit.Assert.*
import java.time.*
@SdkSuppress(minSdkVersion=36)
class AssessmentFunctionTest {
    private val now=Instant.parse("2026-11-03T08:00:00Z")
    private val clock=Clock.fixed(now,ZoneOffset.UTC)
    private var access=true
    private var cancel=false
    private var event=CalendarOccurrence(OccurrenceKey(44,Instant.parse("2026-11-04T09:00:00Z")),Instant.parse("2026-11-04T10:00:00Z"),"private-title","private-calendar")
    private val repository=object:GrantRepository {
        override val available=true;var grant:GrantRecord?=null
        override suspend fun read()=grant
        override suspend fun publish(grant:GrantRecord){this.grant=grant}
        override suspend fun revokeAll(){grant=null}
        override suspend fun revokeIf(referenceHash:String){if(grant?.referenceHash==referenceHash)grant=null}
    }
    private val calendar=GetCalendarDay(object:CalendarRepository {
        override fun hasReadAccess()=access
        override suspend fun read(day:CalendarDay):CalendarRead {if(cancel)throw CancellationException("cancel");return CalendarRead.Data(listOf(event))}
    })
    private val validation=RevalidateCalendarSelection(calendar)
    private val grants=AssessmentAuthorizations(repository,validation,calendar,GrantClock{GrantTime(now,1000,2)})
    private val query=QueryJourneyArrival(object:JourneyRepository {
        override suspend fun getHabitualJourney()=JourneyReadResult.Configured(HabitualJourney("private-origin","private-destination",35))
        override suspend fun saveHabitualJourney(journey:HabitualJourney)=error("readonly")
    },JourneyFixtureRepository{scenario->if(scenario==ArrivalScenario.REFERENCE)null else JourneyFixture(HabitualJourney("demo-origin","demo-destination",35),
        DepartureInput(LocalDateTime.parse("2026-11-02T08:00"),ZoneOffset.UTC),Instant.parse("2026-11-01T08:00:00Z"),null)},clock)
    private val input=LocalDateTime.parse("2026-11-04T08:40")
    private suspend fun reference()=grants.issue(GrantTarget(CalendarSelection(event.key,event.end,CalendarDay(LocalDate.parse("2026-11-04"),ZoneOffset.UTC)),event)).reference
    private fun service()=JourneyAppFunctionService().also{
        it.authorizedAssessment=AuthorizedAssessment(grants,query,AssessArrival(query,validation,clock))
        it.sharing=object:JourneySharingRepository {override val available=true;override val allowed=flowOf(false);override suspend fun setAllowed(value:Boolean)=Unit}
    }
    @Test fun minimalDtoMatchesDomainAndDoesNotRequireJourneyOptIn()=runBlocking {
        val ref=reference();val result=service().evaluateAuthorizedCommitment(ref,input,ZoneOffset.UTC)
        assertEquals("LATE",result.status);assertEquals(-15L,result.signedMarginMinutes);assertEquals(event.key.begin,result.meetingStart)
        assertEquals("REFERENCE",result.source)
        for(secret in listOf("private-title","private-calendar","private-origin","private-destination",ref))assertFalse(result.toString().contains(secret))
        val names=AssessmentResponse::class.java.declaredFields.map{it.name}
        assertTrue(names.none{it in setOf("origin","destination","title","calendarName","eventId","reference","attendees","email","description")})
    }
    @Test fun unknownArrivalDisclosesNoMeetingTiming()=runBlocking {
        val result=service().evaluateAuthorizedCommitment(reference(),input,ZoneOffset.UTC,"DEMO_UNKNOWN")
        assertEquals("INDETERMINATE",result.status);assertNull(result.meetingStart);assertNull(result.meetingZone);assertNull(result.signedMarginMinutes)
        assertEquals("FIXTURE",result.source);assertNull(result.arrival)
    }
    @Test fun unknownAndRevokedReferencesHaveUniformBoundedError()=runBlocking {
        val ref=reference();grants.revoke();val messages=mutableSetOf<String?>()
        for(candidate in listOf(ref,"A".repeat(43),"bad")) {
            try{service().evaluateAuthorizedCommitment(candidate,input,ZoneOffset.UTC);fail()}catch(e:AppFunctionDeniedException){messages+=e.errorMessage}
        }
        assertEquals(1,messages.size);assertFalse(messages.first().orEmpty().contains(ref))
    }
    @Test fun permissionLossOrTitleChangeDeniesAndInvalidates()=runBlocking {
        val ref=reference();access=false
        try{service().evaluateAuthorizedCommitment(ref,input,ZoneOffset.UTC);fail()}catch(_:AppFunctionDeniedException){}
        assertNull(repository.grant);access=true
        val next=reference();event=event.copy(title="other")
        try{service().evaluateAuthorizedCommitment(next,input,ZoneOffset.UTC);fail()}catch(_:AppFunctionDeniedException){}
        assertNull(repository.grant)
    }
    @Test fun invalidOptionsReturnNoComparison()=runBlocking {
        val ref=reference()
        try{service().evaluateAuthorizedCommitment(ref,input,ZoneOffset.UTC,"bad");fail()}catch(_:AppFunctionInvalidArgumentException){}
        try{service().evaluateAuthorizedCommitment(ref,input,ZoneOffset.UTC,utcOffset="invalid");fail()}catch(_:AppFunctionInvalidArgumentException){}
    }
    @Test fun cancellationRemainsCancellation()=runBlocking {
        val ref=reference();cancel=true
        try{service().evaluateAuthorizedCommitment(ref,input,ZoneOffset.UTC);fail()}catch(_:CancellationException){}
    }
}
