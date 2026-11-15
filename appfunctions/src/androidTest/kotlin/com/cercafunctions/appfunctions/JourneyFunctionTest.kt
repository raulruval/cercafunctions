package com.cercafunctions.appfunctions
import androidx.appfunctions.*
import androidx.test.filters.SdkSuppress
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.rail.*
import com.cercafunctions.domain.journey.*
import com.cercafunctions.domain.sharing.JourneySharingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test
import java.time.*

@SdkSuppress(minSdkVersion=36)
class JourneyFunctionTest {
    private val clock=Clock.fixed(Instant.parse("2026-11-01T08:00:00Z"),ZoneOffset.UTC)
    private val zone=ZoneId.of("Europe/Madrid")
    private val time=LocalDateTime.parse("2026-11-02T08:00")
    private var configured=true
    private var fail=false
    private var calls=0
    private var feeds=0
    private var revoke=false
    private var cancel=false
    private val allowed=MutableStateFlow(true)
    private val consent=object:JourneySharingRepository {
        override val available=true
        override val allowed=this@JourneyFunctionTest.allowed
        override suspend fun setAllowed(value:Boolean){allowed.value=value}
    }
    private val query=QueryJourneyArrival(object:JourneyRepository {
        override suspend fun getHabitualJourney():JourneyReadResult {
            calls++;if(cancel)throw CancellationException("cancelled")
            if(fail)error("private-secret-path")
            if(revoke)allowed.value=false
            return if(configured)JourneyReadResult.Configured(HabitualJourney("POC A","POC B",35)) else JourneyReadResult.NotConfigured
        }
        override suspend fun saveHabitualJourney(journey:HabitualJourney)=error("read only")
    },JourneyFixtureRepository{scenario->if(scenario==ArrivalScenario.REFERENCE)null else
        JourneyFixture(HabitualJourney("Demo A","Demo B",35),DepartureInput(time,zone),clock.instant(),
            when(scenario){ArrivalScenario.DEMO_UNKNOWN->null;ArrivalScenario.DEMO_DELAYED->15;else->0})},clock)
    private val rail=QueryRailContext(object:RailContextRepository {
        override val sourceUrl="https://example.invalid/public"
        override suspend fun read():RailFeedRead {feeds++;return RailFeedRead.Failure(RailFailure.HTTP)}
    },RailStationCatalog{listOf(RailStation("18000","A"),RailStation("17000","B"))},clock)
    private fun service()=JourneyAppFunctionService().also{it.arrivalQuery=query;it.railQuery=rail;it.sharing=consent}
    @Test fun equivalentReferenceWithoutIdsDoesNotFetchRail()=runBlocking {
        val expected=(query(DepartureInput(time,zone)) as ArrivalResult.Estimated).value
        val result=service().queryJourney(time,zone)
        assertEquals(expected.arrival,result.arrival);assertEquals(expected.departure,result.departure)
        assertEquals(expected.observedAt,result.observedAt);assertEquals(expected.source.name,result.source)
        assertNull(result.delayMinutes);assertEquals("UNRESOLVED",result.rail.status);assertEquals(0,feeds)
    }
    @Test fun demosPreserveEntireFixedEvidenceIncludingUnknown()=runBlocking {
        for(scenario in listOf("DEMO_NORMAL","DEMO_DELAYED","DEMO_UNKNOWN")) {
            val result=service().queryJourney(LocalDateTime.parse("2099-02-03T10:00"),ZoneOffset.UTC,scenario)
            assertEquals(time.atZone(zone).toInstant(),result.departure);assertEquals("FIXTURE",result.source)
            assertEquals(clock.instant(),result.observedAt);assertEquals("DEMO_SKIPPED",result.rail.status)
            if(scenario=="DEMO_UNKNOWN"){assertNull(result.arrival);assertNull(result.delayMinutes)}
        }
        assertEquals(0,feeds)
    }
    @Test fun missingConsentReturnsNoJourneyData()=runBlocking {
        allowed.value=false
        try{service().queryJourney(time,zone);fail()}catch(_:AppFunctionDeniedException){}
        assertEquals(0,calls)
    }
    @Test fun revokedDuringReadReturnsNoJourneyData()=runBlocking {
        revoke=true
        try{service().queryJourney(time,zone);fail()}catch(_:AppFunctionDeniedException){}
    }
    @Test fun malformedOptionsAreRejectedBeforeJourneyRead()=runBlocking {
        for(invoke in listOf<suspend()->Unit>(
            {service().queryJourney(time,zone,"other")},
            {service().queryJourney(time,zone,utcOffset="bad")},
            {service().queryJourney(time,zone,originStationId="18000")},
            {service().queryJourney(time,zone,originStationId="18000",destinationStationId="18000")},
            {service().queryJourney(time,zone,originStationId="missing",destinationStationId="17000")})) {
            try{invoke();fail()}catch(_:AppFunctionInvalidArgumentException){}
        }
        assertEquals(0,calls)
    }
    @Test fun pastAndDstCasesGiveBoundedInvalidArgument()=runBlocking {
        for(input in listOf(LocalDateTime.parse("2020-01-01T08:00"),LocalDateTime.parse("2027-03-28T02:30"),LocalDateTime.parse("2027-10-31T02:30"))) {
            try{service().queryJourney(input,zone);fail()}catch(_:AppFunctionInvalidArgumentException){}
        }
    }
    @Test fun resolvedDstOverlapMatchesDomain()=runBlocking {
        val input=LocalDateTime.parse("2027-10-31T02:30")
        val result=service().queryJourney(input,zone,utcOffset="+01:00")
        assertEquals(input.toInstant(ZoneOffset.ofHours(1)),result.departure)
    }
    @Test fun absentAndFailedStorageMapToSafeErrors()=runBlocking {
        configured=false
        try{service().queryJourney(time,zone);fail()}catch(_:AppFunctionElementNotFoundException){}
        fail=true
        try{service().queryJourney(time,zone);fail()}catch(e:AppFunctionAppUnknownException){assertFalse(e.errorMessage.orEmpty().contains("private"))}
    }
    @Test fun feedFailurePreservesArrivalAndExplicitUnavailability()=runBlocking {
        val result=service().queryJourney(time,zone,originStationId="18000",destinationStationId="17000")
        assertEquals(time.atZone(zone).toInstant().plusSeconds(35*60),result.arrival)
        assertEquals("UNAVAILABLE",result.rail.status);assertEquals("HTTP",result.rail.failure)
        assertEquals("ENDPOINTS_ONLY",result.rail.coverage);assertEquals(1,feeds)
    }
    @Test fun cancellationIsNotRewrittenAsUnknownFailure()=runBlocking {
        cancel=true
        try{service().queryJourney(time,zone);fail()}catch(_:CancellationException){}
    }
}
