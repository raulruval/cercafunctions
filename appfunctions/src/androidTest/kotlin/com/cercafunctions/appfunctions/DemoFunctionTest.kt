package com.cercafunctions.appfunctions

import androidx.appfunctions.*
import androidx.test.filters.SdkSuppress
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.demo.*
import java.time.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

@SdkSuppress(minSdkVersion=36)
class DemoFunctionTest {
    private var saved=DemoSettings(agentAllowed=true)
    private var permitted=true
    private var calls=0
    private var duringRead:()->Unit={}
    private val begin=Instant.parse("2026-11-05T09:00:00Z")
    private var read:CalendarRead=CalendarRead.Data(listOf(CalendarOccurrence(OccurrenceKey(42,begin),begin.plusSeconds(3600),"PRIVATE TITLE","PRIVATE CALENDAR")))
    private val repository=object:DemoSettingsRepository {
        override suspend fun get()=saved
        override suspend fun setScenario(scenario:DemoScenario){saved=saved.copy(scenario=scenario)}
        override suspend fun setAgentAllowed(allowed:Boolean){saved=saved.copy(agentAllowed=allowed)}
    }
    private val calendar=object:CalendarRepository {
        override fun hasReadAccess()=permitted
        override suspend fun read(day:CalendarDay):CalendarRead {calls++;duringRead();return read}
    }
    private fun service()=JourneyAppFunctionService().also {
        it.demoSettings=repository
        it.demoEvaluation=EvaluateDemoJourney(GetCalendarDay(calendar),repository,
            Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"),ZoneOffset.UTC),ZoneOffset.UTC)
    }
    @Test fun noParametersUseLiveMeetingAndPersistedScenario()=runBlocking {
        val service=service()
        val late=service.evaluateDemoJourney()
        assertEquals("LATE",late.status);assertEquals(-15L,late.signedMarginMinutes)
        assertEquals(begin,late.meetingStart);assertEquals("ANDROID_CALENDAR",late.meetingSource)
        assertFalse(late.toString().contains("PRIVATE"));assertEquals("FIXTURE",late.journeySource)
        saved=saved.copy(scenario=DemoScenario.UNKNOWN)
        val unknown=service.evaluateDemoJourney()
        assertEquals("INDETERMINATE",unknown.status);assertNull(unknown.arrival);assertNull(unknown.signedMarginMinutes)
        assertEquals(2,calls)
    }
    @Test fun optInAndPermissionAreBothRequiredBeforeReading()=runBlocking {
        saved=saved.copy(agentAllowed=false)
        try{service().evaluateDemoJourney();fail()}catch(_:AppFunctionDeniedException){}
        saved=saved.copy(agentAllowed=true);permitted=false
        try{service().evaluateDemoJourney();fail()}catch(_:AppFunctionDeniedException){}
        assertEquals(0,calls)
    }
    @Test fun revocationDuringReadPreventsDisclosure()=runBlocking {
        duringRead={saved=saved.copy(agentAllowed=false)}
        try{service().evaluateDemoJourney();fail()}catch(_:AppFunctionDeniedException){}
        saved=saved.copy(agentAllowed=true);duringRead={permitted=false}
        try{service().evaluateDemoJourney();fail()}catch(_:AppFunctionDeniedException){}
    }
    @Test fun noMeetingAndProviderFailureGiveBoundedErrors()=runBlocking {
        read=CalendarRead.Data(emptyList())
        try{service().evaluateDemoJourney();fail()}catch(_:AppFunctionElementNotFoundException){}
        duringRead={error("PRIVATE PATH")}
        try{service().evaluateDemoJourney();fail()}catch(e:AppFunctionAppUnknownException){assertFalse(e.errorMessage.orEmpty().contains("PRIVATE"))}
    }
    @Test fun cancellationPropagates()=runBlocking {
        duringRead={throw CancellationException("cancelled")}
        try{service().evaluateDemoJourney();fail()}catch(_:CancellationException){}
    }
}
